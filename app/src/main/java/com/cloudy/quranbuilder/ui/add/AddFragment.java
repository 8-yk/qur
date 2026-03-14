package com.cloudy.quranbuilder.ui.add;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.databinding.FragmentAddBinding;
import com.cloudy.quranbuilder.model.JsonModels;
import com.cloudy.quranbuilder.utils.TextParser;
import com.google.android.material.snackbar.Snackbar;
import java.util.*;
import java.util.concurrent.Executors;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private ParsedAyahAdapter  previewAdapter;
    private List<JsonModels.AyahJson> parsedAyahs = new ArrayList<>();

    private int selectedSurahNumber = -1;
    private int calculatedJuz   = 0;
    private int calculatedHizb  = 0;
    private int calculatedHizbQ = 0;
    private int enteredPage     = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentAddBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);
        setupSurahPicker();
        setupPreviewList();
        setupHizbWatchers();
        setupButtons();
    }

    // ─── اختيار السورة من DB ──────────────────────────────────
    private void setupSurahPicker() {
        binding.btnPickSurah.setOnClickListener(v -> {
            SurahPickerBottomSheet sheet = new SurahPickerBottomSheet();
            sheet.setOnSurahSelectedListener(surah -> {
                selectedSurahNumber = surah.number;
                binding.tvSelectedSurah.setText(surah.number + " - " + surah.name);
                reparse();
            });
            sheet.show(getParentFragmentManager(), "surah_picker");
        });
    }

    // ─── حساب الحزب + مراقبة الصفحة ──────────────────────────
    private void setupHizbWatchers() {
        TextWatcher hizbW = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) { recalcHizb(); }
        };
        binding.etJuz.addTextChangedListener(hizbW);
        binding.etHizbQuarterInJuz.addTextChangedListener(hizbW);

        binding.etPage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                try { enteredPage = Integer.parseInt(s.toString().trim()); }
                catch (NumberFormatException ignored) { enteredPage = 0; }
            }
        });
    }

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
            int juz    = Math.max(1, Math.min(30, Integer.parseInt(juzStr)));
            int qInJuz = Math.max(1, Math.min(8,  Integer.parseInt(qStr)));
            calculatedJuz   = juz;
            calculatedHizb  = (juz - 1) * 2 + (int) Math.ceil(qInJuz / 4.0);
            calculatedHizbQ = ((qInJuz - 1) % 4) + 1;
            binding.tvCalcHizb.setText("الجزء " + calculatedJuz
                + " · الحزب " + calculatedHizb + " · ربع الحزب " + calculatedHizbQ);
            binding.tvCalcHizb.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ignored) {
            binding.tvCalcHizb.setVisibility(View.GONE);
        }
    }

    // ─── قائمة المعاينة ────────────────────────────────────────
    private void setupPreviewList() {
        previewAdapter = new ParsedAyahAdapter();
        binding.recyclerPreview.setAdapter(previewAdapter);
    }

    // ─── الأزرار ───────────────────────────────────────────────
    private void setupButtons() {
        binding.etRawText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                if (s.length() > 5) reparse();
                else { parsedAyahs.clear(); previewAdapter.submitList(new ArrayList<>()); updatePreviewState(); }
            }
        });
        binding.btnParse.setOnClickListener(v -> reparse());
        binding.btnClear.setOnClickListener(v -> {
            binding.etRawText.setText("");
            parsedAyahs.clear(); previewAdapter.submitList(new ArrayList<>()); updatePreviewState();
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
        boolean hasSurah = selectedSurahNumber > 0;
        boolean hasAyahs = !parsedAyahs.isEmpty();
        if (!hasAyahs) {
            binding.tvPreviewCount.setText("لا يوجد آيات محللة");
            binding.btnSave.setEnabled(false);
            binding.cardPreview.setVisibility(View.GONE);
        } else if (!hasSurah) {
            binding.tvPreviewCount.setText("تم تحليل " + parsedAyahs.size() + " آية — اختر سورة أولاً");
            binding.btnSave.setEnabled(false);
            binding.cardPreview.setVisibility(View.VISIBLE);
        } else {
            binding.tvPreviewCount.setText("تم تحليل " + parsedAyahs.size() + " آية");
            binding.btnSave.setEnabled(true);
            binding.cardPreview.setVisibility(View.VISIBLE);
        }
    }

    private void saveToDatabase() {
        if (parsedAyahs.isEmpty() || selectedSurahNumber < 1 || !isAdded() || binding == null) return;
        binding.btnSave.setEnabled(false);
        binding.progressSave.setVisibility(View.VISIBLE);

        final int finalNum  = selectedSurahNumber;
        final int finalJuz  = calculatedJuz;
        final int finalHizb = calculatedHizb;
        final int finalHizbQ= calculatedHizbQ;
        final int finalPage = enteredPage;
        final List<JsonModels.AyahJson> toSave = new ArrayList<>(parsedAyahs);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            // نجلب السورة الموجودة للحفاظ على isMeccan واسمها
            SurahEntity existing = db.surahDao().getByNumber(finalNum);
            if (existing != null) {
                // نُعيد حفظها كما هي (لا نغيّر شيئاً)
                db.surahDao().insertSurah(existing);
            }

            List<AyahEntity> entities = new ArrayList<>();
            for (JsonModels.AyahJson a : toSave)
                entities.add(new AyahEntity(finalNum, a.numberInSurah, a.text,
                        finalJuz, finalHizb, finalHizbQ, finalPage));
            db.ayahDao().insertAll(entities);

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.progressSave.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Snackbar.make(binding.getRoot(),
                    "✓ تم حفظ " + toSave.size() + " آية", Snackbar.LENGTH_LONG).show();
                binding.etRawText.setText("");
                parsedAyahs.clear(); previewAdapter.submitList(new ArrayList<>()); updatePreviewState();
            });
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
