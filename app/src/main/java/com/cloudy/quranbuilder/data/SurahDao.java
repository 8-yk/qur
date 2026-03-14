package com.cloudy.quranbuilder.data;

import androidx.room.*;
import java.util.List;

@Dao
public interface SurahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSurah(SurahEntity surah);

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    List<SurahEntity> getAllSurahsSync();

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    SurahEntity getByNumber(int number);

    @Query("SELECT COUNT(*) FROM surahs")
    int getSurahCount();

    @Query("DELETE FROM surahs")
    void deleteAll();
}
