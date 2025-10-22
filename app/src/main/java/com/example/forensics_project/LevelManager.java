package com.example.forensics_project;

import android.content.Context;
import android.content.SharedPreferences;

public class LevelManager {
    private static final String PREFS = "levels";
    private static final String KEY_LEVEL = "current_level";

    public static int getCurrentLevel(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_LEVEL, 1);
    }

    public static void setCurrentLevel(Context context, int level) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_LEVEL, level).apply();
    }

    public static boolean isCentipedeUnlocked(Context context) {
        int level = getCurrentLevel(context);
        return level >= 3;
    }

    public static boolean isAsteroidUnlocked(Context context) {
        int level = getCurrentLevel(context);
        return level >= 2;
    }

    public static void incrementLevel(Context context) {
        int currentLevel = getCurrentLevel(context);
        setCurrentLevel(context, currentLevel + 1);
    }

    public static boolean isValidLevel(int level) {
        return level > 0 && level <= 10;
    }

    public static boolean meetsLevelRequirement(Context context, int requiredLevel) {
        int currentLevel = getCurrentLevel(context);
        return currentLevel >= requiredLevel;
    }
}



