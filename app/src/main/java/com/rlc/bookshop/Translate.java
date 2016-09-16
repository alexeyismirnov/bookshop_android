package com.rlc.bookshop;

import java.util.LinkedHashMap;

public final class Translate {

    private Translate() {}

    public static String default_language="en";
    private static String[] files = {"trans_ui"};
    private static String language=default_language;
    private static LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();

    public static void setLanguage(String l) {
        language = l;

        map = new LinkedHashMap<String,String>();

        for (String file : files) {
            String fromName = String.format("%s_en", file);
            String toName = String.format("%s_%s", file, language);

            int idKeys = MyApp.mResources.getIdentifier(fromName, "array", MyApp.PACKAGE_NAME);
            String[] keys = MyApp.mResources.getStringArray(idKeys);

            int idValues = MyApp.mResources.getIdentifier(toName, "array", MyApp.PACKAGE_NAME);
            String[] values = MyApp.mResources.getStringArray(idValues);

            for (int i = 0; i < Math.min(keys.length, values.length); ++i) {
                map.put(keys[i], values[i]);
            }
        }

    }

    public static String getLanguage() {
        return language;
    }

    public static String s(String src) {
        if (language.equals(default_language))
            return src;

        if (map.containsKey(src))
            return map.get(src);
        else
            return src;
    }


}
