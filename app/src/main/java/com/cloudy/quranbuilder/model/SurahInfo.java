package com.cloudy.quranbuilder.model;

public class SurahInfo {
    public final int number;
    public final String name;
    public final int totalAyahs;
    public final boolean isMeccan;

    public SurahInfo(int number, String name, int totalAyahs, boolean isMeccan) {
        this.number = number;
        this.name = name;
        this.totalAyahs = totalAyahs;
        this.isMeccan = isMeccan;
    }

    public String getRevelationType() {
        return isMeccan ? "مكية" : "مدنية";
    }

    // ── جميع السور الـ 114 ──────────────────────────────────────
    public static final SurahInfo[] ALL_SURAHS = {
        new SurahInfo(1,   "الفاتحة",     7,   false),
        new SurahInfo(2,   "البقرة",      286,  false),
        new SurahInfo(3,   "آل عمران",   200,  false),
        new SurahInfo(4,   "النساء",      176,  false),
        new SurahInfo(5,   "المائدة",    120,  false),
        new SurahInfo(6,   "الأنعام",    165,  true),
        new SurahInfo(7,   "الأعراف",    206,  true),
        new SurahInfo(8,   "الأنفال",    75,   false),
        new SurahInfo(9,   "التوبة",     129,  false),
        new SurahInfo(10,  "يونس",       109,  true),
        new SurahInfo(11,  "هود",        123,  true),
        new SurahInfo(12,  "يوسف",       111,  true),
        new SurahInfo(13,  "الرعد",      43,   false),
        new SurahInfo(14,  "إبراهيم",   52,   true),
        new SurahInfo(15,  "الحجر",      99,   true),
        new SurahInfo(16,  "النحل",      128,  true),
        new SurahInfo(17,  "الإسراء",   111,  true),
        new SurahInfo(18,  "الكهف",      110,  true),
        new SurahInfo(19,  "مريم",       98,   true),
        new SurahInfo(20,  "طه",         135,  true),
        new SurahInfo(21,  "الأنبياء",  112,  true),
        new SurahInfo(22,  "الحج",       78,   false),
        new SurahInfo(23,  "المؤمنون",  118,  true),
        new SurahInfo(24,  "النور",      64,   false),
        new SurahInfo(25,  "الفرقان",   77,   true),
        new SurahInfo(26,  "الشعراء",   227,  true),
        new SurahInfo(27,  "النمل",      93,   true),
        new SurahInfo(28,  "القصص",      88,   true),
        new SurahInfo(29,  "العنكبوت",  69,   true),
        new SurahInfo(30,  "الروم",      60,   true),
        new SurahInfo(31,  "لقمان",      34,   true),
        new SurahInfo(32,  "السجدة",    30,   true),
        new SurahInfo(33,  "الأحزاب",   73,   false),
        new SurahInfo(34,  "سبأ",        54,   true),
        new SurahInfo(35,  "فاطر",       45,   true),
        new SurahInfo(36,  "يس",         83,   true),
        new SurahInfo(37,  "الصافات",   182,  true),
        new SurahInfo(38,  "ص",          88,   true),
        new SurahInfo(39,  "الزمر",      75,   true),
        new SurahInfo(40,  "غافر",       85,   true),
        new SurahInfo(41,  "فصلت",       54,   true),
        new SurahInfo(42,  "الشورى",    53,   true),
        new SurahInfo(43,  "الزخرف",    89,   true),
        new SurahInfo(44,  "الدخان",    59,   true),
        new SurahInfo(45,  "الجاثية",   37,   true),
        new SurahInfo(46,  "الأحقاف",   35,   true),
        new SurahInfo(47,  "محمد",       38,   false),
        new SurahInfo(48,  "الفتح",      29,   false),
        new SurahInfo(49,  "الحجرات",   18,   false),
        new SurahInfo(50,  "ق",          45,   true),
        new SurahInfo(51,  "الذاريات",  60,   true),
        new SurahInfo(52,  "الطور",      49,   true),
        new SurahInfo(53,  "النجم",      62,   true),
        new SurahInfo(54,  "القمر",      55,   true),
        new SurahInfo(55,  "الرحمن",    78,   true),
        new SurahInfo(56,  "الواقعة",   96,   true),
        new SurahInfo(57,  "الحديد",    29,   false),
        new SurahInfo(58,  "المجادلة",  22,   false),
        new SurahInfo(59,  "الحشر",      24,   false),
        new SurahInfo(60,  "الممتحنة",  13,   false),
        new SurahInfo(61,  "الصف",       14,   false),
        new SurahInfo(62,  "الجمعة",    11,   false),
        new SurahInfo(63,  "المنافقون", 11,   false),
        new SurahInfo(64,  "التغابن",   18,   false),
        new SurahInfo(65,  "الطلاق",    12,   false),
        new SurahInfo(66,  "التحريم",   12,   false),
        new SurahInfo(67,  "الملك",      30,   true),
        new SurahInfo(68,  "القلم",      52,   true),
        new SurahInfo(69,  "الحاقة",    52,   true),
        new SurahInfo(70,  "المعارج",   44,   true),
        new SurahInfo(71,  "نوح",        28,   true),
        new SurahInfo(72,  "الجن",       28,   true),
        new SurahInfo(73,  "المزمل",    20,   true),
        new SurahInfo(74,  "المدثر",    56,   true),
        new SurahInfo(75,  "القيامة",   40,   true),
        new SurahInfo(76,  "الإنسان",   31,   false),
        new SurahInfo(77,  "المرسلات",  50,   true),
        new SurahInfo(78,  "النبأ",      40,   true),
        new SurahInfo(79,  "النازعات",  46,   true),
        new SurahInfo(80,  "عبس",        42,   true),
        new SurahInfo(81,  "التكوير",   29,   true),
        new SurahInfo(82,  "الانفطار",  19,   true),
        new SurahInfo(83,  "المطففين",  36,   true),
        new SurahInfo(84,  "الانشقاق",  25,   true),
        new SurahInfo(85,  "البروج",    22,   true),
        new SurahInfo(86,  "الطارق",    17,   true),
        new SurahInfo(87,  "الأعلى",    19,   true),
        new SurahInfo(88,  "الغاشية",   26,   true),
        new SurahInfo(89,  "الفجر",      30,   true),
        new SurahInfo(90,  "البلد",      20,   true),
        new SurahInfo(91,  "الشمس",      15,   true),
        new SurahInfo(92,  "الليل",      21,   true),
        new SurahInfo(93,  "الضحى",      11,   true),
        new SurahInfo(94,  "الشرح",      8,    true),
        new SurahInfo(95,  "التين",      8,    true),
        new SurahInfo(96,  "العلق",      19,   true),
        new SurahInfo(97,  "القدر",      5,    true),
        new SurahInfo(98,  "البينة",    8,    false),
        new SurahInfo(99,  "الزلزلة",   8,    false),
        new SurahInfo(100, "العاديات",  11,   true),
        new SurahInfo(101, "القارعة",   11,   true),
        new SurahInfo(102, "التكاثر",   8,    true),
        new SurahInfo(103, "العصر",      3,    true),
        new SurahInfo(104, "الهمزة",    9,    true),
        new SurahInfo(105, "الفيل",      5,    true),
        new SurahInfo(106, "قريش",       4,    true),
        new SurahInfo(107, "الماعون",   7,    false),
        new SurahInfo(108, "الكوثر",    3,    true),
        new SurahInfo(109, "الكافرون",  6,    true),
        new SurahInfo(110, "النصر",      3,    false),
        new SurahInfo(111, "المسد",      5,    true),
        new SurahInfo(112, "الإخلاص",   4,    true),
        new SurahInfo(113, "الفلق",      5,    true),
        new SurahInfo(114, "الناس",      6,    true),
    };

    public static SurahInfo getByNumber(int number) {
        if (number >= 1 && number <= 114) {
            return ALL_SURAHS[number - 1];
        }
        return null;
    }
}
