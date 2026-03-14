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
    private Map<Integer, AyahDao.SurahStat> statsMap  = new HashMap<>();
    private Map<Integer, SurahEntity>       dbSurahMap = new HashMap<>();

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

        // FAB إضافة سورة
        binding.fabAddSurah.setOnClickListener(v -> {
            AddSurahSheet sheet = new AddSurahSheet();
            sheet.setOnSurahAddedListener(() -> loadData());
            sheet.show(getParentFragmentManager(), "add_surah");
        });

        loadData();
    }

    @Override
    public void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        if (!isAdded()) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            List<AyahDao.SurahStat> stats = db.ayahDao().getSurahStats();
            Map<Integer, AyahDao.SurahStat> sm = new HashMap<>();
            for (AyahDao.SurahStat s : stats) sm.put(s.surahNumber, s);

            List<SurahEntity> dbSurahs = db.surahDao().getAllSurahsSync();
            Map<Integer, SurahEntity> dm = new HashMap<>();
            for (SurahEntity s : dbSurahs) dm.put(s.number, s);

            int totalAyahs  = db.ayahDao().getTotalAyahCount();
            int totalSurahs = dm.size();

            List<SurahAdapter.SurahRow> rows = buildRows(sm, dm, "");

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                statsMap   = sm;
                dbSurahMap = dm;
                adapter.submitList(rows);
                binding.tvStats.setText(totalSurahs + " سورة · " + totalAyahs + " آية محفوظة");
            });
        });
    }

    private void filterSurahs(String query) {
        if (!isAdded()) return;
        final Map<Integer, AyahDao.SurahStat> sm = new HashMap<>(statsMap);
        final Map<Integer, SurahEntity>       dm = new HashMap<>(dbSurahMap);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SurahAdapter.SurahRow> rows = buildRows(sm, dm, query.trim());
            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                adapter.submitList(rows);
            });
        });
    }

    private List<SurahAdapter.SurahRow> buildRows(
            Map<Integer, AyahDao.SurahStat> stats,
            Map<Integer, SurahEntity> dbMap,
            String query) {

        List<SurahAdapter.SurahRow> rows = new ArrayList<>();
        for (SurahInfo info : SurahInfo.ALL_SURAHS) {
            SurahEntity dbS = dbMap.get(info.number);
            if (!query.isEmpty()
                    && !info.name.contains(query)
                    && !String.valueOf(info.number).contains(query)) continue;

            AyahDao.SurahStat stat = stats.get(info.number);
            int savedCount   = stat != null ? stat.count  : 0;
            int minJuz       = stat != null ? stat.minJuz : 0;
            boolean isMeccan = dbS != null ? dbS.isMeccan : info.isMeccan;
            int ayahsInSurah = dbS != null ? dbS.ayahsInSurah : info.totalAyahs;
            boolean inDb     = dbS != null;

            rows.add(new SurahAdapter.SurahRow(info, savedCount, minJuz, isMeccan, ayahsInSurah, inDb));
        }
        return rows;
    }

    @Override
    public void onSurahClick(SurahInfo surahInfo) {
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).openAyahsScreen(surahInfo.number, surahInfo.name);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
