package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.*;
import android.util.Pair;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.LiveData;
import com.cloudy.quranbuilder.MainActivity;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.databinding.FragmentBrowseBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrowseFragment extends Fragment implements SurahAdapter.OnSurahClickListener {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private FragmentBrowseBinding binding;
    private SurahAdapter adapter;

    // آخر بيانات وصلت من LiveData
    private List<SurahEntity>              latestSurahs = new ArrayList<>();
    private List<AyahDao.SurahStat>        latestStats  = new ArrayList<>();
    private String                          currentQuery = "";

    private MediatorLiveData<Pair<List<SurahEntity>, List<AyahDao.SurahStat>>> combined;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentBrowseBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        adapter = new SurahAdapter(this);
        binding.recyclerSurahs.setAdapter(adapter);

        // ── LiveData مُركَّب: سور + إحصاءات ─────────────────
        AppDatabase db = AppDatabase.getInstance(requireContext());
        LiveData<List<SurahEntity>>       surahsLd = db.surahDao().getAllSurahsLive();
        LiveData<List<AyahDao.SurahStat>> statsLd  = db.ayahDao().getSurahStatsLive();

        combined = new MediatorLiveData<>();
        combined.addSource(surahsLd, surahs ->
                combined.setValue(new Pair<>(surahs, statsLd.getValue())));
        combined.addSource(statsLd, stats ->
                combined.setValue(new Pair<>(surahsLd.getValue(), stats)));

        combined.observe(getViewLifecycleOwner(), pair -> {
            latestSurahs = pair.first  != null ? pair.first  : new ArrayList<>();
            latestStats  = pair.second != null ? pair.second : new ArrayList<>();
            rebuildList(currentQuery);
        });

        // ── البحث ────────────────────────────────────────────
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                currentQuery = s.toString().trim();
                rebuildList(currentQuery);
            }
        });

        // ── FAB ──────────────────────────────────────────────
        binding.fabAddSurah.setOnClickListener(v -> {
            AddSurahSheet sheet = new AddSurahSheet();
            // LiveData يُحدّث القائمة تلقائياً بعد الإضافة
            sheet.show(getParentFragmentManager(), "add_surah");
        });
    }

    /** يُعيد بناء القائمة في الخلفية مع الفلتر الحالي */
    private void rebuildList(String query) {
        final List<SurahEntity>       snapSurahs = new ArrayList<>(latestSurahs);
        final List<AyahDao.SurahStat> snapStats  = new ArrayList<>(latestStats);

        EXECUTOR.execute(() -> {
            // بناء statsMap
            Map<Integer, AyahDao.SurahStat> sm = new HashMap<>();
            for (AyahDao.SurahStat st : snapStats) sm.put(st.surahNumber, st);

            int totalAyahs = 0;
            for (AyahDao.SurahStat st : snapStats) totalAyahs += st.count;
            final int finalTotal = totalAyahs;

            // بناء الصفوف
            List<SurahAdapter.SurahRow> rows = buildRows(snapSurahs, sm, query);

            if (!isAdded() || binding == null) return;
            final String stats = snapSurahs.size() + " سورة · " + finalTotal + " آية محفوظة";
            List<SurahAdapter.SurahRow> finalRows = rows;

            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                adapter.submitList(finalRows);
                binding.tvStats.setText(stats);
            });
        });
    }

    private List<SurahAdapter.SurahRow> buildRows(
            List<SurahEntity> surahs,
            Map<Integer, AyahDao.SurahStat> stats,
            String query) {

        List<SurahAdapter.SurahRow> rows = new ArrayList<>();
        for (SurahEntity s : surahs) {
            if (!query.isEmpty()
                    && !s.name.contains(query)
                    && !String.valueOf(s.number).contains(query)) continue;

            AyahDao.SurahStat stat = stats.get(s.number);
            int savedCount = stat != null ? stat.count  : 0;
            int minJuz     = stat != null ? stat.minJuz : 0;
            SurahInfo info = SurahInfo.getByNumber(s.number);
            rows.add(new SurahAdapter.SurahRow(info, s, savedCount, minJuz));
        }
        return rows;
    }

    @Override
    public void onSurahClick(SurahInfo surahInfo) {
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).openAyahsScreen(
                    surahInfo != null ? surahInfo.number : 0,
                    surahInfo != null ? surahInfo.name   : "");
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
