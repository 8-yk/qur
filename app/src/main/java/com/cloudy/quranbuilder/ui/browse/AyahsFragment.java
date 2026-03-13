package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.databinding.FragmentAyahsBinding;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.List;
import java.util.concurrent.Executors;

public class AyahsFragment extends Fragment {

    private static final String ARG_SURAH_NUMBER = "surah_number";
    private static final String ARG_SURAH_NAME   = "surah_name";

    private FragmentAyahsBinding binding;
    private AyahAdapter adapter;
    private int surahNumber;
    private String surahName;

    public static AyahsFragment newInstance(int surahNumber, String surahName) {
        AyahsFragment f = new AyahsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SURAH_NUMBER, surahNumber);
        args.putString(ARG_SURAH_NAME, surahName);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAyahsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            surahNumber = getArguments().getInt(ARG_SURAH_NUMBER);
            surahName   = getArguments().getString(ARG_SURAH_NAME);
        }

        // زر الرجوع
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.tvSurahTitle.setText(surahName);

        SurahInfo info = SurahInfo.getByNumber(surahNumber);
        if (info != null) {
            binding.tvSurahMeta.setText(
                "سورة " + surahNumber + " · " + info.getRevelationType() + " · " + info.totalAyahs + " آية"
            );
        }

        adapter = new AyahAdapter();
        binding.recyclerAyahs.setAdapter(adapter);

        loadAyahs();
    }

    private void loadAyahs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<AyahEntity> ayahs = db.ayahDao().getAyahsForSurahSync(surahNumber);

            requireActivity().runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);

                if (ayahs.isEmpty()) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.recyclerAyahs.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.GONE);
                    binding.recyclerAyahs.setVisibility(View.VISIBLE);
                    adapter.submitList(ayahs);
                    binding.tvAyahCount.setText(ayahs.size() + " آية محفوظة");
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
