package com.cloudy.quranbuilder.ui.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AyahEntity;

public class AyahAdapter extends ListAdapter<AyahEntity, AyahAdapter.ViewHolder> {

    public AyahAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<AyahEntity> DIFF = new DiffUtil.ItemCallback<AyahEntity>() {
        @Override public boolean areItemsTheSame(@NonNull AyahEntity o, @NonNull AyahEntity n) {
            return o.id == n.id;
        }
        @Override public boolean areContentsTheSame(@NonNull AyahEntity o, @NonNull AyahEntity n) {
            return o.text.equals(n.text) && o.numberInSurah == n.numberInSurah;
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ayah, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AyahEntity ayah = getItem(position);
        holder.tvNumber.setText(toArabicNumber(ayah.numberInSurah));
        holder.tvText.setText(ayah.text);
    }

    // تحويل الرقم إلى الشكل العربي في دائرة
    private String toArabicNumber(int n) {
        String[] arabicDigits = {"٠","١","٢","٣","٤","٥","٦","٧","٨","٩"};
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(n).toCharArray()) {
            sb.append(arabicDigits[c - '0']);
        }
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNumber;
        final TextView tvText;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tvAyahNumber);
            tvText   = v.findViewById(R.id.tvAyahText);
        }
    }
}
