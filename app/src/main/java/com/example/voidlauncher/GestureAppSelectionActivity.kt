package com.example.voidlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity for selecting an app for a specific gesture direction
 */
class GestureAppSelectionActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var direction: String

    companion object {
        const val EXTRA_DIRECTION = "direction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_app_selection)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Get direction from intent
        direction = intent.getStringExtra(EXTRA_DIRECTION) ?: "up"

        headerText = findViewById(R.id.headerText)
        recyclerView = findViewById(R.id.gestureAppRecyclerView)

        // Set header text based on direction
        headerText.text = "Select App for Swipe ${direction.capitalize()}"

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load all apps with "None" option
        val apps = loadAllApps()
        val fontSize = prefsManager.getFontSize()

        val adapter = AppAdapter(
            apps = apps,
            fontSize = fontSize,
            onAppClick = { app ->
                // Save selection and finish
                if (app.packageName == "none") {
                    // Disable gesture
                    prefsManager.saveGestureApp(direction, "")
                } else {
                    prefsManager.saveGestureApp(direction, app.packageName)
                }
                finish()
            }
        )
        recyclerView.adapter = adapter
    }

    /**
     * Load all installed apps plus "None" option
     */
    private fun loadAllApps(): List<App> {
        val pm = packageManager
        val apps = mutableListOf<App>()

        // Add "None (Disable)" option at the top
        apps.add(App("None (Disable)", "none", null))

        // Get all apps with a launcher intent
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in activities) {
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                apps.add(App(label, packageName, launchIntent))
            }
        }

        // Sort alphabetically (but keep "None" at the top)
        val noneOption = apps.first()
        val sortedApps = apps.drop(1).sortedBy { it.label.lowercase() }

        return listOf(noneOption) + sortedApps
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
