package com.cloudy.quranbuilder.ui.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.data.SurahEntity;
import com.cloudy.quranbuilder.databinding.FragmentAddBinding;
import com.cloudy.quranbuilder.model.JsonModels;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.cloudy.quranbuilder.utils.TextParser;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private ParsedAyahAdapter previewAdapter;
    private List<JsonModels.AyahJson> parsedAyahs = new ArrayList<>();
    private int selectedSurahNumber = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSurahDropdown();
        setupPreviewList();
        setupButtons();
    }

    private void setupSurahDropdown() {
        // بناء قائمة السور
        String[] surahNames = new String[114];
        for (int i = 0; i < 114; i++) {
            SurahInfo info = SurahInfo.ALL_SURAHS[i];
            surahNames[i] = info.number + " - " + info.name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                surahNames
        );
        binding.dropdownSurah.setAdapter(adapter);
        binding.dropdownSurah.setText(surahNames[0], false);
        selectedSurahNumber = 1;

        binding.dropdownSurah.setOnItemClickListener((parent, v, position, id) -> {
            selectedSurahNumber = position + 1;
            reparse();
        });
    }

    private void setupPreviewList() {
        previewAdapter = new ParsedAyahAdapter();
        binding.recyclerPreview.setAdapter(previewAdapter);
    }

    private void setupButtons() {
        // زر التحليل الفوري عند تغيير النص
        binding.etRawText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // تحليل تلقائي
                if (s.length() > 5) reparse();
                else {
                    parsedAyahs.clear();
                    previewAdapter.submitList(new ArrayList<>());
                    updatePreviewState();
                }
            }
        });

        // زر التحليل اليدوي
        binding.btnParse.setOnClickListener(v -> reparse());

        // زر المسح
        binding.btnClear.setOnClickListener(v -> {
            binding.etRawText.setText("");
            parsedAyahs.clear();
            previewAdapter.submitList(new ArrayList<>());
            updatePreviewState();
        });

        // زر الحفظ
        binding.btnSave.setOnClickListener(v -> saveToDatabase());
    }

    private void reparse() {
        String text = binding.etRawText.getText().toString().trim();
        if (text.isEmpty()) return;

        int startAyah = 1;
        try {
            String startStr = binding.etStartAyah.getText().toString().trim();
            if (!startStr.isEmpty()) startAyah = Integer.parseInt(startStr);
        } catch (NumberFormatException ignored) {}

        parsedAyahs = TextParser.parseText(text, startAyah);
        previewAdapter.submitList(new ArrayList<>(parsedAyahs));
        updatePreviewState();
    }

    private void updatePreviewState() {
        if (parsedAyahs.isEmpty()) {
            binding.tvPreviewCount.setText("لا يوجد آيات محللة");
            binding.btnSave.setEnabled(false);
            binding.cardPreview.setVisibility(View.GONE);
        } else {
            binding.tvPreviewCount.setText("تم تحليل " + parsedAyahs.size() + " آية");
            binding.btnSave.setEnabled(true);
            binding.cardPreview.setVisibility(View.VISIBLE);
        }
    }

    private void saveToDatabase() {
        if (parsedAyahs.isEmpty()) return;

        binding.btnSave.setEnabled(false);
        binding.progressSave.setVisibility(View.VISIBLE);

        SurahInfo info = SurahInfo.getByNumber(selectedSurahNumber);
        String surahName = info != null ? info.name : "سورة " + selectedSurahNumber;
        int finalSurahNumber = selectedSurahNumber;
        List<JsonModels.AyahJson> toSave = new ArrayList<>(parsedAyahs);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // حفظ معلومات السورة
            db.surahDao().insertSurah(new SurahEntity(finalSurahNumber, surahName));

            // حفظ الآيات (IGNORE الموجود)
            List<AyahEntity> entities = new ArrayList<>();
            for (JsonModels.AyahJson ayah : toSave) {
                entities.add(new AyahEntity(finalSurahNumber, ayah.numberInSurah, ayah.text));
            }
            db.ayahDao().insertAll(entities);

            requireActivity().runOnUiThread(() -> {
                binding.progressSave.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);

                Snackbar.make(
                    binding.getRoot(),
                    "✓ تم حفظ " + toSave.size() + " آية من سورة " + surahName,
                    Snackbar.LENGTH_LONG
                ).show();

                // مسح النص بعد الحفظ
                binding.etRawText.setText("");
                parsedAyahs.clear();
                previewAdapter.submitList(new ArrayList<>());
                updatePreviewState();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
