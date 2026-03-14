package com.cloudy.quranbuilder.ui.mushaf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
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
    private Typeface quranTypeface; // خط القرآن الكريم (اختياري)

    public MushafPagerAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
        loadQuranFont();
    }

    /** تحميل الخط من assets/fonts/quran.ttf إن وُجد */
    private void loadQuranFont() {
        try {
            quranTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/quran.ttf");
        } catch (Exception ignored) {
            quranTypeface = null; // سيستخدم خط النظام كـ fallback
        }
    }

    @Override public long getItemId(int position) { return SurahInfo.ALL_SURAHS[position].number; }

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

    @Override public int getItemCount() { return 114; }

    // ─────────────────────────────────────────────────────────
    class PageHolder extends RecyclerView.ViewHolder {
        final TextView tvSurahHeader;
        final TextView tvBismillah;
        final View     bismillahDivider;
        final TextView tvMushafContent;
        final TextView tvMushafEmpty;
        final ProgressBar progressBar;

        PageHolder(@NonNull View v) {
            super(v);
            tvSurahHeader    = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah      = v.findViewById(R.id.tvBismillah);
            bismillahDivider = v.findViewById(R.id.bismillahDivider);
            tvMushafContent  = v.findViewById(R.id.tvMushafContent);
            tvMushafEmpty    = v.findViewById(R.id.tvMushafEmpty);
            progressBar      = v.findViewById(R.id.mushafProgress);

            // تطبيق الخط إن وُجد
            if (quranTypeface != null) {
                tvBismillah.setTypeface(quranTypeface);
                tvMushafContent.setTypeface(quranTypeface);
            }
        }

        void bind(SurahInfo info) {
            itemView.setTag(info.number);
            tvSurahHeader.setText("سورة " + info.name);

            // إخفاء كل شيء + إظهار الـ loading
            tvMushafContent.setVisibility(View.GONE);
            tvMushafEmpty.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            bismillahDivider.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            int expectedSurah = info.number;

            Executors.newSingleThreadExecutor().execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!Integer.valueOf(expectedSurah).equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);

                    if (ayahs.isEmpty()) {
                        tvMushafEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // البسملة: لكل سورة إلا التوبة (9) والفاتحة (1 — بسملتها آية)
                        if (info.number != 9 && info.number != 1) {
                            tvBismillah.setVisibility(View.VISIBLE);
                            bismillahDivider.setVisibility(View.VISIBLE);
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
                sb.append("\u00A0"); // non-breaking space

                // رقم الآية ﴿١﴾ بلون ذهبي وحجم أصغر
                String numStr = " ﴿" + toArabicNum(ayah.numberInSurah) + "﴾ ";
                int start = sb.length();
                sb.append(numStr);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.72f),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                sb.append("\n");
            }
            return sb;
        }

        private String toArabicNum(int n) {
            final String[] d = {"٠","١","٢","٣","٤","٥","٦","٧","٨","٩"};
            StringBuilder sb = new StringBuilder();
            for (char c : String.valueOf(n).toCharArray()) sb.append(d[c - '0']);
            return sb.toString();
        }
    }
}
