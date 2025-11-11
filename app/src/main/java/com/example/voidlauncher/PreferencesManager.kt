package com.example.voidlauncher

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages all VoidLauncher preferences and settings
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "void_launcher_prefs"
        private const val KEY_HOMEPAGE_APPS = "homepage_apps"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_FONT_SIZE = "font_size"
        private const val DEFAULT_FONT_SIZE = 16f
        private const val KEY_SHOW_DATE = "show_date"
        private const val DEFAULT_SHOW_DATE = false
        private const val KEY_CLOCK_SIZE = "clock_size"
        private const val DEFAULT_CLOCK_SIZE = 24f
        private const val KEY_CLOCK_24H = "clock_24h"
        private const val DEFAULT_CLOCK_24H = true
        private const val KEY_CLOCK_APP = "clock_app"
        private const val KEY_SHOW_DAY_OF_WEEK = "show_day_of_week"
        private const val DEFAULT_SHOW_DAY_OF_WEEK = false

        // Gesture app keys
        private const val KEY_GESTURE_UP = "gesture_up"
        private const val KEY_GESTURE_DOWN = "gesture_down"
        private const val KEY_GESTURE_LEFT = "gesture_left"
        private const val KEY_GESTURE_RIGHT = "gesture_right"

        // Double tap to lock screen
        private const val KEY_DOUBLE_TAP_LOCK = "double_tap_lock"
        private const val DEFAULT_DOUBLE_TAP_LOCK = true

        // Default homepage apps if none are selected
        private val DEFAULT_HOMEPAGE_APPS = listOf(
            "com.instagram.android",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.android.settings"
        )

        // Default gesture apps
        private const val DEFAULT_GESTURE_UP = "com.voidlauncher.ALL_APPS"
        private const val DEFAULT_GESTURE_DOWN = "com.android.camera2"
        private const val DEFAULT_GESTURE_LEFT = "com.whatsapp"
        private const val DEFAULT_GESTURE_RIGHT = "com.android.camera2"
    }

    /**
     * Get list of package names for apps to show on homepage
     */
    fun getHomepageApps(): Set<String> {
        val saved = prefs.getStringSet(KEY_HOMEPAGE_APPS, null)
        return if (saved.isNullOrEmpty()) {
            DEFAULT_HOMEPAGE_APPS.toSet()
        } else {
            saved
        }
    }

    /**
     * Save list of package names for homepage apps
     */
    fun saveHomepageApps(packageNames: Set<String>) {
        prefs.edit().putStringSet(KEY_HOMEPAGE_APPS, packageNames).apply()
    }

    /**
     * Get list of package names for apps to hide from All Apps
     */
    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()
    }

    /**
     * Save list of package names for hidden apps
     */
    fun saveHiddenApps(packageNames: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, packageNames).apply()
    }

    /**
     * Get font size in SP
     */
    fun getFontSize(): Float {
        return prefs.getFloat(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
    }

    /**
     * Save font size in SP
     */
    fun saveFontSize(size: Float) {
        prefs.edit().putFloat(KEY_FONT_SIZE, size).apply()
    }

    /**
     * Check if an app is hidden
     */
    fun isAppHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    /**
     * Check if an app is on homepage
     */
    fun isAppOnHomepage(packageName: String): Boolean {
        return getHomepageApps().contains(packageName)
    }

    /**
     * Get package name for a gesture direction
     * Returns null if gesture is disabled
     */
    fun getGestureApp(direction: String): String? {
        val key = when (direction) {
            "up" -> KEY_GESTURE_UP
            "down" -> KEY_GESTURE_DOWN
            "left" -> KEY_GESTURE_LEFT
            "right" -> KEY_GESTURE_RIGHT
            else -> return null
        }

        val saved = prefs.getString(key, null)

        // If nothing saved yet, return default
        if (saved == null) {
            return when (direction) {
                "up" -> DEFAULT_GESTURE_UP
                "down" -> DEFAULT_GESTURE_DOWN
                "left" -> DEFAULT_GESTURE_LEFT
                "right" -> DEFAULT_GESTURE_RIGHT
                else -> null
            }
        }

        // Empty string means disabled
        return if (saved.isEmpty()) null else saved
    }

    /**
     * Save package name for a gesture direction
     * Pass empty string to disable the gesture
     */
    fun saveGestureApp(direction: String, packageName: String) {
        val key = when (direction) {
            "up" -> KEY_GESTURE_UP
            "down" -> KEY_GESTURE_DOWN
            "left" -> KEY_GESTURE_LEFT
            "right" -> KEY_GESTURE_RIGHT
            else -> return
        }
        prefs.edit().putString(key, packageName).apply()
    }

    /**
     * Check if a gesture is enabled (has an app assigned)
     */
    fun isGestureSet(direction: String): Boolean {
        return getGestureApp(direction) != null
    }

    /**
     * Get whether date should be shown under clock
     */
    fun getShowDate(): Boolean {
        return prefs.getBoolean(KEY_SHOW_DATE, DEFAULT_SHOW_DATE)
    }

    /**
     * Save whether date should be shown under clock
     */
    fun saveShowDate(showDate: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DATE, showDate).apply()
    }

    /**
     * Get clock size in SP
     */
    fun getClockSize(): Float {
        return prefs.getFloat(KEY_CLOCK_SIZE, DEFAULT_CLOCK_SIZE)
    }

    /**
     * Save clock size in SP
     */
    fun saveClockSize(size: Float) {
        prefs.edit().putFloat(KEY_CLOCK_SIZE, size).apply()
    }

    /**
     * Get whether to use 24h format
     */
    fun getClock24h(): Boolean {
        return prefs.getBoolean(KEY_CLOCK_24H, DEFAULT_CLOCK_24H)
    }

    /**
     * Save whether to use 24h format
     */
    fun saveClock24h(use24h: Boolean) {
        prefs.edit().putBoolean(KEY_CLOCK_24H, use24h).apply()
    }

    /**
     * Get clock app package name (null = not set)
     */
    fun getClockApp(): String? {
        return prefs.getString(KEY_CLOCK_APP, null)
    }

    /**
     * Save clock app package name
     */
    fun saveClockApp(packageName: String?) {
        prefs.edit().putString(KEY_CLOCK_APP, packageName).apply()
    }

    /**
     * Get whether to show day of week with clock
     */
    fun getShowDayOfWeek(): Boolean {
        return prefs.getBoolean(KEY_SHOW_DAY_OF_WEEK, DEFAULT_SHOW_DAY_OF_WEEK)
    }

    /**
     * Save whether to show day of week with clock
     */
    fun saveShowDayOfWeek(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DAY_OF_WEEK, show).apply()
    }

    /**
     * Get auto-hide rules for a specific app
     * Returns JSON string or null if no rules set
     */
    fun getAutoHideRules(packageName: String): String? {
        return prefs.getString("auto_hide_$packageName", null)
    }

    /**
     * Save auto-hide rules for a specific app
     * Pass null to remove rules
     */
    fun saveAutoHideRules(packageName: String, rulesJson: String?) {
        if (rulesJson == null) {
            prefs.edit().remove("auto_hide_$packageName").apply()
        } else {
            prefs.edit().putString("auto_hide_$packageName", rulesJson).apply()
        }
    }

    /**
     * Get all package names that have auto-hide rules
     */
    fun getAllAutoHidePackages(): Set<String> {
        return prefs.all.keys
            .filter { it.startsWith("auto_hide_") }
            .map { it.removePrefix("auto_hide_") }
            .toSet()
    }

    /**
     * Get whether double tap to lock screen is enabled
     */
    fun getDoubleTapToLock(): Boolean {
        return prefs.getBoolean(KEY_DOUBLE_TAP_LOCK, DEFAULT_DOUBLE_TAP_LOCK)
    }

    /**
     * Save whether double tap to lock screen is enabled
     */
    fun saveDoubleTapToLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DOUBLE_TAP_LOCK, enabled).apply()
    }
}
