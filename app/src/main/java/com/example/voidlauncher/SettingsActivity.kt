package com.example.voidlauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Settings activity for VoidLauncher
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var hiddenAppsOption: LinearLayout
    private lateinit var autoHideOption: LinearLayout
    private lateinit var fontSizeOption: LinearLayout
    private lateinit var clockSettingsOption: LinearLayout
    private lateinit var gesturesOption: LinearLayout
    private lateinit var screenTimeOption: LinearLayout
    private lateinit var defaultLauncherOption: LinearLayout

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Initialize views
        hiddenAppsOption = findViewById(R.id.hiddenAppsOption)
        autoHideOption = findViewById(R.id.autoHideOption)
        fontSizeOption = findViewById(R.id.fontSizeOption)
        clockSettingsOption = findViewById(R.id.clockSettingsOption)
        gesturesOption = findViewById(R.id.gesturesOption)
        screenTimeOption = findViewById(R.id.screenTimeOption)
        defaultLauncherOption = findViewById(R.id.defaultLauncherOption)

        // Apply font size scaling
        applyFontSizes()

        // Setup click listeners
        hiddenAppsOption.setOnClickListener {
            openHiddenApps()
        }

        autoHideOption.setOnClickListener {
            openAutoHideSettings()
        }

        fontSizeOption.setOnClickListener {
            showFontSizeDialog()
        }

        clockSettingsOption.setOnClickListener {
            showClockSettingsDialog()
        }

        gesturesOption.setOnClickListener {
            openGestures()
        }

        screenTimeOption.setOnClickListener {
            openScreenTime()
        }

        defaultLauncherOption.setOnClickListener {
            openDefaultLauncherSettings()
        }
    }

    /**
     * Apply font size scaling to all text elements
     */
    private fun applyFontSizes() {
        val fontSize = prefsManager.getFontSize()

        // Base sizes from layout XML
        val headerBaseSize = 16f
        val titleBaseSize = 16f
        val descBaseSize = 12f

        // Apply to header
        val headerText = findViewById<TextView>(R.id.headerText) ?:
            // Header is the first TextView in the layout
            (findViewById<ViewGroup>(android.R.id.content)
                .getChildAt(0) as? LinearLayout)
                ?.getChildAt(0) as? TextView

        headerText?.textSize = fontSize * headerBaseSize / 16f

        // Apply to all option sections
        val options = listOf(
            fontSizeOption, clockSettingsOption, gesturesOption, screenTimeOption,
            autoHideOption, hiddenAppsOption, defaultLauncherOption
        )

        options.forEach { option ->
            // First TextView is title, second is description
            val titleView = option.getChildAt(0) as? TextView
            val descView = option.getChildAt(1) as? TextView

            titleView?.textSize = fontSize * titleBaseSize / 16f
            descView?.textSize = fontSize * descBaseSize / 16f
        }
    }

    /**
     * Open hidden apps activity
     */
    private fun openHiddenApps() {
        val intent = Intent(this, HiddenAppsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Open auto-hide settings activity
     */
    private fun openAutoHideSettings() {
        val intent = Intent(this, AutoHideSettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Open gestures configuration activity
     */
    private fun openGestures() {
        val intent = Intent(this, GesturesActivity::class.java)
        startActivity(intent)
    }

    /**
     * Open screen time activity
     */
    private fun openScreenTime() {
        val intent = Intent(this, ScreenTimeActivity::class.java)
        startActivity(intent)
    }

    /**
     * Open system settings to set default launcher
     */
    private fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: Some devices might not support ACTION_HOME_SETTINGS
            // In that case, just open general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }

    /**
     * Show clock settings dialog
     */
    private fun showClockSettingsDialog() {
        val options = arrayOf("Clock Size", "12h/24h Format", "Show/Hide Date", "Day of Week", "Clock App")

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            options
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(resources.getColor(R.color.void_white, null))
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Clock Settings")
            .setAdapter(adapter) { _, which ->
                when (which) {
                    0 -> showClockSizeDialog()
                    1 -> showClockFormatDialog()
                    2 -> showDateToggleDialog()
                    3 -> showDayOfWeekToggleDialog()
                    4 -> showClockAppSelectionDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show clock size selection dialog
     */
    private fun showClockSizeDialog() {
        val currentSize = prefsManager.getClockSize()
        val sizes = arrayOf("Small (18sp)", "Medium (24sp)", "Large (32sp)", "Extra Large (40sp)", "Custom...")
        val sizeValues = arrayOf(18f, 24f, 32f, 40f, -1f)

        val currentIndex = sizeValues.indexOf(currentSize).let { if (it == -1) 4 else it }
        var selectedPosition = currentIndex

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.dialog_single_choice_item,
            sizes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(
                    R.layout.dialog_single_choice_item,
                    parent,
                    false
                )

                val textView = view.findViewById<TextView>(android.R.id.text1)
                val radioButton = view.findViewById<RadioButton>(R.id.radio_button)

                textView?.text = getItem(position)
                textView?.setTextColor(Color.WHITE)
                radioButton?.isChecked = (position == selectedPosition)

                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Clock Size")
            .setSingleChoiceItems(adapter, currentIndex) { dialogInterface, which ->
                selectedPosition = which
                adapter.notifyDataSetChanged()

                window.decorView.postDelayed({
                    if (which == 4) {
                        dialogInterface.dismiss()
                        showCustomClockSizeDialog()
                    } else {
                        prefsManager.saveClockSize(sizeValues[which])
                        dialogInterface.dismiss()
                    }
                }, 150)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show custom clock size input dialog
     */
    private fun showCustomClockSizeDialog() {
        val currentSize = prefsManager.getClockSize()

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(currentSize.toInt().toString())
        input.hint = "Enter size (12-60sp)"
        input.setTextColor(resources.getColor(R.color.void_white, null))
        input.setHintTextColor(resources.getColor(R.color.void_white, null))

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
            .setTitle("Custom Clock Size")
            .setMessage("Enter clock size in sp (12-60)")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                val inputText = input.text.toString()
                try {
                    val customSize = inputText.toFloat()
                    if (customSize in 12f..60f) {
                        prefsManager.saveClockSize(customSize)
                    }
                } catch (e: NumberFormatException) {
                    // Invalid input, ignore
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show 12h/24h format toggle dialog
     */
    private fun showClockFormatDialog() {
        val use24h = prefsManager.getClock24h()

        AlertDialog.Builder(this)
            .setTitle("Clock Format")
            .setMessage("Current format: ${if (use24h) "24h" else "12h"}")
            .setPositiveButton("Use ${if (use24h) "12h" else "24h"}") { _, _ ->
                prefsManager.saveClock24h(!use24h)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show date toggle dialog
     */
    private fun showDateToggleDialog() {
        val showDate = prefsManager.getShowDate()

        AlertDialog.Builder(this)
            .setTitle("Date Display")
            .setMessage("Show date under clock?")
            .setPositiveButton(if (showDate) "Hide Date" else "Show Date") { _, _ ->
                prefsManager.saveShowDate(!showDate)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show font size selection dialog
     */
    private fun showFontSizeDialog() {
        val currentFontSize = prefsManager.getFontSize()
        val sizes = arrayOf("Small (14sp)", "Medium (16sp)", "Large (18sp)", "Extra Large (20sp)", "Custom...")
        val sizeValues = arrayOf(14f, 16f, 18f, 20f, -1f) // -1 indicates custom

        // Find current selection index (defaults to custom if not in preset list)
        val currentIndex = sizeValues.indexOf(currentFontSize).let { if (it == -1) 4 else it }

        // Variable to track selected position
        var selectedPosition = currentIndex

        // Create custom adapter with white text and radio button on left
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.dialog_single_choice_item,
            sizes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(
                    R.layout.dialog_single_choice_item,
                    parent,
                    false
                )

                val textView = view.findViewById<TextView>(android.R.id.text1)
                val radioButton = view.findViewById<RadioButton>(R.id.radio_button)

                textView?.text = getItem(position)
                textView?.setTextColor(Color.WHITE)
                radioButton?.isChecked = (position == selectedPosition)

                return view
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Font Size")
            .setSingleChoiceItems(adapter, currentIndex) { dialogInterface, which ->
                selectedPosition = which
                adapter.notifyDataSetChanged() // Refresh to update radio buttons

                // Delay action slightly to show selection change
                window.decorView.postDelayed({
                    if (which == 4) {
                        // Custom option selected
                        dialogInterface.dismiss()
                        showCustomFontSizeDialog()
                    } else {
                        prefsManager.saveFontSize(sizeValues[which])
                        dialogInterface.dismiss()
                    }
                }, 150)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    /**
     * Show custom font size input dialog
     */
    private fun showCustomFontSizeDialog() {
        val currentFontSize = prefsManager.getFontSize()

        // Create EditText for input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(currentFontSize.toInt().toString())
        input.hint = "Enter size (8-32sp)"
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
            .setTitle("Custom Font Size")
            .setMessage("Enter font size in sp (8-32)")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                val inputText = input.text.toString()
                try {
                    val customSize = inputText.toFloat()
                    // Validate range
                    if (customSize in 8f..32f) {
                        prefsManager.saveFontSize(customSize)
                    } else {
                        // Invalid range, show error or revert
                        showFontSizeDialog()
                    }
                } catch (e: NumberFormatException) {
                    // Invalid input, show error or revert
                    showFontSizeDialog()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Return to main font size dialog
                showFontSizeDialog()
            }
            .show()
    }

    /**
     * Show day of week toggle dialog
     */
    private fun showDayOfWeekToggleDialog() {
        val showDayOfWeek = prefsManager.getShowDayOfWeek()

        AlertDialog.Builder(this)
            .setTitle("Day of Week")
            .setMessage("Show day of week with clock?")
            .setPositiveButton(if (showDayOfWeek) "Hide Day of Week" else "Show Day of Week") { _, _ ->
                prefsManager.saveShowDayOfWeek(!showDayOfWeek)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Show clock app selection dialog
     */
    private fun showClockAppSelectionDialog() {
        val pm = packageManager

        // Get all clock/calendar apps
        val clockIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
        clockIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)

        val allApps = pm.queryIntentActivities(clockIntent, 0)
        val clockApps = allApps.filter { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString().lowercase()
            label.contains("clock") || label.contains("calendar") || label.contains("time")
        }.map { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            Pair(label, packageName)
        }.sortedBy { it.first }

        if (clockApps.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Clock App")
                .setMessage("No clock apps found. Would you like to use the default system clock app?")
                .setPositiveButton("Use Default") { _, _ ->
                    prefsManager.saveClockApp(null)
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        val currentClockApp = prefsManager.getClockApp()
        val appLabels = arrayOf("Default System Clock") + clockApps.map { it.first }.toTypedArray()
        val currentIndex = if (currentClockApp == null) {
            0
        } else {
            clockApps.indexOfFirst { it.second == currentClockApp } + 1
        }

        // Create custom adapter with white text
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_single_choice,
            appLabels
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView?.setTextColor(resources.getColor(R.color.void_white, null))
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Select Clock App")
            .setSingleChoiceItems(adapter, currentIndex) { dialog, which ->
                if (which == 0) {
                    prefsManager.saveClockApp(null)
                } else {
                    prefsManager.saveClockApp(clockApps[which - 1].second)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
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
    }
}
