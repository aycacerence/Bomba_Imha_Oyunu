package com.example.codedefuse;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static final String PREF_NAME = "code_defuse_prefs";
    private static final String KEY_USERNAME = "username";

    public static void saveUsername(Context context, String username) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static String getUsername(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USERNAME, "");
    }

    public static boolean hasUsername(Context context) {
        String username = getUsername(context);
        return username != null && !username.trim().isEmpty();
    }
}
