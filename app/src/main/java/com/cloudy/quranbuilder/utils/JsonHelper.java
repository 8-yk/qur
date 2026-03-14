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

    public static void exportToUri(Context context, Uri uri) throws IOException {
        AppDatabase db = AppDatabase.getInstance(context);
        List<SurahEntity> surahs = db.surahDao().getAllSurahsSync();
        List<AyahEntity> allAyahs = db.ayahDao().getAllAyahsSync();

        Map<Integer, List<AyahEntity>> map = new HashMap<>();
        for (AyahEntity a : allAyahs) {
            map.computeIfAbsent(a.surahNumber, k -> new ArrayList<>()).add(a);
        }

        List<JsonModels.SurahJson> surahList = new ArrayList<>();
        for (SurahEntity s : surahs) {
            List<AyahEntity> ayahEntities = map.getOrDefault(s.number, new ArrayList<>());
            List<JsonModels.AyahJson> ayahJsonList = new ArrayList<>();
            for (AyahEntity a : ayahEntities) {
                ayahJsonList.add(new JsonModels.AyahJson(
                        a.numberInSurah, a.text, a.juz, a.hizb, a.hizbQuarter));
            }
            surahList.add(new JsonModels.SurahJson(s.number, s.name, ayahJsonList));
        }

        JsonModels.JsonRoot root = new JsonModels.JsonRoot();
        root.data.surahs = surahList;

        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
            if (os != null) os.write(GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static ImportResult importFromUri(Context context, Uri uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }

        JsonModels.JsonRoot root = GSON.fromJson(sb.toString(), JsonModels.JsonRoot.class);
        if (root == null || root.data == null || root.data.surahs == null)
            throw new IOException("صيغة ملف JSON غير صحيحة");

        AppDatabase db = AppDatabase.getInstance(context);
        int surahsAdded = 0, ayahsAdded = 0;

        for (JsonModels.SurahJson sj : root.data.surahs) {
            db.surahDao().insertSurah(new SurahEntity(sj.number, sj.name));
            surahsAdded++;
            if (sj.ayahs != null) {
                for (JsonModels.AyahJson aj : sj.ayahs) {
                    db.ayahDao().insertAyah(new AyahEntity(
                            sj.number, aj.numberInSurah, aj.text,
                            aj.juz, aj.hizb, aj.hizbQuarter));
                    ayahsAdded++;
                }
            }
        }
        return new ImportResult(surahsAdded, ayahsAdded);
    }

    public static class ImportResult {
        public final int surahsAdded, ayahsAdded;
        public ImportResult(int s, int a) { surahsAdded = s; ayahsAdded = a; }
    }
}
