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
    private lateinit var fontSizeOption: LinearLayout
    private lateinit var gesturesOption: LinearLayout
    private lateinit var defaultLauncherOption: LinearLayout

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Initialize views
        hiddenAppsOption = findViewById(R.id.hiddenAppsOption)
        fontSizeOption = findViewById(R.id.fontSizeOption)
        gesturesOption = findViewById(R.id.gesturesOption)
        defaultLauncherOption = findViewById(R.id.defaultLauncherOption)

        // Setup click listeners
        hiddenAppsOption.setOnClickListener {
            openAppSelection(AppSelectionActivity.MODE_HIDDEN)
        }

        fontSizeOption.setOnClickListener {
            showFontSizeDialog()
        }

        gesturesOption.setOnClickListener {
            openGestures()
        }

        defaultLauncherOption.setOnClickListener {
            openDefaultLauncherSettings()
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
     * Open gestures configuration activity
     */
    private fun openGestures() {
        val intent = Intent(this, GesturesActivity::class.java)
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
