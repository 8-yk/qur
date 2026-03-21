package com.cloudy.quranbuilder.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface AyahDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAyah(AyahEntity ayah);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<AyahEntity> ayahs);

    // ── Sync ────────────────────────────────────────────────

    @Query("SELECT * FROM ayahs WHERE surah_number=:n ORDER BY number_in_surah ASC")
    List<AyahEntity> getAyahsForSurahSync(int n);

    @Query("SELECT COUNT(*) FROM ayahs WHERE surah_number=:n")
    int getAyahCountForSurah(int n);

    @Query("SELECT COALESCE(MIN(juz),0) FROM ayahs WHERE surah_number=:n")
    int getMinJuzForSurah(int n);

    @Query("SELECT COUNT(*) FROM ayahs")
    int getTotalAyahCount();

    @Query("SELECT DISTINCT surah_number FROM ayahs")
    List<Integer> getSurahNumbersWithData();

    @Query("SELECT COUNT(*) FROM ayahs WHERE page > 0")
    int getAyahsWithPageCount();

    @Query("SELECT DISTINCT page FROM ayahs WHERE page > 0 ORDER BY page ASC")
    List<Integer> getDistinctPages();

    @Query("SELECT * FROM ayahs WHERE page = :pageNum ORDER BY surah_number ASC, number_in_surah ASC")
    List<AyahEntity> getAyahsForPage(int pageNum);

    @Query("SELECT * FROM ayahs ORDER BY surah_number, number_in_surah")
    List<AyahEntity> getAllAyahsSync();

    /**
     * مُلخَّص كل صفحة: رقم الجزء + قائمة أرقام السور (مفصولة بفاصلة).
     * استعلام واحد يُحضر بيانات شريط العنوان لكل الصفحات دفعةً واحدة.
     */
    @Query("SELECT page, " +
           "COALESCE(MIN(juz), 0) AS juz, " +
           "GROUP_CONCAT(DISTINCT surah_number) AS surahNums " +
           "FROM ayahs WHERE page > 0 " +
           "GROUP BY page ORDER BY page ASC")
    List<PageMetaSummary> getAllPageMeta();

    // ── LiveData — Room يُشغّل الاستعلام في الخلفية تلقائياً ─

    /** يتحدث تلقائياً عند أي تعديل على جدول ayahs لهذه السورة */
    @Query("SELECT * FROM ayahs WHERE surah_number=:n ORDER BY number_in_surah ASC")
    LiveData<List<AyahEntity>> getAyahsForSurahLive(int n);

    /** إحصاء الآيات لكل سورة — يتحدث تلقائياً */
    @Query("SELECT surah_number, COUNT(*) as cnt, COALESCE(MIN(juz),0) as minJuz " +
           "FROM ayahs GROUP BY surah_number")
    LiveData<List<SurahStat>> getSurahStatsLive();

    /** إجمالي الآيات — يتحدث تلقائياً */
    @Query("SELECT COUNT(*) FROM ayahs")
    LiveData<Integer> getTotalAyahCountLive();

    @Query("DELETE FROM ayahs")
    void deleteAll();

    // ── Inner classes ────────────────────────────────────────

    class SurahStat {
        @ColumnInfo(name = "surah_number") public int surahNumber;
        @ColumnInfo(name = "cnt")          public int count;
        @ColumnInfo(name = "minJuz")       public int minJuz;
    }

    class PageMetaSummary {
        @ColumnInfo(name = "page")      public int    page;
        @ColumnInfo(name = "juz")       public int    juz;
        @ColumnInfo(name = "surahNums") public String surahNums; // "18,19" مثلاً
    }
}
