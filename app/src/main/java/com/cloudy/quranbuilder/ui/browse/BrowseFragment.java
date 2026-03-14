package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.cloudy.quranbuilder.MainActivity;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.databinding.FragmentBrowseBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import java.util.*;
import java.util.concurrent.Executors;

public class BrowseFragment extends Fragment implements SurahAdapter.OnSurahClickListener {

    private FragmentBrowseBinding binding;
    private SurahAdapter adapter;

    // فقط السور الموجودة في DB
    private List<SurahEntity>              dbSurahList = new ArrayList<>();
    private Map<Integer, AyahDao.SurahStat> statsMap   = new HashMap<>();

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

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                filterSurahs(s.toString());
            }
        });

        binding.fabAddSurah.setOnClickListener(v -> {
            AddSurahSheet sheet = new AddSurahSheet();
            sheet.setOnSurahAddedListener(() -> loadData());
            sheet.show(getParentFragmentManager(), "add_surah");
        });

        loadData();
    }

    @Override public void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        if (!isAdded()) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // السور من DB فقط
            List<SurahEntity> surahs = db.surahDao().getAllSurahsSync();

            // إحصاءات الآيات
            List<AyahDao.SurahStat> stats = db.ayahDao().getSurahStats();
            Map<Integer, AyahDao.SurahStat> sm = new HashMap<>();
            for (AyahDao.SurahStat st : stats) sm.put(st.surahNumber, st);

            int totalAyahs = db.ayahDao().getTotalAyahCount();

            List<SurahAdapter.SurahRow> rows = buildRows(surahs, sm, "");

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                dbSurahList = surahs;
                statsMap    = sm;
                adapter.submitList(rows);
                binding.tvStats.setText(surahs.size() + " سورة · " + totalAyahs + " آية محفوظة");
            });
        });
    }

    private void filterSurahs(String query) {
        if (!isAdded()) return;
        final List<SurahEntity>              snapSurahs = new ArrayList<>(dbSurahList);
        final Map<Integer, AyahDao.SurahStat> snapStats = new HashMap<>(statsMap);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SurahAdapter.SurahRow> rows = buildRows(snapSurahs, snapStats, query.trim());
            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                adapter.submitList(rows);
            });
        });
    }

    /** يبني الـ rows من السور الموجودة في DB فقط */
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

            // SurahInfo لمعرفة إجمالي الآيات الثابت (إن وُجد)
            SurahInfo info = SurahInfo.getByNumber(s.number);

            rows.add(new SurahAdapter.SurahRow(info, s, savedCount, minJuz));
        }
        return rows;
    }

    @Override
    public void onSurahClick(SurahInfo surahInfo) {
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).openAyahsScreen(surahInfo != null ? surahInfo.number : 0,
                    surahInfo != null ? surahInfo.name : "");
    }

    // يُستدعى من SurahAdapter عند الضغط على سورة ليس لها SurahInfo ثابتة
    public void onDbSurahClick(SurahEntity surah) {
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).openAyahsScreen(surah.number, surah.name);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
