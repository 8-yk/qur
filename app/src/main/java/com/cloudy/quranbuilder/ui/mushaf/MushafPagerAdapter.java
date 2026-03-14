package com.cloudy.quranbuilder.ui.mushaf;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.List;
import java.util.concurrent.Executors;

public class MushafPagerAdapter extends RecyclerView.Adapter<MushafPagerAdapter.PageHolder> {

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MushafPagerAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return SurahInfo.ALL_SURAHS[position].number;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mushaf_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.bind(SurahInfo.ALL_SURAHS[position]);
    }

    @Override
    public int getItemCount() { return 114; }

    class PageHolder extends RecyclerView.ViewHolder {
        final TextView tvSurahHeader;
        final TextView tvBismillah;
        final TextView tvMushafContent;
        final TextView tvEmpty;
        final ProgressBar progressBar;

        PageHolder(@NonNull View v) {
            super(v);
            tvSurahHeader   = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah     = v.findViewById(R.id.tvBismillah);
            tvMushafContent = v.findViewById(R.id.tvMushafContent);
            tvEmpty         = v.findViewById(R.id.tvMushafEmpty);
            progressBar     = v.findViewById(R.id.mushafProgress);
        }

        void bind(SurahInfo info) {
            // ضع tag لمنع stale callbacks بعد recycling
            itemView.setTag(info.number);

            tvSurahHeader.setText("سورة " + info.name);
            tvMushafContent.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            int expectedSurah = info.number;

            Executors.newSingleThreadExecutor().execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    // تحقق أن الـ ViewHolder لم يُعاد استخدامه
                    if (!Integer.valueOf(expectedSurah).equals(itemView.getTag())) return;

                    progressBar.setVisibility(View.GONE);

                    if (ayahs.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // البسملة (إلا التوبة والفاتحة — الفاتحة تحتوي عليها كآية)
                        if (info.number != 9 && info.number != 1) {
                            tvBismillah.setVisibility(View.VISIBLE);
                        }
                        tvMushafContent.setVisibility(View.VISIBLE);
                        tvMushafContent.setText(buildMushafText(ayahs));
                    }
                });
            });
        }

        private CharSequence buildMushafText(List<AyahEntity> ayahs) {
            SpannableStringBuilder sb = new SpannableStringBuilder();

            for (AyahEntity ayah : ayahs) {
                // نص الآية
                sb.append(ayah.text);
                sb.append(" ");

                // رقم الآية بلون ذهبي داخل علامات ﴿﴾
                String numStr = "﴿" + toArabicNum(ayah.numberInSurah) + "﴾ ";
                int start = sb.length();
                sb.append(numStr);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.8f),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return sb;
        }

        private String toArabicNum(int n) {
            final String[] d = {"٠","١","٢","٣","٤","٥","٦","٧","٨","٩"};
            StringBuilder sb = new StringBuilder();
            for (char c : String.valueOf(n).toCharArray()) {
                sb.append(d[c - '0']);
            }
            return sb.toString();
        }
    }
}
