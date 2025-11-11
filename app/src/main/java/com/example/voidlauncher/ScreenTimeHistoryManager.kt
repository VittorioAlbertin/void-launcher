package com.example.voidlauncher

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Manages historical screen time data storage and retrieval
 */
object ScreenTimeHistoryManager {

    private const val PREFS_NAME = "screen_time_history"
    private const val KEY_DAILY_HISTORY = "daily_history"
    private const val MAX_DAYS_STORED = 365 // Store up to 1 year

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save today's data before reset
     */
    fun saveDailyData(
        context: Context,
        date: String,
        totalScreenTime: Long,
        unlockCount: Int,
        appUsage: List<AppUsageData>
    ) {
        val prefs = getPrefs(context)
        val historyJson = prefs.getString(KEY_DAILY_HISTORY, "[]") ?: "[]"
        val historyArray = JSONArray(historyJson)

        // Create today's entry
        val entry = JSONObject()
        entry.put("date", date)
        entry.put("screenTime", totalScreenTime)
        entry.put("unlocks", unlockCount)

        // Add app usage data
        val appsArray = JSONArray()
        for (usage in appUsage) {
            val appObj = JSONObject()
            appObj.put("package", usage.packageName)
            appObj.put("time", usage.timeSpent)
            appObj.put("opens", usage.openCount)
            appsArray.put(appObj)
        }
        entry.put("apps", appsArray)

        // Add to history
        historyArray.put(entry)

        // Keep only last MAX_DAYS_STORED days
        val trimmedArray = JSONArray()
        val startIndex = maxOf(0, historyArray.length() - MAX_DAYS_STORED)
        for (i in startIndex until historyArray.length()) {
            trimmedArray.put(historyArray.getJSONObject(i))
        }

        // Save
        prefs.edit().putString(KEY_DAILY_HISTORY, trimmedArray.toString()).apply()
    }

    /**
     * Get daily history for last N days
     */
    fun getDailyHistory(context: Context, days: Int): List<DailyData> {
        val prefs = getPrefs(context)
        val historyJson = prefs.getString(KEY_DAILY_HISTORY, "[]") ?: "[]"
        val historyArray = JSONArray(historyJson)

        val result = mutableListOf<DailyData>()
        val startIndex = maxOf(0, historyArray.length() - days)

        for (i in startIndex until historyArray.length()) {
            try {
                val entry = historyArray.getJSONObject(i)
                val date = entry.getString("date")
                val screenTime = entry.getLong("screenTime")
                val unlocks = entry.getInt("unlocks")

                val apps = mutableListOf<AppUsageData>()
                if (entry.has("apps")) {
                    val appsArray = entry.getJSONArray("apps")
                    for (j in 0 until appsArray.length()) {
                        val appObj = appsArray.getJSONObject(j)
                        apps.add(AppUsageData(
                            packageName = appObj.getString("package"),
                            timeSpent = appObj.getLong("time"),
                            openCount = appObj.getInt("opens")
                        ))
                    }
                }

                result.add(DailyData(date, screenTime, unlocks, apps))
            } catch (e: Exception) {
                // Skip malformed entries
            }
        }

        return result
    }

    /**
     * Get weekly summaries
     */
    fun getWeeklySummaries(context: Context, weeks: Int): List<WeeklySummary> {
        val dailyData = getDailyHistory(context, weeks * 7)
        if (dailyData.isEmpty()) return emptyList()

        val summaries = mutableListOf<WeeklySummary>()
        val calendar = Calendar.getInstance()

        // Group by week
        val weekMap = mutableMapOf<String, MutableList<DailyData>>()
        for (data in dailyData) {
            try {
                val date = parseDate(data.date)
                calendar.time = date
                val weekYear = calendar.get(Calendar.YEAR)
                val weekNum = calendar.get(Calendar.WEEK_OF_YEAR)
                val weekKey = "$weekYear-W$weekNum"

                weekMap.getOrPut(weekKey) { mutableListOf() }.add(data)
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }

        // Create summaries
        for ((weekKey, days) in weekMap.entries.sortedBy { it.key }) {
            val totalScreenTime = days.sumOf { it.screenTime }
            val totalUnlocks = days.sumOf { it.unlocks }
            val avgScreenTime = if (days.isNotEmpty()) totalScreenTime / days.size else 0
            val avgUnlocks = if (days.isNotEmpty()) totalUnlocks / days.size else 0

            summaries.add(WeeklySummary(
                weekLabel = weekKey,
                totalScreenTime = totalScreenTime,
                avgScreenTime = avgScreenTime,
                totalUnlocks = totalUnlocks,
                avgUnlocks = avgUnlocks,
                daysCount = days.size
            ))
        }

        return summaries.takeLast(weeks)
    }

    /**
     * Get monthly summaries
     */
    fun getMonthlySummaries(context: Context, months: Int): List<MonthlySummary> {
        val dailyData = getDailyHistory(context, months * 31)
        if (dailyData.isEmpty()) return emptyList()

        val summaries = mutableListOf<MonthlySummary>()
        val calendar = Calendar.getInstance()

        // Group by month
        val monthMap = mutableMapOf<String, MutableList<DailyData>>()
        for (data in dailyData) {
            try {
                val date = parseDate(data.date)
                calendar.time = date
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val monthKey = "$year-${String.format("%02d", month + 1)}"

                monthMap.getOrPut(monthKey) { mutableListOf() }.add(data)
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }

        // Create summaries
        for ((monthKey, days) in monthMap.entries.sortedBy { it.key }) {
            val totalScreenTime = days.sumOf { it.screenTime }
            val totalUnlocks = days.sumOf { it.unlocks }
            val avgScreenTime = if (days.isNotEmpty()) totalScreenTime / days.size else 0
            val avgUnlocks = if (days.isNotEmpty()) totalUnlocks / days.size else 0

            // Format month label (e.g., "Jan 2025")
            val parts = monthKey.split("-")
            val monthLabel = if (parts.size == 2) {
                try {
                    val monthNum = parts[1].toInt() - 1
                    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    "${monthNames[monthNum]} ${parts[0]}"
                } catch (e: Exception) {
                    monthKey
                }
            } else {
                monthKey
            }

            summaries.add(MonthlySummary(
                monthLabel = monthLabel,
                totalScreenTime = totalScreenTime,
                avgScreenTime = avgScreenTime,
                totalUnlocks = totalUnlocks,
                avgUnlocks = avgUnlocks,
                daysCount = days.size
            ))
        }

        return summaries.takeLast(months)
    }

    /**
     * Get today's date in YYYY-MM-DD format
     */
    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }

    /**
     * Parse date string to Date object
     */
    private fun parseDate(dateStr: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.parse(dateStr) ?: Date()
    }

    /**
     * Format date for display (e.g., "Mon, Jan 15")
     */
    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val date = parseDate(dateStr)
            val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            displayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}

/**
 * Data class for daily screen time data
 */
data class DailyData(
    val date: String,
    val screenTime: Long,
    val unlocks: Int,
    val appUsage: List<AppUsageData>
)

/**
 * Data class for weekly summary
 */
data class WeeklySummary(
    val weekLabel: String,
    val totalScreenTime: Long,
    val avgScreenTime: Long,
    val totalUnlocks: Int,
    val avgUnlocks: Int,
    val daysCount: Int
)

/**
 * Data class for monthly summary
 */
data class MonthlySummary(
    val monthLabel: String,
    val totalScreenTime: Long,
    val avgScreenTime: Long,
    val totalUnlocks: Int,
    val avgUnlocks: Int,
    val daysCount: Int
)
