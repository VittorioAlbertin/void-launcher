package com.example.voidlauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Manages automatic hiding of apps based on time schedules and usage limits
 */
object AutoHideManager {

    /**
     * Check if an app should be auto-hidden right now
     */
    fun shouldHideApp(context: Context, packageName: String, prefsManager: PreferencesManager): Boolean {
        val rulesJson = prefsManager.getAutoHideRules(packageName) ?: return false

        try {
            val rules = parseRules(rulesJson)

            // Check time-based rules
            if (rules.timeRules.any { isWithinTimeRange(it) }) {
                return true
            }

            // Check usage-based rules
            val usage = UsageTrackingManager.getAppUsage(context, packageName)

            if (rules.maxOpens > 0 && usage.openCount >= rules.maxOpens) {
                return true
            }

            if (rules.maxTimeMs > 0 && usage.timeSpent >= rules.maxTimeMs) {
                return true
            }

            return false
        } catch (e: Exception) {
            // Invalid JSON or parsing error, don't hide
            return false
        }
    }

    /**
     * Check if current time is within a time range
     */
    private fun isWithinTimeRange(rule: TimeRule): Boolean {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startMinutes = rule.startHour * 60 + rule.startMinute
        val endMinutes = rule.endHour * 60 + rule.endMinute

        return if (startMinutes <= endMinutes) {
            // Normal range (e.g., 8:30 - 12:30)
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnight range (e.g., 21:00 - 3:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    /**
     * Parse rules JSON into AutoHideRules object
     */
    fun parseRules(json: String): AutoHideRules {
        val obj = JSONObject(json)

        val timeRules = mutableListOf<TimeRule>()
        if (obj.has("timeRules")) {
            val array = obj.getJSONArray("timeRules")
            for (i in 0 until array.length()) {
                val ruleObj = array.getJSONObject(i)
                timeRules.add(TimeRule(
                    startHour = ruleObj.getInt("startHour"),
                    startMinute = ruleObj.getInt("startMinute"),
                    endHour = ruleObj.getInt("endHour"),
                    endMinute = ruleObj.getInt("endMinute")
                ))
            }
        }

        val maxOpens = obj.optInt("maxOpens", 0)
        val maxTimeMs = obj.optLong("maxTimeMs", 0)

        return AutoHideRules(timeRules, maxOpens, maxTimeMs)
    }

    /**
     * Convert AutoHideRules to JSON string
     */
    fun rulesToJson(rules: AutoHideRules): String {
        val obj = JSONObject()

        val timeArray = JSONArray()
        for (rule in rules.timeRules) {
            val ruleObj = JSONObject()
            ruleObj.put("startHour", rule.startHour)
            ruleObj.put("startMinute", rule.startMinute)
            ruleObj.put("endHour", rule.endHour)
            ruleObj.put("endMinute", rule.endMinute)
            timeArray.put(ruleObj)
        }
        obj.put("timeRules", timeArray)

        obj.put("maxOpens", rules.maxOpens)
        obj.put("maxTimeMs", rules.maxTimeMs)

        return obj.toString()
    }

    /**
     * Format time rule for display
     */
    fun formatTimeRule(rule: TimeRule): String {
        return "${formatTime(rule.startHour, rule.startMinute)} - ${formatTime(rule.endHour, rule.endMinute)}"
    }

    /**
     * Format hour and minute to HH:MM
     */
    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    /**
     * Get summary of rules for display
     */
    fun getRulesSummary(rules: AutoHideRules): String {
        val parts = mutableListOf<String>()

        if (rules.timeRules.isNotEmpty()) {
            parts.add("${rules.timeRules.size} time schedule${if (rules.timeRules.size > 1) "s" else ""}")
        }

        if (rules.maxOpens > 0) {
            parts.add("Max ${rules.maxOpens} opens/day")
        }

        if (rules.maxTimeMs > 0) {
            val minutes = (rules.maxTimeMs / 1000 / 60).toInt()
            parts.add("Max ${UsageTrackingManager.formatTime(rules.maxTimeMs)}/day")
        }

        return if (parts.isEmpty()) {
            "No rules"
        } else {
            parts.joinToString(", ")
        }
    }
}

/**
 * Data class for auto-hide rules
 */
data class AutoHideRules(
    val timeRules: List<TimeRule>,
    val maxOpens: Int,
    val maxTimeMs: Long
)

/**
 * Data class for a time-based hide rule
 */
data class TimeRule(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)
