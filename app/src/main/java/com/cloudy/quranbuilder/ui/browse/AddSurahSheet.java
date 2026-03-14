package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.*;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddSurahSheet extends BottomSheetDialogFragment {

    public interface OnSurahAddedListener { void onAdded(); }
    private OnSurahAddedListener addedListener;
    public void setOnSurahAddedListener(OnSurahAddedListener l) { addedListener = l; }

    // حالة الاختيار من قائمة الـ 114
    private int    pickedNumber     = -1;
    private String pickedName       = "";
    private boolean pickedIsMeccan  = true;
    private int    pickedTotalAyahs = 0;

    // Adapter داخلي للقائمة
    private SurahInfo[] filtered;
    private SurahListAdapter listAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle saved) {
        return inflater.inflate(R.layout.bottom_sheet_add_surah, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle saved) {
        super.onViewCreated(root, saved);

        EditText etSearch     = root.findViewById(R.id.etAddSurahSearch);
        ListView listView     = root.findViewById(R.id.listAddSurah);
        ChipGroup chipGroup   = root.findViewById(R.id.chipGroupAddMeccan);
        Chip chipMeccan       = root.findViewById(R.id.chipAddMeccan);
        Chip chipMadani       = root.findViewById(R.id.chipAddMadani);
        TextInputEditText etAyahsCount = root.findViewById(R.id.etAddAyahsCount);
        MaterialButton btnAdd = root.findViewById(R.id.btnConfirmAddSurah);
        TextView tvSelected   = root.findViewById(R.id.tvAddSurahSelected);
        root.findViewById(R.id.btnAddSurahClose).setOnClickListener(v -> dismiss());

        // إعداد قائمة السور
        filtered = SurahInfo.ALL_SURAHS.clone();
        listAdapter = new SurahListAdapter();
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((parent, view, pos, id) -> {
            SurahInfo info  = filtered[pos];
            pickedNumber    = info.number;
            pickedName      = info.name;
            pickedIsMeccan  = info.isMeccan;
            pickedTotalAyahs= info.totalAyahs;
            tvSelected.setText(info.number + " - " + info.name);
            tvSelected.setVisibility(View.VISIBLE);
            chipMeccan.setChecked(info.isMeccan);
            chipMadani.setChecked(!info.isMeccan);
            etAyahsCount.setText(String.valueOf(info.totalAyahs));
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                String q = s.toString().trim();
                List<SurahInfo> res = new ArrayList<>();
                for (SurahInfo info : SurahInfo.ALL_SURAHS) {
                    if (q.isEmpty() || info.name.contains(q) || String.valueOf(info.number).contains(q))
                        res.add(info);
                }
                filtered = res.toArray(new SurahInfo[0]);
                listAdapter.notifyDataSetChanged();
            }
        });

        chipGroup.setOnCheckedStateChangeListener((g, ids) ->
                pickedIsMeccan = ids.contains(R.id.chipAddMeccan));
        chipMeccan.setChecked(true);

        btnAdd.setOnClickListener(v -> {
            if (pickedNumber < 1) {
                Toast.makeText(requireContext(), "اختر سورة أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            int count = pickedTotalAyahs;
            try {
                String s = etAyahsCount.getText().toString().trim();
                if (!s.isEmpty()) count = Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}

            final int    num      = pickedNumber;
            final String name     = pickedName;
            final boolean meccan  = chipMeccan.isChecked();
            final int    totalAyahs = count;

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(requireContext())
                        .surahDao().insertSurah(new SurahEntity(num, name, meccan, totalAyahs));
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (addedListener != null) addedListener.onAdded();
                    dismiss();
                });
            });
        });
    }

    // ─── Adapter بسيط لـ ListView ────────────────────────────
    class SurahListAdapter extends BaseAdapter {
        @Override public int getCount() { return filtered.length; }
        @Override public Object getItem(int p) { return filtered[p]; }
        @Override public long getItemId(int p) { return filtered[p].number; }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_surah_picker, parent, false);
            SurahInfo info = filtered[pos];
            ((TextView) convertView.findViewById(R.id.tvPickerNum)).setText(String.valueOf(info.number));
            ((TextView) convertView.findViewById(R.id.tvPickerName)).setText(info.name);
            ((TextView) convertView.findViewById(R.id.tvPickerMeta))
                    .setText(info.getRevelationType() + " · " + info.totalAyahs + " آية");
            return convertView;
        }
    }
}
