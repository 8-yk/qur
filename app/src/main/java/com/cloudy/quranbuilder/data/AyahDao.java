package com.cloudy.quranbuilder.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AyahDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAyah(AyahEntity ayah);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<AyahEntity> ayahs);

    @Query("SELECT * FROM ayahs WHERE surah_number = :surahNumber ORDER BY number_in_surah ASC")
    LiveData<List<AyahEntity>> getAyahsForSurah(int surahNumber);

    @Query("SELECT * FROM ayahs WHERE surah_number = :surahNumber ORDER BY number_in_surah ASC")
    List<AyahEntity> getAyahsForSurahSync(int surahNumber);

    @Query("SELECT COUNT(*) FROM ayahs WHERE surah_number = :surahNumber")
    int getAyahCountForSurah(int surahNumber);

    @Query("SELECT COUNT(*) FROM ayahs")
    int getTotalAyahCount();

    @Query("SELECT DISTINCT surah_number FROM ayahs")
    List<Integer> getSurahNumbersWithData();

    @Query("SELECT * FROM ayahs ORDER BY surah_number, number_in_surah")
    List<AyahEntity> getAllAyahsSync();

    @Query("DELETE FROM ayahs")
    void deleteAll();
}
