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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MushafPagerAdapter extends RecyclerView.Adapter<MushafPagerAdapter.PageHolder> {

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<SurahInfo> surahList;
    private Typeface quranTypeface;

    public MushafPagerAdapter(Context context, List<SurahInfo> surahList) {
        this.context   = context;
        this.surahList = new ArrayList<>(surahList);
        setHasStableIds(true);
        loadQuranFont();
    }

    private void loadQuranFont() {
        try {
            quranTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/quran.ttf");
        } catch (Exception ignored) {
            quranTypeface = null;
        }
    }

    /** تحديث القائمة من الخارج */
    public void updateList(List<SurahInfo> newList) {
        this.surahList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    /** إرجاع موضع السورة في القائمة، أو -1 إن لم توجد */
    public int getPositionForSurah(int surahNumber) {
        for (int i = 0; i < surahList.size(); i++) {
            if (surahList.get(i).number == surahNumber) return i;
        }
        return -1;
    }

    @Override public long getItemId(int position) { return surahList.get(position).number; }
    @Override public int  getItemCount()           { return surahList.size(); }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mushaf_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.bind(surahList.get(position));
    }

    // ─────────────────────────────────────────────────────────
    class PageHolder extends RecyclerView.ViewHolder {
        final TextView   tvSurahCorner;
        final TextView   tvJuzDisplay;
        final TextView   tvMushafSurahHeader;
        final TextView   tvBismillah;
        final View       bismillahDivider;
        final ScrollView svMushafContent;
        final TextView   tvMushafContent;
        final TextView   tvMushafEmpty;
        final TextView   tvPageNumBottom;
        final ProgressBar progressBar;

        PageHolder(@NonNull View v) {
            super(v);
            tvSurahCorner      = v.findViewById(R.id.tvSurahCorner);
            tvJuzDisplay       = v.findViewById(R.id.tvJuzDisplay);
            tvMushafSurahHeader = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah        = v.findViewById(R.id.tvBismillah);
            bismillahDivider   = v.findViewById(R.id.bismillahDivider);
            svMushafContent    = v.findViewById(R.id.svMushafContent);
            tvMushafContent    = v.findViewById(R.id.tvMushafContent);
            tvMushafEmpty      = v.findViewById(R.id.tvMushafEmpty);
            tvPageNumBottom    = v.findViewById(R.id.tvPageNumBottom);
            progressBar        = v.findViewById(R.id.mushafProgress);

            if (quranTypeface != null) {
                tvBismillah.setTypeface(quranTypeface);
                tvMushafContent.setTypeface(quranTypeface);
                tvMushafSurahHeader.setTypeface(quranTypeface);
            }
        }

        void bind(SurahInfo info) {
            itemView.setTag(info.number);

            // ما هو متاح فوراً
            tvMushafSurahHeader.setText("سورة " + info.name);
            tvSurahCorner.setText(info.name);
            tvPageNumBottom.setText(toArabicNum(info.number));

            // إعادة ضبط الحقول المؤجلة
            tvJuzDisplay.setVisibility(View.INVISIBLE);

            // إخفاء المحتوى + إظهار loading
            svMushafContent.setVisibility(View.GONE);
            tvMushafEmpty.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            bismillahDivider.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            final int expectedSurah = info.number;

            Executors.newSingleThreadExecutor().execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!Integer.valueOf(expectedSurah).equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);

                    // رقم الجزء من أول آية
                    if (!ayahs.isEmpty() && ayahs.get(0).juz > 0) {
                        tvJuzDisplay.setText("الجزء " + toArabicNum(ayahs.get(0).juz));
                        tvJuzDisplay.setVisibility(View.VISIBLE);
                    }

                    // رقم الصفحة الفعلي (إن وُجد)
                    if (!ayahs.isEmpty() && ayahs.get(0).page > 0) {
                        tvPageNumBottom.setText(toArabicNum(ayahs.get(0).page));
                    }

                    if (ayahs.isEmpty()) {
                        tvMushafEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // البسملة: لكل سورة إلا التوبة (9) والفاتحة (1 بسملتها آية)
                        if (info.number != 9 && info.number != 1) {
                            tvBismillah.setVisibility(View.VISIBLE);
                            bismillahDivider.setVisibility(View.VISIBLE);
                        }
                        tvMushafContent.setText(buildMushafText(ayahs));
                        svMushafContent.setVisibility(View.VISIBLE);
                        // ابدأ من الأعلى دائماً
                        svMushafContent.scrollTo(0, 0);
                    }
                });
            });
        }

        /**
         * نص مصحفي متصل — الآيات تتدفق بدون سطر جديد بينها
         * رقم الآية ﴿١﴾ بلون ذهبي وحجم أصغر
         */
        private CharSequence buildMushafText(List<AyahEntity> ayahs) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (AyahEntity ayah : ayahs) {
                sb.append(ayah.text);
                sb.append(" ");

                String numStr = "﴿" + toArabicNum(ayah.numberInSurah) + "﴾ ";
                int start = sb.length();
                sb.append(numStr);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.62f),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
