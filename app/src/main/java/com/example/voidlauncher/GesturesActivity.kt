package com.example.voidlauncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity for configuring gesture shortcuts
 */
class GesturesActivity : AppCompatActivity() {

    private lateinit var gestureUpOption: LinearLayout
    private lateinit var gestureDownOption: LinearLayout
    private lateinit var gestureLeftOption: LinearLayout
    private lateinit var gestureRightOption: LinearLayout
    private lateinit var gestureUpValue: TextView
    private lateinit var gestureDownValue: TextView
    private lateinit var gestureLeftValue: TextView
    private lateinit var gestureRightValue: TextView

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestures)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Initialize views
        gestureUpOption = findViewById(R.id.gestureUpOption)
        gestureDownOption = findViewById(R.id.gestureDownOption)
        gestureLeftOption = findViewById(R.id.gestureLeftOption)
        gestureRightOption = findViewById(R.id.gestureRightOption)
        gestureUpValue = findViewById(R.id.gestureUpValue)
        gestureDownValue = findViewById(R.id.gestureDownValue)
        gestureLeftValue = findViewById(R.id.gestureLeftValue)
        gestureRightValue = findViewById(R.id.gestureRightValue)

        // Setup click listeners
        gestureUpOption.setOnClickListener {
            openGestureSelection("up")
        }

        gestureDownOption.setOnClickListener {
            openGestureSelection("down")
        }

        gestureLeftOption.setOnClickListener {
            openGestureSelection("left")
        }

        gestureRightOption.setOnClickListener {
            openGestureSelection("right")
        }

        // Load current gesture settings
        loadGestureSettings()
    }

    /**
     * Open gesture app selection activity
     */
    private fun openGestureSelection(direction: String) {
        val intent = Intent(this, GestureAppSelectionActivity::class.java)
        intent.putExtra(GestureAppSelectionActivity.EXTRA_DIRECTION, direction)
        startActivity(intent)
    }

    /**
     * Load and display current gesture settings
     */
    private fun loadGestureSettings() {
        gestureUpValue.text = getGestureAppName("up")
        gestureDownValue.text = getGestureAppName("down")
        gestureLeftValue.text = getGestureAppName("left")
        gestureRightValue.text = getGestureAppName("right")
    }

    /**
     * Get app name for a gesture direction
     */
    private fun getGestureAppName(direction: String): String {
        val packageName = prefsManager.getGestureApp(direction)
        return if (packageName == null) {
            "Not Set"
        } else {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                "Not Set"
            }
        }
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
        // Reload gesture settings in case they were changed
        loadGestureSettings()
    }
}
