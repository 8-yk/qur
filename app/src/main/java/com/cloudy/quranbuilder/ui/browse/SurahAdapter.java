package com.cloudy.quranbuilder.ui.browse;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.google.android.material.card.MaterialCardView;

public class SurahAdapter extends ListAdapter<SurahAdapter.SurahRow, SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener { void onSurahClick(SurahInfo surahInfo); }
    private final OnSurahClickListener listener;

    public SurahAdapter(OnSurahClickListener l) { super(DIFF); this.listener = l; }

    private static final DiffUtil.ItemCallback<SurahRow> DIFF = new DiffUtil.ItemCallback<SurahRow>() {
        @Override public boolean areItemsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
            return o.info.number == n.info.number;
        }
        @Override public boolean areContentsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
            return o.savedAyahCount == n.savedAyahCount && o.inDb == n.inDb
                && o.dbIsMeccan == n.dbIsMeccan && o.ayahsInSurah == n.ayahsInSurah;
        }
    };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) { h.bind(getItem(pos), listener); }

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
            tvNumber.setText(String.valueOf(row.info.number));
            tvName.setText(row.info.name);

            if (row.inDb && row.savedAyahCount > 0) {
                // سورة مضافة ولها آيات
                String type   = row.dbIsMeccan ? "مكية" : "مدنية";
                String juzStr = row.minJuz > 0 ? " · ج" + row.minJuz : "";
                String total  = row.ayahsInSurah > 0 ? "/" + row.ayahsInSurah : "";
                tvMeta.setText(type + " · " + row.savedAyahCount + total + " آية" + juzStr);
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("محفوظة");
                card.setStrokeColor(itemView.getContext().getColor(R.color.gold_dim));
                card.setAlpha(1.0f);
            } else if (row.inDb) {
                // سورة مضافة بدون آيات بعد
                String type = row.dbIsMeccan ? "مكية" : "مدنية";
                String total = row.ayahsInSurah > 0 ? row.ayahsInSurah + " آية" : "";
                tvMeta.setText(type + (total.isEmpty() ? "" : " · " + total));
                tvBadge.setVisibility(View.VISIBLE);
                tvBadge.setText("مضافة");
                card.setStrokeColor(itemView.getContext().getColor(R.color.gold_dim));
                card.setAlpha(0.9f);
            } else {
                // غير مضافة
                tvMeta.setText(row.info.getRevelationType() + " · " + row.info.totalAyahs + " آية");
                tvBadge.setVisibility(View.GONE);
                card.setStrokeColor(itemView.getContext().getColor(R.color.divider_color));
                card.setAlpha(0.6f);
            }
            card.setOnClickListener(v -> listener.onSurahClick(row.info));
        }
    }

    public static class SurahRow {
        public final SurahInfo info;
        public final int  savedAyahCount;
        public final int  minJuz;
        public final boolean dbIsMeccan;
        public final int  ayahsInSurah;
        public final boolean inDb;

        public SurahRow(SurahInfo info, int savedAyahCount, int minJuz,
                        boolean dbIsMeccan, int ayahsInSurah, boolean inDb) {
            this.info           = info;
            this.savedAyahCount = savedAyahCount;
            this.minJuz         = minJuz;
            this.dbIsMeccan     = dbIsMeccan;
            this.ayahsInSurah   = ayahsInSurah;
            this.inDb           = inDb;
        }
    }
}
