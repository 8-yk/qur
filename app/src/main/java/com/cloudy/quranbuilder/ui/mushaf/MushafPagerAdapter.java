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

    /**
     * علامة نهاية الآية في يونيكود (U+06DD).
     * خط القرآن يرسمها دائرة ذهبية تحتوي الرقم تلقائياً —
     * لا حاجة لأي Span مخصص.
     */
    private static final char END_OF_AYAH = '\u06DD';

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

    // ── PageMeta ─────────────────────────────────────────────
    public static class PageMeta {
        public final int          pageNum;
        public final int          juz;
        public final List<String> surahNames;

        public PageMeta(int pageNum, int juz, List<String> surahNames) {
            this.pageNum    = pageNum;
            this.juz        = juz;
            this.surahNames = surahNames;
        }

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

    @Nullable
    public PageMeta getMetaAt(int position) {
        if (mode == Mode.PAGE_BASED && position >= 0 && position < pages.size())
            return metaCache.get(pages.get(position));
        if (mode == Mode.SURAH_BASED && position >= 0 && position < surahs.size()) {
            SurahInfo info = surahs.get(position);
            return new PageMeta(0, 0, Collections.singletonList(info.name));
        }
        return null;
    }

    public int getPositionForSurah(int surahNum) {
        if (mode == Mode.SURAH_BASED)
            for (int i = 0; i < surahs.size(); i++)
                if (surahs.get(i).number == surahNum) return i;
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

    // ─────────────────────────────────────────────────────────
    class PageHolder extends RecyclerView.ViewHolder {

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
            surahHeaderContainer = v.findViewById(R.id.surahHeaderContainer);
            tvMushafSurahHeader  = v.findViewById(R.id.tvMushafSurahHeader);
            tvBismillah          = v.findViewById(R.id.tvBismillah);
            bismillahDivider     = v.findViewById(R.id.bismillahDivider);
            svMushafContent      = v.findViewById(R.id.svMushafContent);
            tvMushafContent      = v.findViewById(R.id.tvMushafContent);
            emptyContainer       = v.findViewById(R.id.emptyContainer);
            tvPageNumBottom      = v.findViewById(R.id.tvPageNumBottom);
            progressBar          = v.findViewById(R.id.mushafProgress);

            // تطبيق خط القرآن — هو نفسه يُنشئ دوائر الأرقام
            if (quranTypeface != null) {
                tvBismillah.setTypeface(quranTypeface);
                tvMushafContent.setTypeface(quranTypeface);
                tvMushafSurahHeader.setTypeface(quranTypeface);
            }
        }

        // ── وضع الصفحة الحقيقية ──────────────────────────
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
                    renderPage(ayahs);
                });
            });
        }

        // ── وضع سورة بسورة ───────────────────────────────
        void bindSurah(SurahInfo info) {
            final String tag = "s:" + info.number;
            itemView.setTag(tag);
            resetUI();
            tvMushafSurahHeader.setText("سُورَةُ " + info.name);
            surahHeaderContainer.setVisibility(View.VISIBLE);
            tvPageNumBottom.setText(toAr(info.number));

            EXECUTOR.execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!tag.equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);
                    if (ayahs.isEmpty()) { showEmpty(); return; }

                    if (!ayahs.isEmpty() && ayahs.get(0).page > 0)
                        tvPageNumBottom.setText(toAr(ayahs.get(0).page));

                    if (info.number != 9 && info.number != 1) showBismillah();
                    tvMushafContent.setText(buildAyahText(ayahs, true));
                    showContent();
                });
            });
        }

        // ── بناء صفحة قد تحتوي أكثر من سورة ────────────
        private void renderPage(List<AyahEntity> ayahs) {
            AyahEntity first = ayahs.get(0);
            boolean pageStartsNewSurah = (first.numberInSurah == 1);

            if (pageStartsNewSurah) {
                SurahInfo fi = SurahInfo.getByNumber(first.surahNumber);
                tvMushafSurahHeader.setText("سُورَةُ " + (fi != null ? fi.name : ""));
                surahHeaderContainer.setVisibility(View.VISIBLE);
                if (fi != null && fi.number != 9 && fi.number != 1) showBismillah();
            } else {
                surahHeaderContainer.setVisibility(View.GONE);
            }

            if (!ayahs.isEmpty() && ayahs.get(0).page > 0)
                tvPageNumBottom.setText(toAr(ayahs.get(0).page));

            tvMushafContent.setText(buildPageText(ayahs, pageStartsNewSurah));
            showContent();
        }

        /**
         * نص مصحفي — أرقام الآيات بـ U+06DD + أرقام عربية.
         * خط القرآن يرسمها دائرة ذهبية تلقائياً.
         *
         * المثال: "نص الآية ۝١ نص الآية التالية ۝٢"
         */
        private SpannableStringBuilder buildAyahText(List<AyahEntity> ayahs,
                                                       boolean singleSurah) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (AyahEntity a : ayahs) {
                sb.append(a.text);
                sb.append("\u00A0"); // non-breaking space

                // علامة نهاية الآية + الرقم بالأرقام العربية
                // الخط يُحوّلها لدائرة تلقائياً
                String ayahMark = String.valueOf(END_OF_AYAH) + toAr(a.numberInSurah) + " ";
                int start = sb.length();
                sb.append(ayahMark);

                // اللون الذهبي على رقم الآية
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // حجم أصغر قليلاً
                sb.setSpan(new RelativeSizeSpan(0.85f),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return sb;
        }

        /**
         * نص صفحة متعددة السور — يُضيف رأس السورة عند الانتقال
         */
        private CharSequence buildPageText(List<AyahEntity> ayahs, boolean skipFirst) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            int currentSurah = -1;
            boolean isFirst  = true;

            for (AyahEntity a : ayahs) {
                if (a.surahNumber != currentSurah) {
                    boolean insertHeader = !(isFirst && skipFirst);
                    if (insertHeader) {
                        if (sb.length() > 0) sb.append("\n\n");

                        SurahInfo ni = SurahInfo.getByNumber(a.surahNumber);
                        String hdr = "﴾  سُورَةُ " + (ni != null ? ni.name : "") + "  ﴿";
                        int hs = sb.length();
                        sb.append(hdr);
                        sb.setSpan(new ForegroundColorSpan(Color.parseColor("#EAD080")),
                                hs, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(new RelativeSizeSpan(0.80f),
                                hs, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // بسملة السورة الجديدة
                        if (a.numberInSurah == 1 && ni != null
                                && ni.number != 9 && ni.number != 1) {
                            sb.append("\n");
                            String bism = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ";
                            int bs = sb.length();
                            sb.append(bism);
                            sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                                    bs, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.setSpan(new RelativeSizeSpan(0.82f),
                                    bs, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        sb.append("\n\n");
                    }
                    currentSurah = a.surahNumber;
                    isFirst = false;
                }

                sb.append(a.text).append("\u00A0");
                String mark = String.valueOf(END_OF_AYAH) + toAr(a.numberInSurah) + " ";
                int ms = sb.length();
                sb.append(mark);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        ms, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.85f),
                        ms, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            surahHeaderContainer.setVisibility(View.GONE);
            tvMushafSurahHeader.setText("");
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
