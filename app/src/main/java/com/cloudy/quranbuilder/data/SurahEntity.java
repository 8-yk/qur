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

    public SurahEntity(int number, String name) {
        this.number = number;
        this.name = name;
    }
}
