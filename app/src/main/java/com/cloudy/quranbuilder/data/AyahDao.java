package com.cloudy.quranbuilder.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface AyahDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAyah(AyahEntity ayah);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<AyahEntity> ayahs);

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

    // ── استعلامات المصحف بالصفحة ────────────────────────────

    /**
     * هل هناك آيات فيها رقم صفحة مُعبَّأ؟
     */
    @Query("SELECT COUNT(*) FROM ayahs WHERE page > 0")
    int getAyahsWithPageCount();

    /**
     * أرقام الصفحات المتميزة التي فيها آيات، مرتبة تصاعدياً.
     */
    @Query("SELECT DISTINCT page FROM ayahs WHERE page > 0 ORDER BY page ASC")
    List<Integer> getDistinctPages();

    /**
     * كل الآيات في صفحة معينة، مرتبة حسب رقم السورة ثم الآية.
     */
    @Query("SELECT * FROM ayahs WHERE page = :pageNum ORDER BY surah_number ASC, number_in_surah ASC")
    List<AyahEntity> getAyahsForPage(int pageNum);

    // ────────────────────────────────────────────────────────

    @Query("SELECT surah_number, COUNT(*) as cnt, COALESCE(MIN(juz),0) as minJuz " +
           "FROM ayahs GROUP BY surah_number")
    List<SurahStat> getSurahStats();

    @Query("SELECT * FROM ayahs ORDER BY surah_number, number_in_surah")
    List<AyahEntity> getAllAyahsSync();

    @Query("DELETE FROM ayahs")
    void deleteAll();

    class SurahStat {
        @ColumnInfo(name = "surah_number") public int surahNumber;
        @ColumnInfo(name = "cnt")          public int count;
        @ColumnInfo(name = "minJuz")       public int minJuz;
    }
}
