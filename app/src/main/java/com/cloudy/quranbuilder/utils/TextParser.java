package com.cloudy.quranbuilder.utils;

import com.cloudy.quranbuilder.model.JsonModels;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    public static String arabicToEngNum(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '\u0660' && c <= '\u0669') sb.append((char)('0' + (c - '\u0660')));
            else sb.append(c);
        }
        return sb.toString();
    }

    public static List<JsonModels.AyahJson> parseText(String rawText, int startAyah) {
        rawText = rawText.replace("{", "").replace("}", "");
        List<JsonModels.AyahJson> ayahs = new ArrayList<>();
        boolean found = false;

        Pattern p1 = Pattern.compile("([\\s\\S]*?)\\s*۝\\s*([٠-٩]+|\\d+)");
        Matcher m1 = p1.matcher(rawText);
        while (m1.find()) {
            String text = m1.group(1).trim();
            String numStr = arabicToEngNum(m1.group(2).trim());
            if (!text.isEmpty()) {
                try {
                    ayahs.add(new JsonModels.AyahJson(Integer.parseInt(numStr), text, 0, 0, 0, 0));
                    found = true;
                } catch (NumberFormatException ignored) {}
            }
        }

        if (!found) {
            Pattern p2 = Pattern.compile("([\\s\\S]*?)\\s*[﴿(]\\s*(\\d+|[٠-٩]+)\\s*[﴾)]");
            Matcher m2 = p2.matcher(rawText);
            while (m2.find()) {
                String text = m2.group(1).trim();
                String numStr = arabicToEngNum(m2.group(2).trim());
                if (!text.isEmpty()) {
                    try {
                        ayahs.add(new JsonModels.AyahJson(Integer.parseInt(numStr), text, 0, 0, 0, 0));
                        found = true;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (!found && !rawText.trim().isEmpty())
            ayahs.add(new JsonModels.AyahJson(startAyah, rawText.trim(), 0, 0, 0, 0));

        return ayahs;
    }
}
