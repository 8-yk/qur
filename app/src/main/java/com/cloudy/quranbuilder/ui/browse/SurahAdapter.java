package com.cloudy.quranbuilder.ui.browse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.Objects;

public class SurahAdapter extends ListAdapter<SurahAdapter.SurahRow, SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener {
        void onSurahClick(SurahInfo surahInfo);
    }

    private final OnSurahClickListener listener;

    public SurahAdapter(OnSurahClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SurahRow> DIFF_CALLBACK = new DiffUtil.ItemCallback<SurahRow>() {
        @Override
        public boolean areItemsTheSame(@NonNull SurahRow oldItem, @NonNull SurahRow newItem) {
            return oldItem.info.number == newItem.info.number;
        }
        @Override
        public boolean areContentsTheSame(@NonNull SurahRow oldItem, @NonNull SurahRow newItem) {
            return oldItem.hasData == newItem.hasData;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SurahRow row = getItem(position);
        holder.bind(row, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvNumber;
        final TextView tvName;
        final TextView tvMeta;
        final TextView tvBadge;
        final View dot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card     = itemView.findViewById(R.id.cardSurah);
            tvNumber = itemView.findViewById(R.id.tvSurahNumber);
            tvName   = itemView.findViewById(R.id.tvSurahName);
            tvMeta   = itemView.findViewById(R.id.tvSurahMeta);
            tvBadge  = itemView.findViewById(R.id.tvBadge);
            dot      = itemView.findViewById(R.id.viewDot);
        }

        void bind(SurahRow row, OnSurahClickListener listener) {
            Context ctx = itemView.getContext();
            tvNumber.setText(String.valueOf(row.info.number));
            tvName.setText(row.info.name);
            tvMeta.setText(row.info.getRevelationType() + " · " + row.info.totalAyahs + " آية");

            if (row.hasData) {
                dot.setVisibility(View.VISIBLE);
                tvBadge.setVisibility(View.VISIBLE);
                card.setAlpha(1.0f);
            } else {
                dot.setVisibility(View.INVISIBLE);
                tvBadge.setVisibility(View.GONE);
                card.setAlpha(0.85f);
            }

            card.setOnClickListener(v -> listener.onSurahClick(row.info));
        }
    }

    public static class SurahRow {
        public final SurahInfo info;
        public final boolean hasData;

        public SurahRow(SurahInfo info, boolean hasData) {
            this.info = info;
            this.hasData = hasData;
        }
    }
}
