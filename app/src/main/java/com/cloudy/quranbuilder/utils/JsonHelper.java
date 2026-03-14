package com.cloudy.quranbuilder.utils;

import android.content.Context;
import android.net.Uri;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.model.JsonModels;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().disableHtmlEscaping().create();

    public static void exportToUri(Context ctx, Uri uri) throws IOException {
        AppDatabase db = AppDatabase.getInstance(ctx);
        List<SurahEntity> surahs  = db.surahDao().getAllSurahsSync();
        List<AyahEntity>  allAyahs = db.ayahDao().getAllAyahsSync();

        // تجميع الآيات حسب السورة
        Map<Integer, List<AyahEntity>> map = new HashMap<>();
        for (AyahEntity a : allAyahs)
            map.computeIfAbsent(a.surahNumber, k -> new ArrayList<>()).add(a);

        List<JsonModels.SurahJson> list = new ArrayList<>();
        for (SurahEntity s : surahs) {
            List<AyahEntity> ae = map.getOrDefault(s.number, new ArrayList<>());

            // عدد الآيات الفعلي المضاف (وليس القيمة المخزنة في surah)
            int actualAyahCount = ae.size();

            List<JsonModels.AyahJson> aj = new ArrayList<>();
            for (AyahEntity a : ae)
                aj.add(new JsonModels.AyahJson(a.numberInSurah, a.text,
                        a.juz, a.hizb, a.hizbQuarter, a.page));

            list.add(new JsonModels.SurahJson(
                    s.number, s.name, s.isMeccan, actualAyahCount, aj));
        }

        JsonModels.JsonRoot root = new JsonModels.JsonRoot();
        root.data.surahs = list;

        try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
            if (os != null) os.write(GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static ImportResult importFromUri(Context ctx, Uri uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        JsonModels.JsonRoot root = GSON.fromJson(sb.toString(), JsonModels.JsonRoot.class);
        if (root == null || root.data == null || root.data.surahs == null)
            throw new IOException("صيغة ملف JSON غير صحيحة");

        AppDatabase db = AppDatabase.getInstance(ctx);
        int sc = 0, ac = 0;
        for (JsonModels.SurahJson sj : root.data.surahs) {
            // عند الاستيراد ayahsInSurah = 0 دائماً (يُحسب من الآيات)
            db.surahDao().insertSurah(new SurahEntity(sj.number, sj.name, sj.isMeccan, 0));
            sc++;
            if (sj.ayahs != null) for (JsonModels.AyahJson aj : sj.ayahs) {
                db.ayahDao().insertAyah(new AyahEntity(sj.number, aj.numberInSurah, aj.text,
                        aj.juz, aj.hizb, aj.hizbQuarter, aj.page));
                ac++;
            }
        }
        return new ImportResult(sc, ac);
    }

    public static class ImportResult {
        public final int surahsAdded, ayahsAdded;
        public ImportResult(int s, int a) { surahsAdded = s; ayahsAdded = a; }
    }
}
