package com.cloudy.quranbuilder.ui.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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

    // ─── الحالة الحالية ────────────────────────────────────────
    private int selectedSurahNumber = 1;
    private int calculatedJuz       = 0;
    private int calculatedHizb      = 0;
    private int calculatedHizbQ     = 0; // ربع الحزب 1-4

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);
        setupSurahDropdown();
        setupPreviewList();
        setupHizbWatchers();
        setupButtons();
    }

    // ─── إعداد قائمة السور ─────────────────────────────────────
    private void setupSurahDropdown() {
        String[] surahNames = new String[114];
        for (int i = 0; i < 114; i++) {
            SurahInfo info = SurahInfo.ALL_SURAHS[i];
            surahNames[i] = info.number + " - " + info.name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                surahNames);

        binding.dropdownSurah.setAdapter(adapter);
        binding.dropdownSurah.setText(surahNames[0], false);
        selectedSurahNumber = 1;

        // ← إصلاح bug الحفظ: نأخذ الرقم من النص المختار وليس position
        binding.dropdownSurah.setOnItemClickListener((parent, v, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            try {
                selectedSurahNumber = Integer.parseInt(item.split(" - ")[0].trim());
            } catch (NumberFormatException ignored) {
                selectedSurahNumber = 1;
            }
            reparse();
        });
    }

    // ─── مراقبة حقلي الجزء وربع الحزب لحساب الحزب تلقائياً ────
    private void setupHizbWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalcHizb();
            }
        };
        binding.etJuz.addTextChangedListener(watcher);
        binding.etHizbQuarterInJuz.addTextChangedListener(watcher);
    }

    /**
     * كل جزء = 8 أرباع حزب (= 2 حزب × 4)
     * الحزب المطلق = (الجزء-1)×2 + ceil(ربع_الحزب_في_الجزء / 4)
     * ربع الحزب (1-4) = ((ربع_في_الجزء - 1) % 4) + 1
     */
    private void recalcHizb() {
        if (binding == null) return;
        String juzStr = binding.etJuz.getText().toString().trim();
        String qStr   = binding.etHizbQuarterInJuz.getText().toString().trim();

        if (juzStr.isEmpty() || qStr.isEmpty()) {
            calculatedJuz = 0; calculatedHizb = 0; calculatedHizbQ = 0;
            binding.tvCalcHizb.setVisibility(View.GONE);
            return;
        }

        try {
            int juz = Integer.parseInt(juzStr);
            int qInJuz = Integer.parseInt(qStr);

            // التحقق من النطاقات
            juz    = Math.max(1, Math.min(30, juz));
            qInJuz = Math.max(1, Math.min(8, qInJuz));

            calculatedJuz   = juz;
            calculatedHizb  = (juz - 1) * 2 + (int) Math.ceil(qInJuz / 4.0);
            calculatedHizbQ = ((qInJuz - 1) % 4) + 1;

            binding.tvCalcHizb.setText(
                "الجزء " + calculatedJuz +
                " · الحزب " + calculatedHizb +
                " · ربع الحزب " + calculatedHizbQ
            );
            binding.tvCalcHizb.setVisibility(View.VISIBLE);

        } catch (NumberFormatException ignored) {
            binding.tvCalcHizb.setVisibility(View.GONE);
        }
    }

    // ─── قائمة المعاينة ─────────────────────────────────────────
    private void setupPreviewList() {
        previewAdapter = new ParsedAyahAdapter();
        binding.recyclerPreview.setAdapter(previewAdapter);
    }

    // ─── الأزرار ────────────────────────────────────────────────
    private void setupButtons() {
        binding.etRawText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 5) reparse();
                else { parsedAyahs.clear(); previewAdapter.submitList(new ArrayList<>()); updatePreviewState(); }
            }
        });

        binding.btnParse.setOnClickListener(v -> reparse());
        binding.btnClear.setOnClickListener(v -> {
            binding.etRawText.setText("");
            parsedAyahs.clear();
            previewAdapter.submitList(new ArrayList<>());
            updatePreviewState();
        });
        binding.btnSave.setOnClickListener(v -> saveToDatabase());
    }

    private void reparse() {
        if (binding == null) return;
        String text = binding.etRawText.getText().toString().trim();
        if (text.isEmpty()) return;

        int startAyah = 1;
        try {
            String s = binding.etStartAyah.getText().toString().trim();
            if (!s.isEmpty()) startAyah = Integer.parseInt(s);
        } catch (NumberFormatException ignored) {}

        parsedAyahs = TextParser.parseText(text, startAyah);
        previewAdapter.submitList(new ArrayList<>(parsedAyahs));
        updatePreviewState();
    }

    private void updatePreviewState() {
        if (binding == null) return;
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
        if (parsedAyahs.isEmpty() || !isAdded() || binding == null) return;

        binding.btnSave.setEnabled(false);
        binding.progressSave.setVisibility(View.VISIBLE);

        SurahInfo info = SurahInfo.getByNumber(selectedSurahNumber);
        String surahName = info != null ? info.name : "سورة " + selectedSurahNumber;
        int finalSurahNum = selectedSurahNumber;
        int finalJuz      = calculatedJuz;
        int finalHizb     = calculatedHizb;
        int finalHizbQ    = calculatedHizbQ;
        List<JsonModels.AyahJson> toSave = new ArrayList<>(parsedAyahs);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.surahDao().insertSurah(new SurahEntity(finalSurahNum, surahName));

            List<AyahEntity> entities = new ArrayList<>();
            for (JsonModels.AyahJson ayah : toSave) {
                entities.add(new AyahEntity(
                        finalSurahNum, ayah.numberInSurah, ayah.text,
                        finalJuz, finalHizb, finalHizbQ));
            }
            db.ayahDao().insertAll(entities);

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.progressSave.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);

                Snackbar.make(binding.getRoot(),
                    "✓ تم حفظ " + toSave.size() + " آية من سورة " + surahName,
                    Snackbar.LENGTH_LONG).show();

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
