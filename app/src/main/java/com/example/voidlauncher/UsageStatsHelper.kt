package com.example.voidlauncher

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

/**
 * Helper for accessing Android's UsageStatsManager API
 */
object UsageStatsHelper {

    /**
     * Check if PACKAGE_USAGE_STATS permission is granted
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Open system settings to grant PACKAGE_USAGE_STATS permission
     */
    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Get usage stats for today (from reset hour until now)
     */
    fun getTodayUsageStats(context: Context, resetHour: Int): Map<String, UsageStats> {
        if (!hasUsageStatsPermission(context)) {
            return emptyMap()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Calculate start time (today at reset hour)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, resetHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If current time is before reset hour, use yesterday's reset time
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < resetHour) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Query usage stats
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Convert to map by package name, aggregating stats
        val statsMap = mutableMapOf<String, UsageStats>()
        usageStatsList?.forEach { stats ->
            if (stats.totalTimeInForeground > 0) {
                val existing = statsMap[stats.packageName]
                if (existing == null) {
                    statsMap[stats.packageName] = stats
                } else {
                    // Aggregate stats if multiple entries exist
                    existing.add(stats)
                }
            }
        }

        return statsMap
    }

    /**
     * Get usage stats for a specific app today
     */
    fun getAppUsageToday(context: Context, packageName: String, resetHour: Int): AppUsageData {
        val todayStats = getTodayUsageStats(context, resetHour)
        val stats = todayStats[packageName]

        if (stats != null) {
            val timeSpent = stats.totalTimeInForeground
            // Approximate open count by counting events (not perfect but reasonable)
            val openCount = if (stats.lastTimeUsed > 0) {
                // This is a rough approximation - actual count would require event queries
                maxOf(1, (timeSpent / (5 * 60 * 1000)).toInt()) // Estimate: 1 open per 5 min avg
            } else {
                0
            }

            return AppUsageData(
                packageName = packageName,
                timeSpent = timeSpent,
                openCount = openCount
            )
        }

        return AppUsageData(packageName, 0, 0)
    }

    /**
     * Get all app usage data for today, sorted by time spent
     */
    fun getAllAppUsageToday(context: Context, resetHour: Int): List<AppUsageData> {
        val todayStats = getTodayUsageStats(context, resetHour)

        return todayStats.map { (packageName, stats) ->
            val timeSpent = stats.totalTimeInForeground
            val openCount = maxOf(1, (timeSpent / (5 * 60 * 1000)).toInt())

            AppUsageData(
                packageName = packageName,
                timeSpent = timeSpent,
                openCount = openCount
            )
        }.sortedByDescending { it.timeSpent }
    }

    /**
     * Get last time an app was used (timestamp)
     * Returns 0 if never used or permission not granted
     */
    fun getLastTimeUsed(context: Context, packageName: String): Long {
        if (!hasUsageStatsPermission(context)) {
            return 0
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Query last 7 days to find most recent usage
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L)

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var lastTimeUsed = 0L
        usageStatsList?.forEach { stats ->
            if (stats.packageName == packageName && stats.lastTimeUsed > lastTimeUsed) {
                lastTimeUsed = stats.lastTimeUsed
            }
        }

        return lastTimeUsed
    }

    /**
     * Get total screen time for today
     * Sum of all app foreground times
     */
    fun getTotalScreenTime(context: Context, resetHour: Int): Long {
        val todayStats = getTodayUsageStats(context, resetHour)
        return todayStats.values.sumOf { it.totalTimeInForeground }
    }
}
