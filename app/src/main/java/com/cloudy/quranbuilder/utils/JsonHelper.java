package com.cloudy.quranbuilder.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.data.AyahEntity;
import com.cloudy.quranbuilder.data.SurahEntity;
import com.cloudy.quranbuilder.model.JsonModels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /** تصدير كامل قاعدة البيانات إلى JSON عبر SAF Uri */
    public static void exportToUri(Context context, Uri uri) throws IOException {
        AppDatabase db = AppDatabase.getInstance(context);
        List<SurahEntity> surahs = db.surahDao().getAllSurahsSync();
        List<AyahEntity> allAyahs = db.ayahDao().getAllAyahsSync();

        // تجميع الآيات حسب السورة
        Map<Integer, List<AyahEntity>> ayahsBySurah = new HashMap<>();
        for (AyahEntity ayah : allAyahs) {
            ayahsBySurah
                .computeIfAbsent(ayah.surahNumber, k -> new ArrayList<>())
                .add(ayah);
        }

        // بناء الـ JSON
        List<JsonModels.SurahJson> surahJsonList = new ArrayList<>();
        for (SurahEntity surah : surahs) {
            List<AyahEntity> ayahEntities = ayahsBySurah.getOrDefault(surah.number, new ArrayList<>());
            List<JsonModels.AyahJson> ayahJsonList = new ArrayList<>();
            for (AyahEntity a : ayahEntities) {
                ayahJsonList.add(new JsonModels.AyahJson(a.numberInSurah, a.text));
            }
            surahJsonList.add(new JsonModels.SurahJson(surah.number, surah.name, ayahJsonList));
        }

        JsonModels.JsonRoot root = new JsonModels.JsonRoot();
        root.data.surahs = surahJsonList;

        String json = GSON.toJson(root);

        // كتابة الملف عبر SAF (لا تحتاج أي صلاحيات)
        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /** استيراد بيانات من JSON عبر SAF Uri — مع دمج البيانات الموجودة */
    public static ImportResult importFromUri(Context context, Uri uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

        JsonModels.JsonRoot root = GSON.fromJson(sb.toString(), JsonModels.JsonRoot.class);

        if (root == null || root.data == null || root.data.surahs == null) {
            throw new IOException("صيغة ملف JSON غير صحيحة");
        }

        AppDatabase db = AppDatabase.getInstance(context);
        int surahsAdded = 0;
        int ayahsAdded = 0;

        for (JsonModels.SurahJson surahJson : root.data.surahs) {
            // إضافة أو تحديث السورة
            db.surahDao().insertSurah(new SurahEntity(surahJson.number, surahJson.name));
            surahsAdded++;

            // إضافة الآيات
            if (surahJson.ayahs != null) {
                for (JsonModels.AyahJson ayahJson : surahJson.ayahs) {
                    long result = -1;
                    try {
                        AyahEntity entity = new AyahEntity(
                                surahJson.number,
                                ayahJson.numberInSurah,
                                ayahJson.text
                        );
                        db.ayahDao().insertAyah(entity);
                        ayahsAdded++;
                    } catch (Exception ignored) {}
                }
            }
        }

        return new ImportResult(surahsAdded, ayahsAdded);
    }

    public static class ImportResult {
        public final int surahsAdded;
        public final int ayahsAdded;

        public ImportResult(int surahsAdded, int ayahsAdded) {
            this.surahsAdded = surahsAdded;
            this.ayahsAdded = ayahsAdded;
        }
    }
}
