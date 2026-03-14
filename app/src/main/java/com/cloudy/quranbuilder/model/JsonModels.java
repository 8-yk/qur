package com.cloudy.quranbuilder.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JsonModels {

    public static class JsonRoot {
        @SerializedName("data") public DataNode data;
        public JsonRoot() { this.data = new DataNode(); }
    }

    public static class DataNode {
        @SerializedName("surahs") public List<SurahJson> surahs;
    }

    public static class SurahJson {
        @SerializedName("number")        public int number;
        @SerializedName("name")          public String name;
        @SerializedName("isMeccan")      public boolean isMeccan;
        @SerializedName("ayahsInSurah")  public int ayahsInSurah;
        @SerializedName("ayahs")         public List<AyahJson> ayahs;

        public SurahJson() {}
        public SurahJson(int number, String name, boolean isMeccan,
                         int ayahsInSurah, List<AyahJson> ayahs) {
            this.number       = number;
            this.name         = name;
            this.isMeccan     = isMeccan;
            this.ayahsInSurah = ayahsInSurah;
            this.ayahs        = ayahs;
        }
    }

    public static class AyahJson {
        @SerializedName("numberInSurah") public int numberInSurah;
        @SerializedName("text")          public String text;
        @SerializedName("juz")           public int juz;
        @SerializedName("hizb")          public int hizb;
        @SerializedName("hizbQuarter")   public int hizbQuarter;
        @SerializedName("page")          public int page;

        public AyahJson() {}
        public AyahJson(int numberInSurah, String text,
                        int juz, int hizb, int hizbQuarter, int page) {
            this.numberInSurah = numberInSurah;
            this.text          = text;
            this.juz           = juz;
            this.hizb          = hizb;
            this.hizbQuarter   = hizbQuarter;
            this.page          = page;
        }
    }
}
