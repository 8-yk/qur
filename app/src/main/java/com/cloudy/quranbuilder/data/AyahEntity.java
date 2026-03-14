package com.cloudy.quranbuilder.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "ayahs",
    foreignKeys = @ForeignKey(
        entity = SurahEntity.class,
        parentColumns = "number",
        childColumns = "surah_number",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = {"surah_number", "number_in_surah"}, unique = true)}
)
public class AyahEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "surah_number")
    public int surahNumber;

    @ColumnInfo(name = "number_in_surah")
    public int numberInSurah;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "juz", defaultValue = "0")
    public int juz;

    @ColumnInfo(name = "hizb", defaultValue = "0")
    public int hizb;

    @ColumnInfo(name = "hizb_quarter", defaultValue = "0")
    public int hizbQuarter;

    public AyahEntity(int surahNumber, int numberInSurah, String text,
                      int juz, int hizb, int hizbQuarter) {
        this.surahNumber  = surahNumber;
        this.numberInSurah = numberInSurah;
        this.text          = text;
        this.juz           = juz;
        this.hizb          = hizb;
        this.hizbQuarter   = hizbQuarter;
    }
}
