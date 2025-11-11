package com.example.voidlauncher

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity for displaying screen time and app usage statistics
 */
class ScreenTimeActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var todayText: TextView
    private lateinit var totalScreenTimeText: TextView
    private lateinit var unlocksCountText: TextView
    private lateinit var appUsageRecyclerView: RecyclerView
    private lateinit var resetTimeLayout: LinearLayout
    private lateinit var resetTimeText: TextView
    private lateinit var historyButton: TextView
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_time)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        todayText = findViewById(R.id.todayText)
        totalScreenTimeText = findViewById(R.id.totalScreenTimeText)
        unlocksCountText = findViewById(R.id.unlocksCountText)
        appUsageRecyclerView = findViewById(R.id.appUsageRecyclerView)
        resetTimeLayout = findViewById(R.id.resetTimeLayout)
        resetTimeText = findViewById(R.id.resetTimeText)
        historyButton = findViewById(R.id.historyButton)

        // Apply font size scaling
        applyFontSizes()

        // Setup RecyclerView
        appUsageRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup reset time click handler
        resetTimeLayout.setOnClickListener {
            showResetTimeDialog()
        }

        // Setup history button click handler
        historyButton.setOnClickListener {
            showHistoryDialog()
        }

        // Check for usage stats permission
        if (!UsageStatsHelper.hasUsageStatsPermission(this)) {
            showPermissionDialog()
        } else {
            loadUsageStats()
        }
    }

    /**
     * Apply font size scaling to all text elements
     */
    private fun applyFontSizes() {
        val fontSize = prefsManager.getFontSize()

        // Base sizes from layout XML
        val headerBaseSize = 16f
        val todayBaseSize = 12f
        val statsBaseSize = 14f

        // Apply scaled sizes
        headerText.textSize = fontSize * headerBaseSize / 16f
        todayText.textSize = fontSize * todayBaseSize / 16f
        totalScreenTimeText.textSize = fontSize * statsBaseSize / 16f
        unlocksCountText.textSize = fontSize * statsBaseSize / 16f
        resetTimeText.textSize = fontSize * statsBaseSize / 16f
        historyButton.textSize = fontSize * statsBaseSize / 16f

        // Apply to static labels in statsCard and resetTimeLayout
        val statsCard = findViewById<LinearLayout>(R.id.statsCard)
        val appsUsageLabel = findViewById<TextView>(R.id.appsUsageLabel)

        // Screen Time label
        ((statsCard.getChildAt(0) as? LinearLayout)?.getChildAt(0) as? TextView)?.textSize =
            fontSize * statsBaseSize / 16f
        // Unlocks label
        ((statsCard.getChildAt(1) as? LinearLayout)?.getChildAt(0) as? TextView)?.textSize =
            fontSize * statsBaseSize / 16f
        // Apps Usage label
        appsUsageLabel?.textSize = fontSize * statsBaseSize / 16f
        // Reset Time label and [Change] text
        ((resetTimeLayout.getChildAt(0) as? TextView)?.textSize =
            fontSize * statsBaseSize / 16f)
        ((resetTimeLayout.getChildAt(2) as? TextView)?.textSize =
            fontSize * statsBaseSize / 16f)
    }

    /**
     * Show dialog explaining permission requirement
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("VoidLauncher needs access to usage statistics to display screen time and app usage data.\n\nThis is a system-level permission that requires manual approval in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                UsageStatsHelper.openUsageAccessSettings(this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Load and display all usage statistics
     */
    private fun loadUsageStats() {
        // Update reset time display
        val resetHour = UsageTrackingManager.getResetHour(this)
        updateResetTimeDisplay(resetHour)
        todayText.text = "Today (since ${formatHour(resetHour)})"

        // Get and display total screen time
        val screenTime = UsageTrackingManager.getTotalScreenTime(this)
        totalScreenTimeText.text = UsageTrackingManager.formatTime(screenTime)

        // Get and display unlock count
        val unlockCount = UsageTrackingManager.getScreenUnlockCount(this)
        unlocksCountText.text = unlockCount.toString()

        // Get and display app usage
        val appUsage = UsageTrackingManager.getAllAppUsage(this)
        val adapter = AppUsageAdapter(this, appUsage)
        appUsageRecyclerView.adapter = adapter
    }

    /**
     * Show dialog to change reset time
     */
    private fun showResetTimeDialog() {
        val currentHour = UsageTrackingManager.getResetHour(this)

        // Create EditText for hour input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(currentHour.toString())
        input.hint = "Enter hour (0-23)"
        input.setTextColor(resources.getColor(R.color.void_white, null))
        input.setHintTextColor(resources.getColor(R.color.void_white, null))

        // Create container with padding
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 20)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Reset Time")
            .setMessage("Enter hour (0-23) for daily reset")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                val inputText = input.text.toString()
                try {
                    val newHour = inputText.toInt()
                    if (newHour in 0..23) {
                        UsageTrackingManager.saveResetHour(this, newHour)
                        loadUsageStats()
                    }
                } catch (e: NumberFormatException) {
                    // Invalid input, ignore
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Update reset time display text
     */
    private fun updateResetTimeDisplay(hour: Int) {
        resetTimeText.text = formatHour(hour)
    }

    /**
     * Format hour to readable time (e.g., "3:00am", "15:00")
     */
    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00am"
            hour < 12 -> "${hour}:00am"
            hour == 12 -> "12:00pm"
            else -> "${hour - 12}:00pm"
        }
    }

    /**
     * Show history viewing dialog
     */
    private fun showHistoryDialog() {
        val options = arrayOf("Last 7 Days", "Last 4 Weeks", "Last 12 Months")

        // Create custom adapter with white text
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            options
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView?.setTextColor(resources.getColor(R.color.void_white, null))
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("View History")
            .setAdapter(adapter) { _, which ->
                when (which) {
                    0 -> showDailyHistory(7)
                    1 -> showWeeklyHistory(4)
                    2 -> showMonthlyHistory(12)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show daily history
     */
    private fun showDailyHistory(days: Int) {
        val history = ScreenTimeHistoryManager.getDailyHistory(this, days)

        if (history.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Daily History")
                .setMessage("No historical data available yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val message = buildString {
            appendLine("Last $days Days:\n")
            for (data in history) {
                val dateLabel = ScreenTimeHistoryManager.formatDateForDisplay(data.date)
                val timeLabel = UsageTrackingManager.formatTime(data.screenTime)
                appendLine("$dateLabel")
                appendLine("  Screen Time: $timeLabel")
                appendLine("  Unlocks: ${data.unlocks}")
                appendLine()
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Daily History")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Show weekly history
     */
    private fun showWeeklyHistory(weeks: Int) {
        val history = ScreenTimeHistoryManager.getWeeklySummaries(this, weeks)

        if (history.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Weekly History")
                .setMessage("No historical data available yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val message = buildString {
            appendLine("Last $weeks Weeks:\n")
            for (summary in history) {
                appendLine("${summary.weekLabel}")
                appendLine("  Total: ${UsageTrackingManager.formatTime(summary.totalScreenTime)}")
                appendLine("  Avg/day: ${UsageTrackingManager.formatTime(summary.avgScreenTime)}")
                appendLine("  Total unlocks: ${summary.totalUnlocks}")
                appendLine("  Avg unlocks/day: ${summary.avgUnlocks}")
                appendLine("  Days recorded: ${summary.daysCount}")
                appendLine()
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Weekly History")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Show monthly history
     */
    private fun showMonthlyHistory(months: Int) {
        val history = ScreenTimeHistoryManager.getMonthlySummaries(this, months)

        if (history.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Monthly History")
                .setMessage("No historical data available yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val message = buildString {
            appendLine("Last $months Months:\n")
            for (summary in history) {
                appendLine("${summary.monthLabel}")
                appendLine("  Total: ${UsageTrackingManager.formatTime(summary.totalScreenTime)}")
                appendLine("  Avg/day: ${UsageTrackingManager.formatTime(summary.avgScreenTime)}")
                appendLine("  Total unlocks: ${summary.totalUnlocks}")
                appendLine("  Avg unlocks/day: ${summary.avgUnlocks}")
                appendLine("  Days recorded: ${summary.daysCount}")
                appendLine()
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Monthly History")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Hide system UI bars for minimal void aesthetic
     */
    private fun hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        // Refresh stats when resuming (check permission again in case user granted it)
        if (UsageStatsHelper.hasUsageStatsPermission(this)) {
            loadUsageStats()
        }
    }
}
