package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudy.quranbuilder.MainActivity;
import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.databinding.FragmentBrowseBinding;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class BrowseFragment extends Fragment implements SurahAdapter.OnSurahClickListener {

    private FragmentBrowseBinding binding;
    private SurahAdapter adapter;
    private Set<Integer> surahsWithData = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBrowseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SurahAdapter(this);
        binding.recyclerSurahs.setAdapter(adapter);

        // إعداد البحث
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSurahs(s.toString());
            }
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // تحديث عند العودة
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Integer> numbersWithData = db.ayahDao().getSurahNumbersWithData();
            surahsWithData = new HashSet<>(numbersWithData);

            List<SurahAdapter.SurahRow> rows = buildRows(SurahInfo.ALL_SURAHS, surahsWithData, "");

            requireActivity().runOnUiThread(() -> {
                adapter.submitList(rows);
                updateStats();
            });
        });
    }

    private void filterSurahs(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SurahAdapter.SurahRow> rows = buildRows(SurahInfo.ALL_SURAHS, surahsWithData, query.trim());
            requireActivity().runOnUiThread(() -> adapter.submitList(rows));
        });
    }

    private List<SurahAdapter.SurahRow> buildRows(SurahInfo[] surahs, Set<Integer> withData, String query) {
        List<SurahAdapter.SurahRow> rows = new ArrayList<>();
        for (SurahInfo info : surahs) {
            if (!query.isEmpty() && !info.name.contains(query) &&
                !String.valueOf(info.number).contains(query)) continue;
            rows.add(new SurahAdapter.SurahRow(info, withData.contains(info.number)));
        }
        return rows;
    }

    private void updateStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int totalAyahs = db.ayahDao().getTotalAyahCount();
            int totalSurahs = surahsWithData.size();

            requireActivity().runOnUiThread(() -> {
                binding.tvStats.setText(totalSurahs + " سورة · " + totalAyahs + " آية محفوظة");
            });
        });
    }

    @Override
    public void onSurahClick(SurahInfo surahInfo) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openAyahsScreen(surahInfo.number, surahInfo.name);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
