package com.cloudy.quranbuilder.ui.browse;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.google.android.material.card.MaterialCardView;

public class SurahAdapter extends ListAdapter<SurahAdapter.SurahRow, SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener {
        void onSurahClick(SurahInfo surahInfo);
    }

    private final OnSurahClickListener listener;

    public SurahAdapter(OnSurahClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SurahRow> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SurahRow>() {
            @Override public boolean areItemsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
                return o.info.number == n.info.number;
            }
            @Override public boolean areContentsTheSame(@NonNull SurahRow o, @NonNull SurahRow n) {
                return o.hasData == n.hasData;
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        h.bind(getItem(position), listener);
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
            tvNumber.setText(String.valueOf(row.info.number));
            tvName.setText(row.info.name);
            tvMeta.setText(row.info.getRevelationType() + " · " + row.info.totalAyahs + " آية");

            if (row.hasData) {
                tvBadge.setVisibility(View.VISIBLE);
                // إطار ذهبي للسور المحفوظة
                card.setStrokeColor(itemView.getContext().getColor(R.color.gold_dim));
                card.setAlpha(1.0f);
            } else {
                tvBadge.setVisibility(View.GONE);
                card.setStrokeColor(itemView.getContext().getColor(R.color.divider_color));
                card.setAlpha(0.8f);
            }

            card.setOnClickListener(v -> listener.onSurahClick(row.info));
        }
    }

    public static class SurahRow {
        public final SurahInfo info;
        public final boolean hasData;
        public SurahRow(SurahInfo info, boolean hasData) {
            this.info = info; this.hasData = hasData;
        }
    }
}
