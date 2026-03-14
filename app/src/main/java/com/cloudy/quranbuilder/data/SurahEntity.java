package com.cloudy.quranbuilder.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "surahs")
public class SurahEntity {

    @PrimaryKey
    @ColumnInfo(name = "number")
    public int number;

    @ColumnInfo(name = "name")
    public String name;

    /** true = مكية، false = مدنية */
    @ColumnInfo(name = "is_meccan", defaultValue = "1")
    public boolean isMeccan;

    /** عدد الآيات الكلي في السورة (يُدخله المستخدم عند إضافة السورة) */
    @ColumnInfo(name = "ayahs_in_surah", defaultValue = "0")
    public int ayahsInSurah;

    public SurahEntity(int number, String name, boolean isMeccan, int ayahsInSurah) {
        this.number        = number;
        this.name          = name;
        this.isMeccan      = isMeccan;
        this.ayahsInSurah  = ayahsInSurah;
    }
}
