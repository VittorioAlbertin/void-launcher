package com.example.voidlauncher

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView

/**
 * Dialog for displaying app statistics and actions
 */
class AppStatsDialog(
    context: Context,
    private val app: App,
    private val isOnHomepage: Boolean,
    private val isHidden: Boolean,
    private val onPinToggle: (App) -> Unit,
    private val onHideToggle: (App) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_app_stats)

        // Set dialog background to match app theme
        window?.setBackgroundDrawableResource(R.color.void_black)

        val appNameText: TextView = findViewById(R.id.appNameText)
        val usageStatsText: TextView = findViewById(R.id.usageStatsText)
        val pinButton: Button = findViewById(R.id.pinButton)
        val hideButton: Button = findViewById(R.id.hideButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        // Set app name
        appNameText.text = app.label

        // Get and display usage stats
        val usage = UsageTrackingManager.getAppUsage(context, app.packageName)
        val timeFormatted = UsageTrackingManager.formatTime(usage.timeSpent)
        val opensText = "${usage.openCount} open${if (usage.openCount == 1) "" else "s"}"

        if (usage.timeSpent > 0 || usage.openCount > 0) {
            usageStatsText.text = "Today's Usage:\n$timeFormatted • $opensText"
        } else {
            usageStatsText.text = "Today's Usage:\nNo usage today"
        }

        // Set button text based on current state
        pinButton.text = if (isOnHomepage) "Unpin from Homepage" else "Pin to Homepage"
        hideButton.text = if (isHidden) "Show App" else "Hide App"

        // Setup button click handlers
        pinButton.setOnClickListener {
            onPinToggle(app)
            dismiss()
        }

        hideButton.setOnClickListener {
            onHideToggle(app)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
