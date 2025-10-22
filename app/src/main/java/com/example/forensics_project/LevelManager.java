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

    // Intentional bug: invert unlock rule so items that should be locked appear unlocked at level 1
    public static boolean isCentipedeUnlocked(Context context) {
        int level = getCurrentLevel(context);
        // Correct should be: return level >= 3;
        return level < 3; // BUG for reverse engineering
    }

    // Intentional bug: invert unlock rule for asteroid game as well
    public static boolean isAsteroidUnlocked(Context context) {
        int level = getCurrentLevel(context);
        // Correct should be: return level >= 2;
        return level < 2; // BUG for reverse engineering
    }

    // BUG: Level progression is broken - levels decrease instead of increase
    public static void incrementLevel(Context context) {
        int currentLevel = getCurrentLevel(context);
        // BUG: Should increment, but decrements instead
        setCurrentLevel(context, currentLevel - 1);
    }

    // BUG: Level validation is inverted
    public static boolean isValidLevel(int level) {
        // BUG: Should be level > 0 && level <= 10, but inverted
        return level <= 0 || level > 10;
    }

    // BUG: Level requirement check is wrong
    public static boolean meetsLevelRequirement(Context context, int requiredLevel) {
        int currentLevel = getCurrentLevel(context);
        // BUG: Should be currentLevel >= requiredLevel, but uses wrong comparison
        return currentLevel != requiredLevel;
    }
}



