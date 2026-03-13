package com.cloudy.quranbuilder.utils;

import com.cloudy.quranbuilder.model.JsonModels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    // تحويل الأرقام العربية إلى إنجليزية (مطابق لمنطق Python)
    public static String arabicToEngNum(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '\u0660' && c <= '\u0669') {
                sb.append((char) ('0' + (c - '\u0660')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * تحليل النص واستخراج الآيات — مطابق لمنطق السكربت Python
     */
    public static List<JsonModels.AyahJson> parseText(String rawText, int startAyah) {
        // تنظيف الأقواس المعقوفة
        rawText = rawText.replace("{", "").replace("}", "");

        List<JsonModels.AyahJson> ayahs = new ArrayList<>();

        // النمط الأساسي: رمز العلامة ۝
        String primaryPattern = "([\\s\\S]*?)\\s*۝\\s*([٠-٩]+|\\d+)";
        Pattern p1 = Pattern.compile(primaryPattern);
        Matcher m1 = p1.matcher(rawText);

        boolean found = false;
        while (m1.find()) {
            String text = m1.group(1).trim();
            String numStr = arabicToEngNum(m1.group(2).trim());
            if (!text.isEmpty()) {
                try {
                    int num = Integer.parseInt(numStr);
                    ayahs.add(new JsonModels.AyahJson(num, text));
                    found = true;
                } catch (NumberFormatException ignored) {}
            }
        }

        // النمط الاحتياطي: الأقواس ﴿ ﴾ أو ( )
        if (!found) {
            String backupPattern = "([\\s\\S]*?)\\s*[﴿(]\\s*(\\d+|[٠-٩]+)\\s*[﴾)]";
            Pattern p2 = Pattern.compile(backupPattern);
            Matcher m2 = p2.matcher(rawText);
            while (m2.find()) {
                String text = m2.group(1).trim();
                String numStr = arabicToEngNum(m2.group(2).trim());
                if (!text.isEmpty()) {
                    try {
                        int num = Integer.parseInt(numStr);
                        ayahs.add(new JsonModels.AyahJson(num, text));
                        found = true;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // إذا فشل كلا النمطين: نعتبر النص آية واحدة
        if (!found && !rawText.trim().isEmpty()) {
            ayahs.add(new JsonModels.AyahJson(startAyah, rawText.trim()));
        }

        return ayahs;
    }
}
