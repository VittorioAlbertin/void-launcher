package com.example.voidlauncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

/**
 * Settings activity for VoidLauncher
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var homepageAppsOption: LinearLayout
    private lateinit var hiddenAppsOption: LinearLayout
    private lateinit var fontSizeRadioGroup: RadioGroup
    private lateinit var fontSmall: RadioButton
    private lateinit var fontMedium: RadioButton
    private lateinit var fontLarge: RadioButton
    private lateinit var fontXLarge: RadioButton

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Initialize views
        homepageAppsOption = findViewById(R.id.homepageAppsOption)
        hiddenAppsOption = findViewById(R.id.hiddenAppsOption)
        fontSizeRadioGroup = findViewById(R.id.fontSizeRadioGroup)
        fontSmall = findViewById(R.id.fontSmall)
        fontMedium = findViewById(R.id.fontMedium)
        fontLarge = findViewById(R.id.fontLarge)
        fontXLarge = findViewById(R.id.fontXLarge)

        // Setup click listeners
        homepageAppsOption.setOnClickListener {
            openAppSelection(AppSelectionActivity.MODE_HOMEPAGE)
        }

        hiddenAppsOption.setOnClickListener {
            openAppSelection(AppSelectionActivity.MODE_HIDDEN)
        }

        // Load current font size
        loadFontSizeSelection()

        // Setup font size change listener
        fontSizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val fontSize = when (checkedId) {
                R.id.fontSmall -> 14f
                R.id.fontLarge -> 18f
                R.id.fontXLarge -> 20f
                else -> 16f // fontMedium
            }
            prefsManager.saveFontSize(fontSize)
        }
    }

    /**
     * Load and set the current font size selection
     */
    private fun loadFontSizeSelection() {
        val currentFontSize = prefsManager.getFontSize()
        when (currentFontSize) {
            14f -> fontSmall.isChecked = true
            18f -> fontLarge.isChecked = true
            20f -> fontXLarge.isChecked = true
            else -> fontMedium.isChecked = true
        }
    }

    /**
     * Open app selection activity
     */
    private fun openAppSelection(mode: String) {
        val intent = Intent(this, AppSelectionActivity::class.java)
        intent.putExtra(AppSelectionActivity.EXTRA_SELECTION_MODE, mode)
        startActivity(intent)
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
        // Reload font size in case it was changed
        loadFontSizeSelection()
    }
}
