package com.cloudy.quranbuilder.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SurahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSurah(SurahEntity surah);

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    LiveData<List<SurahEntity>> getAllSurahs();

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    List<SurahEntity> getAllSurahsSync();

    @Query("SELECT COUNT(*) FROM surahs")
    int getSurahCount();

    @Query("DELETE FROM surahs")
    void deleteAll();
}
