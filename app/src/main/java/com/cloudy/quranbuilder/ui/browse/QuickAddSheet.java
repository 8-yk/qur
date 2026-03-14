package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.model.JsonModels;
import com.cloudy.quranbuilder.utils.TextParser;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.*;
import java.util.concurrent.Executors;

public class QuickAddSheet extends BottomSheetDialogFragment {

    public interface OnAyahsSavedListener { void onSaved(int count); }

    private static final String ARG_NUM  = "surah_num";
    private static final String ARG_NAME = "surah_name";
    private static final String ARG_MC   = "is_meccan";

    private OnAyahsSavedListener savedListener;
    public void setOnAyahsSavedListener(OnAyahsSavedListener l) { savedListener = l; }

    public static QuickAddSheet newInstance(int num, String name, boolean meccan) {
        QuickAddSheet s = new QuickAddSheet();
        Bundle b = new Bundle();
        b.putInt(ARG_NUM, num); b.putString(ARG_NAME, name); b.putBoolean(ARG_MC, meccan);
        s.setArguments(b);
        return s;
    }

    private int surahNumber; private String surahName; private boolean isMeccan;
    private List<JsonModels.AyahJson> parsed = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.bottom_sheet_quick_add, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        if (getArguments() != null) {
            surahNumber = getArguments().getInt(ARG_NUM, 1);
            surahName   = getArguments().getString(ARG_NAME, "");
            isMeccan    = getArguments().getBoolean(ARG_MC, true);
        }

        TextView       tvTitle   = view.findViewById(R.id.tvQuickAddSurahName);
        TextView       tvCount   = view.findViewById(R.id.tvQuickCount);
        MaterialButton btnSave   = view.findViewById(R.id.btnQuickSave);
        TextInputEditText etText = view.findViewById(R.id.etQuickText);
        TextInputEditText etJuz  = view.findViewById(R.id.etQuickJuz);
        TextInputEditText etHizbQ= view.findViewById(R.id.etQuickHizbQ);
        TextInputEditText etStart= view.findViewById(R.id.etQuickStartAyah);
        // حقل الصفحة (اختياري في الـ sheet)
        TextInputEditText etPage = view.findViewById(R.id.etQuickPage);

        tvTitle.setText("سورة " + surahName);
        view.findViewById(R.id.btnQuickAddClose).setOnClickListener(v -> dismiss());

        etText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                if (s.length() < 5) {
                    parsed.clear(); tvCount.setText("لا يوجد آيات محللة"); btnSave.setEnabled(false); return;
                }
                int startAyah = 1;
                try { if (etStart != null && etStart.getText() != null)
                    startAyah = Integer.parseInt(etStart.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                parsed = TextParser.parseText(s.toString().trim(), startAyah);
                tvCount.setText("تم تحليل " + parsed.size() + " آية");
                btnSave.setEnabled(!parsed.isEmpty());
            }
        });

        btnSave.setOnClickListener(v -> {
            if (parsed.isEmpty()) return;
            btnSave.setEnabled(false);

            int juz = 0, hizb = 0, hizbQ = 0, page = 0;
            try {
                String jStr = etJuz != null && etJuz.getText() != null ? etJuz.getText().toString().trim() : "";
                String qStr = etHizbQ != null && etHizbQ.getText() != null ? etHizbQ.getText().toString().trim() : "";
                if (!jStr.isEmpty() && !qStr.isEmpty()) {
                    juz   = Math.max(1, Math.min(30, Integer.parseInt(jStr)));
                    int q = Math.max(1, Math.min(8,  Integer.parseInt(qStr)));
                    hizb  = (juz - 1) * 2 + (int) Math.ceil(q / 4.0);
                    hizbQ = ((q - 1) % 4) + 1;
                }
                if (etPage != null && etPage.getText() != null && !etPage.getText().toString().trim().isEmpty())
                    page = Integer.parseInt(etPage.getText().toString().trim());
            } catch (NumberFormatException ignored) {}

            final int fJuz = juz, fHizb = hizb, fHizbQ = hizbQ, fPage = page;
            final List<JsonModels.AyahJson> toSave = new ArrayList<>(parsed);
            final int fNum = surahNumber; final String fName = surahName; final boolean fMc = isMeccan;

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                // حافظ على ayahsInSurah الموجود
                SurahEntity existing = db.surahDao().getByNumber(fNum);
                int totalAyahs = existing != null ? existing.ayahsInSurah : 0;
                db.surahDao().insertSurah(new SurahEntity(fNum, fName, fMc, totalAyahs));
                List<AyahEntity> entities = new ArrayList<>();
                for (JsonModels.AyahJson a : toSave)
                    entities.add(new AyahEntity(fNum, a.numberInSurah, a.text, fJuz, fHizb, fHizbQ, fPage));
                db.ayahDao().insertAll(entities);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (savedListener != null) savedListener.onSaved(toSave.size());
                    dismiss();
                });
            });
        });
    }
}
