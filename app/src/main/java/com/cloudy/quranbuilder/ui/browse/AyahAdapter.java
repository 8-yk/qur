package com.cloudy.quranbuilder.ui.browse;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AyahEntity;

public class AyahAdapter extends ListAdapter<AyahEntity, AyahAdapter.ViewHolder> {

    public AyahAdapter() { super(DIFF); }

    private static final DiffUtil.ItemCallback<AyahEntity> DIFF =
        new DiffUtil.ItemCallback<AyahEntity>() {
            @Override public boolean areItemsTheSame(@NonNull AyahEntity o, @NonNull AyahEntity n) {
                return o.id == n.id;
            }
            @Override public boolean areContentsTheSame(@NonNull AyahEntity o, @NonNull AyahEntity n) {
                return o.numberInSurah == n.numberInSurah && o.text.equals(n.text)
                    && o.juz == n.juz && o.page == n.page;
            }
        };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ayah, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AyahEntity a = getItem(position);
        h.tvNumber.setText(toAr(a.numberInSurah));
        h.tvText.setText(a.text);

        // معلومات الجزء / الصفحة
        StringBuilder meta = new StringBuilder();
        if (a.juz > 0) meta.append("ج").append(a.juz);
        if (a.hizb > 0) {
            if (meta.length() > 0) meta.append("  ·  ");
            meta.append("ح").append(a.hizb);
            if (a.hizbQuarter > 0) meta.append("/").append(a.hizbQuarter);
        }
        if (a.page > 0) {
            if (meta.length() > 0) meta.append("  ·  ");
            meta.append("ص").append(a.page);
        }
        if (meta.length() > 0) {
            h.tvMeta.setText(meta.toString());
            h.tvMeta.setVisibility(View.VISIBLE);
        } else {
            h.tvMeta.setVisibility(View.GONE);
        }
    }

    private static String toAr(int n) {
        final char[] d = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(n).toCharArray()) sb.append(d[c - '0']);
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNumber, tvText, tvMeta;
        ViewHolder(@NonNull View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tvAyahNumber);
            tvText   = v.findViewById(R.id.tvAyahText);
            tvMeta   = v.findViewById(R.id.tvAyahMeta);
        }
    }
}
