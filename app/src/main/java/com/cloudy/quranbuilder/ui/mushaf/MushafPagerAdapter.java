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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

    // ── أسماء الأجزاء كاملة ──────────────────────────────────
    private static final String[] JUZ_NAMES = {
            "الجزء الأوَّل",        "الجزء الثاني",          "الجزء الثالث",
            "الجزء الرابع",         "الجزء الخامس",          "الجزء السادس",
            "الجزء السابع",         "الجزء الثامن",          "الجزء التاسع",
            "الجزء العاشر",         "الجزء الحادي عشر",      "الجزء الثاني عشر",
            "الجزء الثالث عشر",     "الجزء الرابع عشر",      "الجزء الخامس عشر",
            "الجزء السادس عشر",     "الجزء السابع عشر",      "الجزء الثامن عشر",
            "الجزء التاسع عشر",     "الجزء العشرون",         "الجزء الحادي والعشرون",
            "الجزء الثاني والعشرون","الجزء الثالث والعشرون", "الجزء الرابع والعشرون",
            "الجزء الخامس والعشرون","الجزء السادس والعشرون", "الجزء السابع والعشرون",
            "الجزء الثامن والعشرون","الجزء التاسع والعشرون", "الجزء الثلاثون"
    };

    private static String juzName(int juz) {
        if (juz >= 1 && juz <= 30) return JUZ_NAMES[juz - 1];
        return juz > 0 ? "الجزء " + juz : "";
    }

    // ─────────────────────────────────────────────────────────
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
        } catch (Exception ignored) { quranTypeface = null; }
    }

    public void updateList(List<SurahInfo> newList) {
        this.surahList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public int getPositionForSurah(int surahNumber) {
        for (int i = 0; i < surahList.size(); i++)
            if (surahList.get(i).number == surahNumber) return i;
        return -1;
    }

    @Override public long getItemId(int pos) { return surahList.get(pos).number; }
    @Override public int  getItemCount()      { return surahList.size(); }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mushaf_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int pos) {
        h.bind(surahList.get(pos));
    }

    // ─────────────────────────────────────────────────────────
    class PageHolder extends RecyclerView.ViewHolder {

        final TextView      tvPageSurahName;
        final TextView      tvPageJuzName;
        final FrameLayout   surahHeaderContainer;
        final TextView      tvMushafSurahHeader;
        final TextView      tvBismillah;
        final View          bismillahDivider;
        final ScrollView    svMushafContent;
        final TextView      tvMushafContent;
        final LinearLayout  emptyContainer;
        final TextView      tvPageNumBottom;
        final ProgressBar   progressBar;

        PageHolder(@NonNull View v) {
            super(v);
            tvPageSurahName      = v.findViewById(R.id.tvPageSurahName);
            tvPageJuzName        = v.findViewById(R.id.tvPageJuzName);
            surahHeaderContainer = v.findViewById(R.id.surahHeaderContainer);
            tvMushafSurahHeader  = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah          = v.findViewById(R.id.tvBismillah);
            bismillahDivider     = v.findViewById(R.id.bismillahDivider);
            svMushafContent      = v.findViewById(R.id.svMushafContent);
            tvMushafContent      = v.findViewById(R.id.tvMushafContent);
            emptyContainer       = v.findViewById(R.id.emptyContainer);
            tvPageNumBottom      = v.findViewById(R.id.tvPageNumBottom);
            progressBar          = v.findViewById(R.id.mushafProgress);

            if (quranTypeface != null) {
                tvBismillah.setTypeface(quranTypeface);
                tvMushafContent.setTypeface(quranTypeface);
                tvMushafSurahHeader.setTypeface(quranTypeface);
            }
        }

        void bind(SurahInfo info) {
            itemView.setTag(info.number);

            // ما يعرف فوراً
            tvPageSurahName.setText(info.name);
            tvMushafSurahHeader.setText("سُورَةُ " + info.name);

            // reset
            tvPageJuzName.setText("");
            tvPageNumBottom.setText(toAr(info.number)); // fallback

            // إخفاء المحتوى
            svMushafContent.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            bismillahDivider.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            final int expected = info.number;

            Executors.newSingleThreadExecutor().execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!Integer.valueOf(expected).equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);

                    // جزء وصفحة من البيانات الفعلية
                    if (!ayahs.isEmpty()) {
                        AyahEntity first = ayahs.get(0);
                        if (first.juz > 0)
                            tvPageJuzName.setText(juzName(first.juz));
                        if (first.page > 0)
                            tvPageNumBottom.setText(toAr(first.page));
                    }

                    if (ayahs.isEmpty()) {
                        emptyContainer.setVisibility(View.VISIBLE);
                    } else {
                        // البسملة: لكل سورة إلا التوبة (9) والفاتحة (1)
                        if (info.number != 9 && info.number != 1) {
                            tvBismillah.setText(buildBismillah());
                            tvBismillah.setVisibility(View.VISIBLE);
                            bismillahDivider.setVisibility(View.VISIBLE);
                        }
                        tvMushafContent.setText(buildMushafText(ayahs));
                        svMushafContent.setVisibility(View.VISIBLE);
                        svMushafContent.scrollTo(0, 0);
                    }
                });
            });
        }

        /** البسملة بالطريقة المصحفية مع شرطات تمديد */
        private String buildBismillah() {
            return "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ";
        }

        /**
         * نص مصحفي متصل — رقم الآية ﴿١﴾ ذهبي وصغير مدمج في التدفق
         */
        private CharSequence buildMushafText(List<AyahEntity> ayahs) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (AyahEntity ayah : ayahs) {
                sb.append(ayah.text);
                sb.append("\u00A0"); // مسافة غير قاطعة

                // رقم الآية
                String num = " ﴿" + toAr(ayah.numberInSurah) + "﴾ ";
                int start = sb.length();
                sb.append(num);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.6f),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return sb;
        }

        // ── تحويل الأرقام للعربية ──
        private String toAr(int n) {
            final String[] d = {"٠","١","٢","٣","٤","٥","٦","٧","٨","٩"};
            StringBuilder sb = new StringBuilder();
            for (char c : String.valueOf(n).toCharArray()) sb.append(d[c - '0']);
            return sb.toString();
        }
    }
}
