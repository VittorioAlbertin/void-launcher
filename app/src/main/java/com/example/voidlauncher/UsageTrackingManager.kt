package com.example.voidlauncher

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * Manages app usage tracking and screen time statistics
 */
object UsageTrackingManager {

    private const val PREFS_NAME = "usage_tracking_prefs"
    private const val KEY_RESET_HOUR = "usage_reset_hour"
    private const val KEY_LAST_RESET = "usage_last_reset"
    private const val KEY_SCREEN_UNLOCKS = "usage_screen_unlocks"
    private const val KEY_CURRENT_SCREEN_ON = "usage_current_screen_on"

    private const val DEFAULT_RESET_HOUR = 3 // 3:00 AM

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if daily reset is needed and perform it
     */
    fun checkAndPerformDailyReset(context: Context) {
        val prefs = getPrefs(context)
        val lastReset = prefs.getLong(KEY_LAST_RESET, 0)
        val resetHour = prefs.getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now

        // Get today's reset time
        val todayReset = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, resetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If current time is past reset time and last reset was before today's reset time
        if (now >= todayReset.timeInMillis && lastReset < todayReset.timeInMillis) {
            performReset(context)
        }
    }

    /**
     * Reset all daily statistics
     */
    private fun performReset(context: Context) {
        // Save today's data to history before resetting
        val resetHour = getResetHour(context)
        // Get data directly without triggering another reset check
        val totalScreenTime = UsageStatsHelper.getTotalScreenTime(context, resetHour)
        val unlockCount = getPrefs(context).getInt(KEY_SCREEN_UNLOCKS, 0)
        val appUsage = UsageStatsHelper.getAllAppUsageToday(context, resetHour)

        // Get yesterday's date (since we're resetting for yesterday's data)
        val yesterday = ScreenTimeHistoryManager.getTodayDate()

        // Save to history
        ScreenTimeHistoryManager.saveDailyData(
            context,
            yesterday,
            totalScreenTime,
            unlockCount,
            appUsage
        )

        // Now reset counters
        val prefs = getPrefs(context)
        prefs.edit().apply {
            // Reset daily counters (UsageStats API handles app data)
            putInt(KEY_SCREEN_UNLOCKS, 0)
            putLong(KEY_LAST_RESET, System.currentTimeMillis())
            remove(KEY_CURRENT_SCREEN_ON)
            apply()
        }
    }

    /**
     * Track when screen turns on
     */
    fun trackScreenOn(context: Context) {
        checkAndPerformDailyReset(context)

        val prefs = getPrefs(context)
        prefs.edit().apply {
            putLong(KEY_CURRENT_SCREEN_ON, System.currentTimeMillis())
            putInt(KEY_SCREEN_UNLOCKS, prefs.getInt(KEY_SCREEN_UNLOCKS, 0) + 1)
            apply()
        }
    }

    /**
     * Track when screen turns off
     */
    fun trackScreenOff(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_CURRENT_SCREEN_ON).apply()
    }

    /**
     * Get usage data for a specific app (using system UsageStats)
     */
    fun getAppUsage(context: Context, packageName: String): AppUsageData {
        checkAndPerformDailyReset(context)
        val resetHour = getResetHour(context)
        return UsageStatsHelper.getAppUsageToday(context, packageName, resetHour)
    }

    /**
     * Get all app usage data (using system UsageStats)
     */
    fun getAllAppUsage(context: Context): List<AppUsageData> {
        checkAndPerformDailyReset(context)
        val resetHour = getResetHour(context)
        return UsageStatsHelper.getAllAppUsageToday(context, resetHour)
    }

    /**
     * Get total screen time for today (using system UsageStats)
     */
    fun getTotalScreenTime(context: Context): Long {
        checkAndPerformDailyReset(context)
        val resetHour = getResetHour(context)
        return UsageStatsHelper.getTotalScreenTime(context, resetHour)
    }

    /**
     * Get last time an app was opened (permanent, not reset daily)
     */
    fun getLastTimeUsed(context: Context, packageName: String): Long {
        return UsageStatsHelper.getLastTimeUsed(context, packageName)
    }

    /**
     * Get screen unlock count for today
     */
    fun getScreenUnlockCount(context: Context): Int {
        checkAndPerformDailyReset(context)

        return getPrefs(context).getInt(KEY_SCREEN_UNLOCKS, 0)
    }

    /**
     * Get reset hour (0-23)
     */
    fun getResetHour(context: Context): Int {
        return getPrefs(context).getInt(KEY_RESET_HOUR, DEFAULT_RESET_HOUR)
    }

    /**
     * Save reset hour (0-23)
     */
    fun saveResetHour(context: Context, hour: Int) {
        getPrefs(context).edit().putInt(KEY_RESET_HOUR, hour).apply()
    }

    /**
     * Format milliseconds to human-readable time
     */
    fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000 / 60).toInt()
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}

/**
 * Data class for app usage statistics
 */
data class AppUsageData(
    val packageName: String,
    val timeSpent: Long,  // in milliseconds
    val openCount: Int
)
