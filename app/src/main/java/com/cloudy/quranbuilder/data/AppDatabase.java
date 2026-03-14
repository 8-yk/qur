package com.cloudy.quranbuilder.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {SurahEntity.class, AyahEntity.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract SurahDao surahDao();
    public abstract AyahDao  ayahDao();

    static final Migration M_1_2 = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE ayahs ADD COLUMN juz INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE ayahs ADD COLUMN hizb INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE ayahs ADD COLUMN hizb_quarter INTEGER NOT NULL DEFAULT 0");
        }
    };
    static final Migration M_2_3 = new Migration(2, 3) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE surahs ADD COLUMN is_meccan INTEGER NOT NULL DEFAULT 1");
        }
    };
    static final Migration M_3_4 = new Migration(3, 4) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE surahs ADD COLUMN ayahs_in_surah INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE ayahs  ADD COLUMN page INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "quran_builder.db"
                    ).addMigrations(M_1_2, M_2_3, M_3_4).build();
                }
            }
        }
        return INSTANCE;
    }
}
