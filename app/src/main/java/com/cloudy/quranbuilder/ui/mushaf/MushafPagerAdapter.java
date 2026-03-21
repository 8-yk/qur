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
import androidx.recyclerview.widget.RecyclerView;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.model.SurahInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MushafPagerAdapter extends RecyclerView.Adapter<MushafPagerAdapter.PageHolder> {

    // ── executor مشترك — لا نُنشئ واحداً جديداً في كل bind ──
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    // ── أسماء الأجزاء الثلاثين ───────────────────────────────
    private static final String[] JUZ_NAMES = {
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

    private static String juzName(int juz) {
        return (juz >= 1 && juz <= 30) ? JUZ_NAMES[juz - 1]
                                        : (juz > 0 ? "الجزء " + juz : "");
    }

    // ── نوع القائمة: صفحة (int رقم) أو سورة (SurahInfo) ─────
    public enum Mode { PAGE_BASED, SURAH_BASED }

    private final Context     context;
    private final Handler     mainHandler = new Handler(Looper.getMainLooper());
    private       Mode        mode;
    private       List<Integer>   pages;     // يُستخدم في PAGE_BASED
    private       List<SurahInfo> surahs;    // يُستخدم في SURAH_BASED
    private       Typeface    quranTypeface;

    public MushafPagerAdapter(Context context) {
        this.context = context;
        this.mode    = Mode.PAGE_BASED;
        this.pages   = new ArrayList<>();
        this.surahs  = new ArrayList<>();
        setHasStableIds(true);
        loadFont();
    }

    private void loadFont() {
        try { quranTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/quran.ttf"); }
        catch (Exception ignored) { quranTypeface = null; }
    }

    // ── تحديث القائمة ──────────────────────────────────────
    public void setPageMode(List<Integer> pageList) {
        this.mode   = Mode.PAGE_BASED;
        this.pages  = new ArrayList<>(pageList);
        notifyDataSetChanged();
    }

    public void setSurahMode(List<SurahInfo> surahList) {
        this.mode   = Mode.SURAH_BASED;
        this.surahs = new ArrayList<>(surahList);
        notifyDataSetChanged();
    }

    public Mode getMode() { return mode; }

    /** إرجاع موضع الصفحة التي تحتوي السورة رقم surahNum */
    public int getPositionForSurah(int surahNum) {
        if (mode == Mode.SURAH_BASED) {
            for (int i = 0; i < surahs.size(); i++)
                if (surahs.get(i).number == surahNum) return i;
        }
        // في وضع الصفحة لا نقفز بدقة — نُعيد -1
        return -1;
    }

    @Override public int  getItemCount() {
        return mode == Mode.PAGE_BASED ? pages.size() : surahs.size();
    }

    @Override public long getItemId(int pos) {
        return mode == Mode.PAGE_BASED ? pages.get(pos) : surahs.get(pos).number;
    }

    @NonNull
    @Override
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

        // ── وضع الصفحة الحقيقية ──────────────────────────
        void bindPage(int pageNum) {
            final Object tag = "p:" + pageNum;
            itemView.setTag(tag);
            resetLoading();
            tvPageNumBottom.setText(toAr(pageNum));

            EXECUTOR.execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForPage(pageNum);

                mainHandler.post(() -> {
                    if (!tag.equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);
                    if (ayahs.isEmpty()) { showEmpty(); return; }
                    renderPage(ayahs, /* isPageMode= */ true, pageNum);
                });
            });
        }

        // ── وضع سورة بسورة (fallback) ────────────────────
        void bindSurah(SurahInfo info) {
            final Object tag = "s:" + info.number;
            itemView.setTag(tag);
            resetLoading();
            tvPageSurahName.setText(info.name);
            tvMushafSurahHeader.setText("سُورَةُ " + info.name);
            tvPageNumBottom.setText(toAr(info.number));

            EXECUTOR.execute(() -> {
                List<AyahEntity> ayahs = AppDatabase.getInstance(context)
                        .ayahDao().getAyahsForSurahSync(info.number);

                mainHandler.post(() -> {
                    if (!tag.equals(itemView.getTag())) return;
                    progressBar.setVisibility(View.GONE);
                    if (ayahs.isEmpty()) { showEmpty(); return; }

                    // جزء وصفحة
                    AyahEntity first = ayahs.get(0);
                    if (first.juz  > 0) tvPageJuzName.setText(juzName(first.juz));
                    if (first.page > 0) tvPageNumBottom.setText(toAr(first.page));

                    // بسملة
                    if (info.number != 9 && info.number != 1) showBismillah();

                    // النص
                    tvMushafContent.setText(buildText(ayahs));
                    showContent();
                });
            });
        }

        // ── بناء محتوى صفحة كاملة (عدة سور محتملة) ────────
        private void renderPage(List<AyahEntity> ayahs, boolean isPageMode, int pageNum) {
            // معلومات من أول آية
            AyahEntity firstAyah = ayahs.get(0);
            int firstSurahNum = firstAyah.surahNumber;
            SurahInfo firstInfo = SurahInfo.getByNumber(firstSurahNum);

            // اسم السورة الأولى (يمين) + الجزء (يسار)
            tvPageSurahName.setText(firstInfo != null ? firstInfo.name : "");
            if (firstAyah.juz > 0) tvPageJuzName.setText(juzName(firstAyah.juz));

            // نكتشف: هل الصفحة تبدأ بأول آية في سورة؟
            boolean startsAtSurahBeginning = (firstAyah.numberInSurah == 1);

            if (startsAtSurahBeginning && firstInfo != null) {
                // نُظهر رأس السورة المركزي مع البسملة
                tvMushafSurahHeader.setText("سُورَةُ " + firstInfo.name);
                surahHeaderContainer.setVisibility(View.VISIBLE);
                if (firstInfo.number != 9 && firstInfo.number != 1) showBismillah();
            } else {
                // الصفحة تبدأ من وسط سورة — نُخفي الرأس المركزي
                surahHeaderContainer.setVisibility(View.GONE);
            }

            // بناء نص الصفحة كاملة (مع رؤوس داخلية عند تغيير السورة)
            tvMushafContent.setText(buildPageText(ayahs, startsAtSurahBeginning));
            showContent();
        }

        /**
         * بناء النص المصحفي للصفحة — يتعامل مع عدة سور.
         * عند تغيير السورة يُضاف spacer + اسم السورة الجديدة كـ block نصي.
         */
        private CharSequence buildPageText(List<AyahEntity> ayahs, boolean skipFirstHeader) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            int currentSurah = -1;

            for (AyahEntity ayah : ayahs) {
                boolean isSurahChange = (ayah.surahNumber != currentSurah);

                if (isSurahChange) {
                    // رأس السورة الجديدة داخل النص (إلا لو هي الأولى وعندها رأس خارجي)
                    if (currentSurah != -1) {
                        // فراغ + اسم السورة الجديدة بلون ذهبي
                        sb.append("\n");
                        SurahInfo newInfo = SurahInfo.getByNumber(ayah.surahNumber);
                        String header = newInfo != null
                                ? "─── سُورَةُ " + newInfo.name + " ───"
                                : "─── سورة " + ayah.surahNumber + " ───";
                        int hStart = sb.length();
                        sb.append(header);
                        sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                                hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(new RelativeSizeSpan(0.78f),
                                hStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // بسملة السورة الجديدة
                        if (ayah.numberInSurah == 1) {
                            SurahInfo ni = SurahInfo.getByNumber(ayah.surahNumber);
                            if (ni != null && ni.number != 9 && ni.number != 1) {
                                sb.append("\n");
                                String bism = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ";
                                int bStart = sb.length();
                                sb.append(bism);
                                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                                        bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                sb.setSpan(new RelativeSizeSpan(0.85f),
                                        bStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        sb.append("\n");
                    }
                    currentSurah = ayah.surahNumber;
                }

                sb.append(ayah.text);
                sb.append("\u00A0");

                // رقم الآية ذهبي صغير
                String num = " ﴿" + toAr(ayah.numberInSurah) + "﴾ ";
                int nStart = sb.length();
                sb.append(num);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        nStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.6f),
                        nStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return sb;
        }

        /** النص لوضع السورة الواحدة (بدون رؤوس داخلية) */
        private CharSequence buildText(List<AyahEntity> ayahs) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (AyahEntity a : ayahs) {
                sb.append(a.text).append("\u00A0");
                String num = " ﴿" + toAr(a.numberInSurah) + "﴾ ";
                int s = sb.length();
                sb.append(num);
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                        s, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.6f),
                        s, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return sb;
        }

        // ── مساعدات حالة الـ View ─────────────────────────
        private void resetLoading() {
            svMushafContent.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.GONE);
            tvBismillah.setVisibility(View.GONE);
            bismillahDivider.setVisibility(View.GONE);
            surahHeaderContainer.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            tvPageJuzName.setText("");
            tvPageSurahName.setText("");
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
