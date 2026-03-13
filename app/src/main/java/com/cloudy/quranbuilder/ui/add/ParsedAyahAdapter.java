package com.cloudy.quranbuilder.ui.add;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.model.JsonModels;

public class ParsedAyahAdapter extends ListAdapter<JsonModels.AyahJson, ParsedAyahAdapter.ViewHolder> {

    public ParsedAyahAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<JsonModels.AyahJson> DIFF = new DiffUtil.ItemCallback<JsonModels.AyahJson>() {
        @Override public boolean areItemsTheSame(@NonNull JsonModels.AyahJson o, @NonNull JsonModels.AyahJson n) {
            return o.numberInSurah == n.numberInSurah;
        }
        @Override public boolean areContentsTheSame(@NonNull JsonModels.AyahJson o, @NonNull JsonModels.AyahJson n) {
            return o.text.equals(n.text);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parsed_ayah, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonModels.AyahJson ayah = getItem(position);
        holder.tvNum.setText("﴿" + ayah.numberInSurah + "﴾");
        holder.tvText.setText(ayah.text);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNum;
        final TextView tvText;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNum  = v.findViewById(R.id.tvParsedNum);
            tvText = v.findViewById(R.id.tvParsedText);
        }
    }
}
