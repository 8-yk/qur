package com.cloudy.quranbuilder.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JsonModels {

    public static class JsonRoot {
        @SerializedName("data")
        public DataNode data;

        public JsonRoot() {
            this.data = new DataNode();
        }
    }

    public static class DataNode {
        @SerializedName("surahs")
        public List<SurahJson> surahs;
    }

    public static class SurahJson {
        @SerializedName("number")
        public int number;

        @SerializedName("name")
        public String name;

        @SerializedName("ayahs")
        public List<AyahJson> ayahs;

        public SurahJson() {}

        public SurahJson(int number, String name, List<AyahJson> ayahs) {
            this.number = number;
            this.name = name;
            this.ayahs = ayahs;
        }
    }

    public static class AyahJson {
        @SerializedName("numberInSurah")
        public int numberInSurah;

        @SerializedName("text")
        public String text;

        public AyahJson() {}

        public AyahJson(int numberInSurah, String text) {
            this.numberInSurah = numberInSurah;
            this.text = text;
        }
    }
}
