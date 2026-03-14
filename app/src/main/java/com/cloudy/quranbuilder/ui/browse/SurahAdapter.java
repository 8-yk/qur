package com.cloudy.quranbuilder.ui.browse;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.SurahEntity;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.google.android.material.card.MaterialCardView;

public class SurahAdapter extends ListAdapter<SurahAdapter.SurahRow, SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener { void onSurahClick(SurahInfo surahInfo); }
    private final OnSurahClickListener listener;

    public SurahAdapter(OnSurahClickListener l) { super(DIFF); this.listener = l; }

    private static final DiffUtil.ItemCallback<SurahRow> DIFF = new DiffUtil.ItemCallback<SurahRow>() {
        @Override public boolean areItemsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
            return o.surah.number == n.surah.number;
        }
        @Override public boolean areContentsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
            return o.savedAyahCount == n.savedAyahCount
                && o.surah.isMeccan == n.surah.isMeccan
                && o.surah.name.equals(n.surah.name);
        }
    };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        h.bind(getItem(pos), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvNumber, tvName, tvMeta, tvBadge;

        ViewHolder(@NonNull View v) {
            super(v);
            card     = v.findViewById(R.id.cardSurah);
            tvNumber = v.findViewById(R.id.tvSurahNumber);
            tvName   = v.findViewById(R.id.tvSurahName);
            tvMeta   = v.findViewById(R.id.tvSurahMeta);
            tvBadge  = v.findViewById(R.id.tvBadge);
        }

        void bind(SurahRow row, OnSurahClickListener listener) {
            tvNumber.setText(String.valueOf(row.surah.number));
            tvName.setText(row.surah.name);

            String type = row.surah.isMeccan ? "مكية" : "مدنية";

            if (row.savedAyahCount > 0) {
                String juzStr = row.minJuz > 0 ? " · ج" + row.minJuz : "";
                tvMeta.setText(type + " · " + row.savedAyahCount + " آية محفوظة" + juzStr);
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("محفوظة");
                card.setStrokeColor(itemView.getContext().getColor(R.color.gold_dim));
                card.setAlpha(1.0f);
            } else {
                tvMeta.setText(type);
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("مضافة");
                card.setStrokeColor(itemView.getContext().getColor(R.color.divider_color));
                card.setAlpha(0.85f);
            }

            card.setOnClickListener(v -> {
                // نمرر SurahInfo إن وُجد، وإلا نصنع info مؤقتة من بيانات DB
                SurahInfo info = row.info != null ? row.info
                        : new SurahInfo(row.surah.number, row.surah.name, 0, row.surah.isMeccan);
                listener.onSurahClick(info);
            });
        }
    }

    public static class SurahRow {
        public final SurahInfo   info;   // قد يكون null إن لم يكن في القائمة الثابتة
        public final SurahEntity surah;  // دائماً من DB
        public final int  savedAyahCount;
        public final int  minJuz;

        public SurahRow(SurahInfo info, SurahEntity surah, int savedAyahCount, int minJuz) {
            this.info           = info;
            this.surah          = surah;
            this.savedAyahCount = savedAyahCount;
            this.minJuz         = minJuz;
        }
    }
}
