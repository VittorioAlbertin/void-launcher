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

        // Gesture app keys
        private const val KEY_GESTURE_UP = "gesture_up"
        private const val KEY_GESTURE_DOWN = "gesture_down"
        private const val KEY_GESTURE_LEFT = "gesture_left"
        private const val KEY_GESTURE_RIGHT = "gesture_right"

        // Default homepage apps if none are selected
        private val DEFAULT_HOMEPAGE_APPS = listOf(
            "com.instagram.android",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.google.android.youtube",
            "com.android.settings"
        )

        // Default gesture apps
        private const val DEFAULT_GESTURE_UP = "com.android.chrome"
        private const val DEFAULT_GESTURE_DOWN = "com.android.camera2"
        private const val DEFAULT_GESTURE_LEFT = "com.whatsapp"
        private const val DEFAULT_GESTURE_RIGHT = "com.google.android.apps.messaging"
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
}
