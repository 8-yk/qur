package com.cloudy.quranbuilder.ui.mushaf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MushafPagerAdapter extends RecyclerView.Adapter<MushafPagerAdapter.PageHolder> {

    static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    // ── أسماء الأجزاء ────────────────────────────────────────
    static final String[] JUZ_NAMES = {
            "الجزء الأوَّل",           "الجزء الثاني",
            "الجزء الثالث",            "الجزء الرابع",
            "الجزء الخامس",            "الجزء السادس",
            "الجزء السابع",            "الجزء الثامن",
            "الجزء التاسع",            "الجزء العاشر",
            "الجزء الحادي عشر",        "الجزء الثاني عشر",
            "الجزء الثالث عشر",        "الجزء الرابع عشر",
            "الجزء الخامس عشر",        "الجزء السادس عشر",
            "الجزء السابع عشر",        "الجزء الثامن عشر",
            "الجزء التاسع عشر",        "الجزء العشرون",
            "الجزء الحادي والعشرون",   "الجزء الثاني والعشرون",
            "الجزء الثالث والعشرون",   "الجزء الرابع والعشرون",
            "الجزء الخامس والعشرون",   "الجزء السادس والعشرون",
            "الجزء السابع والعشرون",   "الجزء الثامن والعشرون",
            "الجزء التاسع والعشرون",   "الجزء الثلاثون"
    };

    static String juzName(int juz) {
        return (juz >= 1 && juz <= 30) ? JUZ_NAMES[juz - 1]
                : (juz > 0 ? "الجزء " + juz : "");
    }

    // ── PageMeta: بيانات شريط العنوان لكل صفحة ───────────────
    public static class PageMeta {
        public final int          pageNum;
        public final int          juz;
        public final List<String> surahNames;

        public PageMeta(int pageNum, int juz, List<String> surahNames) {
            this.pageNum    = pageNum;
            this.juz        = juz;
            this.surahNames = surahNames;
        }

        /** النص الذي يظهر في أعلى يسار الشاشة */
        public String getSurahNamesLabel() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < surahNames.size(); i++) {
                if (i > 0) sb.append("  ·  ");
                sb.append(surahNames.get(i));
            }
            return sb.toString();
        }
    }

    // ── نوع العرض ────────────────────────────────────────────
    public enum Mode { PAGE_BASED, SURAH_BASED }

    private final Context  context;
    private final Handler  mainHandler = new Handler(Looper.getMainLooper());
    private       Mode     mode        = Mode.PAGE_BASED;
    private       List<Integer>   pages  = new ArrayList<>();
    private       List<SurahInfo> surahs = new ArrayList<>();
    private final Map<Integer, PageMeta> metaCache = new HashMap<>();
    private       Typeface quranTypeface;

    public MushafPagerAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
        loadFont();
    }

    private void loadFont() {
        try { quranTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/quran.ttf"); }
        catch (Exception ignored) { quranTypeface = null; }
    }

    // ── تحديث البيانات ──────────────────────────────────────

    public void setPageMode(List<Integer> pageList, Map<Integer, PageMeta> meta) {
        mode = Mode.PAGE_BASED;
        pages = new ArrayList<>(pageList);
        metaCache.clear();
        metaCache.putAll(meta);
        notifyDataSetChanged();
    }

    public void setSurahMode(List<SurahInfo> surahList) {
        mode = Mode.SURAH_BASED;
        surahs = new ArrayList<>(surahList);
        notifyDataSetChanged();
    }

    /** بيانات العنوان للموضع — تُستخدم في onPageSelected */
    @Nullable
    public PageMeta getMetaAt(int position) {
        if (mode == Mode.PAGE_BASED && position >= 0 && position < pages.size()) {
            return metaCache.get(pages.get(position));
        }
        if (mode == Mode.SURAH_BASED && position >= 0 && position < surahs.size()) {
            SurahInfo info = surahs.get(position);
            return new PageMeta(0, 0, Collections.singletonList(info.name));
        }
        return null;
    }

    public int getPositionForSurah(int surahNum) {
        if (mode == Mode.SURAH_BASED) {
            for (int i = 0; i < surahs.size(); i++)
                if (surahs.get(i).number == surahNum) return i;
        }
        return -1;
    }

    @Override public int  getItemCount() {
        return mode == Mode.PAGE_BASED ? pages.size() : surahs.size();
    }
    @Override public long getItemId(int pos) {
        return mode == Mode.PAGE_BASED ? pages.get(pos) : surahs.get(pos).number;
    }

    @NonNull @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_mushaf_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int pos) {
        if (mode == Mode.PAGE_BASED) h.bindPage(pages.get(pos));
        else                          h.bindSurah(surahs.get(pos));
    }

    // ── دائرة رقم الآية (مزدوجة الإطار مثل المصحف) ──────────
    static class AyahNumSpan extends ReplacementSpan {
        private static final int GOLD = Color.parseColor("#C9A84C");

        @Override
        public int getSize(@NonNull Paint paint, CharSequence t, int s, int e,
                           @Nullable Paint.FontMetricsInt fm) {
            return (int) (paint.getTextSize() * 0.6f * 2.6f);
        }

        @Override
        public void draw(@NonNull Canvas cv, CharSequence t, int s, int e,
                         float x, int top, int y, int bottom, @NonNull Paint paint) {
            float numSz  = paint.getTextSize() * 0.58f;
            float diam   = numSz * 2.4f;
            float cx     = x + diam / 2f;
            float cy     = (top + bottom) / 2f;
            float r      = diam / 2f;

            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(GOLD);

            // الدائرة الخارجية
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.4f);
            cv.drawCircle(cx, cy, r, p);

            // الدائرة الداخلية (الإطار المزدوج)
            p.setStrokeWidth(0.6f);
            cv.drawCircle(cx, cy, r * 0.78f, p);

            // النص
            p.setStyle(Paint.Style.FILL);
            p.setTextSize(numSz);
            p.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = p.getFontMetrics();
            cv.drawText(t, s, e, cx, cy - (fm.ascent + fm.descent) / 2f, p);
        }
    }

    // ── ViewHolder ───────────────────────────────────────────
    class PageHolder extends RecyclerView.ViewHolder {

        final TextView     tvPageSurahName;
        final TextView     tvPageJuzName;
        final View         surahHeaderContainer;
        final TextView     tvMushafSurahHeader;
        final TextView     tvBismillah;
        final View         bismillahDivider;
        final ScrollView   svMushafContent;
        final TextView     tvMushafContent;
        final LinearLayout emptyContainer;
        final TextView     tvPageNumBottom;
        final ProgressBar  progressBar;

        PageHolder(@NonNull View v) {
            super(v);
            tvPageSurahName     = v.findViewById(R.id.tvPageSurahName);
            tvPageJuzName       = v.findViewById(R.id.tvPageJuzName);
            surahHeaderContainer= v.findViewById(R.id.surahHeaderContainer);
            tvMushafSurahHeader = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah         = v.findViewById(R.id.tvBismillah);
            bismillahDivider    = v.findViewById(R.id.bismillahDivider);
            svMushafContent     = v.findViewById(R.id.svMushafContent);
            tvMushafContent     = v.findViewById(R.id.tvMushafContent);
            emptyContainer      = v.findViewById(R.id.emptyContainer);
            tvPageNumBottom     = v.findViewById(R.id.tvPageNumBottom);
            progressBar         = v.findViewById(R.id.mushafProgress);

            if (quranTypeface != null) {
                tvBismillah.setTypeface(quranTypeface);
                tvMushafContent.setTypeface(quranTypeface);
                tvMushafSurahHeader.setTypeface(quranTypeface);
            }
        }

        void bindPage(int pageNum) {
            final String tag = "p:" + pageNum;
            itemView.setTag(tag);
            resetUI();
            tvPageNumBottom.setText(toAr(pageNum));

            EXECUTOR.execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForPage(pageNum);

                mainHandler.post(() -> {
                    if (!tag.equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);
                    if (ayahs.isEmpty()) { showEmpty(); return; }
                    renderMultiSurahPage(ayahs, pageNum);
                });
            });
        }

        void bindSurah(SurahInfo info) {
            final String tag = "s:" + info.number;
            itemView.setTag(tag);
            resetUI();
            tvMushafSurahHeader.setText("سُورَةُ " + info.name);
            tvPageNumBottom.setText(toAr(info.number));

            EXECUTOR.execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!tag.equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);
                    if (ayahs.isEmpty()) { showEmpty(); return; }

                    if (!ayahs.isEmpty()) {
                        AyahEntity first = ayahs.get(0);
                        if (first.juz  > 0) tvPageJuzName.setText(juzName(first.juz));
                        if (first.page > 0) tvPageNumBottom.setText(toAr(first.page));
                    }
                    if (info.number != 9 && info.number != 1) showBismillah();
                    tvMushafContent.setText(buildText(ayahs));
                    showContent();
                });
            });
        }

        /**
         * يرسم صفحة كاملة قد تحتوي أكثر من سورة.
         * - إذا بدأت الصفحة بأول آية لسورة: يُظهر رأس السورة المركزي + البسملة
         * - إذا انتقلنا لسورة جديدة وسط الصفحة: يُضيف رأساً داخلياً في النص
         */
        private void renderMultiSurahPage(List<AyahEntity> ayahs, int pageNum) {
            AyahEntity first = ayahs.get(0);

            // الجزء من أول آية
            if (first.juz > 0) tvPageJuzName.setText(juzName(first.juz));

            // هل الصفحة تبدأ بأول آية في سورة؟
            boolean pageStartsNewSurah = (first.numberInSurah == 1);

            if (pageStartsNewSurah) {
                SurahInfo fi = SurahInfo.getByNumber(first.surahNumber);
                tvMushafSurahHeader.setText("سُورَةُ " + (fi != null ? fi.name : ""));
                surahHeaderContainer.setVisibility(View.VISIBLE);
                if (fi != null && fi.number != 9 && fi.number != 1) showBismillah();
            } else {
                surahHeaderContainer.setVisibility(View.GONE);
            }

            tvMushafContent.setText(buildPageText(ayahs, pageStartsNewSurah));
            showContent();
        }

        /** بناء النص المصحفي لصفحة — يتعامل مع تعدد السور */
        private CharSequence buildPageText(List<AyahEntity> ayahs, boolean skipFirstHeader) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            int currentSurah = -1;
            boolean isFirst  = true;

            for (AyahEntity ayah : ayahs) {
                boolean surahChanged = (ayah.surahNumber != currentSurah);

                if (surahChanged) {
                    // رأس سورة جديدة داخل نص الصفحة
                    boolean insertHeader = !(isFirst && skipFirstHeader);
                    if (insertHeader) {
                        // فراغ قبل الرأس
                        if (sb.length() > 0) sb.append("\n\n");

                        SurahInfo ni = SurahInfo.getByNumber(ayah.surahNumber);
                        String hdr = "— سُورَةُ " + (ni != null ? ni.name : "") + " —";
                        int hStart = sb.length();
                        sb.append(hdr);
                        sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                                hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(new RelativeSizeSpan(0.72f),
                                hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // بسملة السورة الجديدة (إن كانت أول آية)
                        if (ayah.numberInSurah == 1 && ni != null
                                && ni.number != 9 && ni.number != 1) {
                            sb.append("\n");
                            String bism = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ";
                            int bStart = sb.length();
                            sb.append(bism);
                            sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                                    bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.setSpan(new RelativeSizeSpan(0.82f),
                                    bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        sb.append("\n\n");
                    }
                    currentSurah = ayah.surahNumber;
                    isFirst      = false;
                }

                // نص الآية
                sb.append(ayah.text);
                sb.append(" ");

                // رقم الآية في دائرة
                String num = toAr(ayah.numberInSurah);
                int nStart = sb.length();
                sb.append("  " + num + "  ");
                // الـ Span يغطي الرقم فقط (بين المسافتين)
                sb.setSpan(new AyahNumSpan(),
                        nStart + 2, nStart + 2 + num.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.append(" ");
            }
            return sb;
        }

        /** النص لوضع سورة واحدة */
        private CharSequence buildText(List<AyahEntity> ayahs) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (AyahEntity a : ayahs) {
                sb.append(a.text).append(" ");
                String num = toAr(a.numberInSurah);
                int nStart = sb.length();
                sb.append("  " + num + "  ");
                sb.setSpan(new AyahNumSpan(),
                        nStart + 2, nStart + 2 + num.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.append(" ");
            }
            return sb;
        }

        // ── Helpers ──────────────────────────────────────────
        private void resetUI() {
            progressBar.setVisibility(View.VISIBLE);
            svMushafContent.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            bismillahDivider.setVisibility(View.GONE);
            surahHeaderContainer.setVisibility(View.VISIBLE);
            tvMushafSurahHeader.setText("");
            tvPageJuzName.setText("");
            tvPageSurahName.setText("");
        }
        private void showBismillah() {
            tvBismillah.setVisibility(View.VISIBLE);
            bismillahDivider.setVisibility(View.VISIBLE);
        }
        private void showContent() {
            svMushafContent.setVisibility(View.VISIBLE);
            svMushafContent.scrollTo(0, 0);
        }
        private void showEmpty() {
            emptyContainer.setVisibility(View.VISIBLE);
        }

        private String toAr(int n) {
            final String[] d = {"٠","١","٢","٣","٤","٥","٦","٧","٨","٩"};
            StringBuilder sb = new StringBuilder();
            for (char c : String.valueOf(n).toCharArray()) sb.append(d[c - '0']);
            return sb.toString();
        }
    }
}
