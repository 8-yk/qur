package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

/**
 * إضافة سورة يدوياً — رقم + اسم + مكية/مدنية.
 * عدد الآيات يُحسب تلقائياً من الآيات المضافة فعلياً.
 */
public class AddSurahSheet extends BottomSheetDialogFragment {

    public interface OnSurahAddedListener { void onAdded(); }
    private OnSurahAddedListener addedListener;
    public void setOnSurahAddedListener(OnSurahAddedListener l) { addedListener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle saved) {
        return inflater.inflate(R.layout.bottom_sheet_add_surah, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle saved) {
        super.onViewCreated(root, saved);

        TextInputEditText etNumber = root.findViewById(R.id.etSurahNumber);
        TextInputEditText etName   = root.findViewById(R.id.etSurahName);
        ChipGroup  chipGroup       = root.findViewById(R.id.chipGroupAddMeccan);
        Chip       chipMeccan      = root.findViewById(R.id.chipAddMeccan);
        MaterialButton btnAdd      = root.findViewById(R.id.btnConfirmAddSurah);

        root.findViewById(R.id.btnAddSurahClose).setOnClickListener(v -> dismiss());

        // تفعيل زر الإضافة فقط حين يُملأ الاسم والرقم
        TextWatcher enableBtn = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                String num  = etNumber.getText() != null ? etNumber.getText().toString().trim() : "";
                String name = etName.getText()   != null ? etName.getText().toString().trim()   : "";
                btnAdd.setEnabled(!num.isEmpty() && !name.isEmpty());
            }
        };
        etNumber.addTextChangedListener(enableBtn);
        etName.addTextChangedListener(enableBtn);
        btnAdd.setEnabled(false);

        chipMeccan.setChecked(true);

        btnAdd.setOnClickListener(v -> {
            String numStr = etNumber.getText() != null ? etNumber.getText().toString().trim() : "";
            String name   = etName.getText()   != null ? etName.getText().toString().trim()   : "";

            if (numStr.isEmpty() || name.isEmpty()) {
                Toast.makeText(requireContext(), "أدخل رقم السورة واسمها", Toast.LENGTH_SHORT).show();
                return;
            }

            int number;
            try { number = Integer.parseInt(numStr); }
            catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "رقم السورة غير صحيح", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isMeccan = chipMeccan.isChecked();
            final int    finalNum    = number;
            final String finalName   = name;
            final boolean finalMeccan = isMeccan;

            Executors.newSingleThreadExecutor().execute(() -> {
                // ayahsInSurah = 0 دائماً — يُحسب من الآيات المضافة لاحقاً
                AppDatabase.getInstance(requireContext())
                        .surahDao().insertSurah(new SurahEntity(finalNum, finalName, finalMeccan, 0));
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (addedListener != null) addedListener.onAdded();
                    dismiss();
                });
            });
        });
    }
}
