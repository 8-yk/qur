package com.cloudy.quranbuilder.ui.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.recyclerview.widget.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * يعرض السور المضافة في قاعدة البيانات فقط (لا القائمة الثابتة).
 */
public class SurahPickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnSurahSelectedListener { void onSurahSelected(SurahEntity surah); }

    private OnSurahSelectedListener listener;
    private PickerAdapter adapter;
    private List<SurahEntity> allSurahs = new ArrayList<>();
    private List<SurahEntity> filtered  = new ArrayList<>();

    public void setOnSurahSelectedListener(OnSurahSelectedListener l) { this.listener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle s) {
        return inflater.inflate(R.layout.bottom_sheet_surah_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        RecyclerView recycler = view.findViewById(R.id.recyclerPickerSurahs);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PickerAdapter(filtered, surah -> {
            if (listener != null) listener.onSurahSelected(surah);
            dismiss();
        });
        recycler.setAdapter(adapter);

        EditText etSearch = view.findViewById(R.id.etPickerSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int co) {
                filterSurahs(s.toString().trim());
            }
        });

        // جلب السور من DB
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SurahEntity> dbSurahs = AppDatabase.getInstance(requireContext())
                    .surahDao().getAllSurahsSync();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                allSurahs.addAll(dbSurahs);
                filtered.addAll(dbSurahs);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void filterSurahs(String q) {
        filtered.clear();
        for (SurahEntity s : allSurahs) {
            if (q.isEmpty() || s.name.contains(q) || String.valueOf(s.number).contains(q))
                filtered.add(s);
        }
        adapter.notifyDataSetChanged();
    }

    static class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.VH> {
        interface OnPick { void pick(SurahEntity s); }
        private final List<SurahEntity> list;
        private final OnPick onPick;
        PickerAdapter(List<SurahEntity> l, OnPick p) { list = l; onPick = p; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_surah_picker, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(list.get(pos)); }
        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            final TextView tvNum, tvName, tvMeta;
            VH(View v) {
                super(v);
                tvNum  = v.findViewById(R.id.tvPickerNum);
                tvName = v.findViewById(R.id.tvPickerName);
                tvMeta = v.findViewById(R.id.tvPickerMeta);
            }
            void bind(SurahEntity s) {
                tvNum.setText(String.valueOf(s.number));
                tvName.setText(s.name);
                String type = s.isMeccan ? "مكية" : "مدنية";
                tvMeta.setText(type + (s.ayahsInSurah > 0 ? " · " + s.ayahsInSurah + " آية" : ""));
                itemView.setOnClickListener(v -> onPick.pick(s));
            }
        }
    }
}
