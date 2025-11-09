package com.example.voidlauncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Main launcher screen showing curated list of essential apps
 */
class MainActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var appRecyclerView: RecyclerView
    private lateinit var allAppsText: TextView
    private lateinit var settingsText: TextView
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide system UI for minimal look
        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        appRecyclerView = findViewById(R.id.appRecyclerView)
        allAppsText = findViewById(R.id.allAppsText)
        settingsText = findViewById(R.id.settingsText)

        // Setup RecyclerView
        appRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup "All Apps >" click handler
        allAppsText.setOnClickListener {
            val intent = Intent(this, AllAppsActivity::class.java)
            startActivity(intent)
        }

        // Setup "Settings" click handler
        settingsText.setOnClickListener {
            openSettings()
        }

        // Load homepage apps
        loadHomepageApps()
    }

    /**
     * Load homepage apps from preferences
     */
    private fun loadHomepageApps() {
        val pm = packageManager
        val apps = mutableListOf<App>()
        val homepagePackages = prefsManager.getHomepageApps()

        for (packageName in homepagePackages) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val launchIntent = pm.getLaunchIntentForPackage(packageName)

                if (launchIntent != null) {
                    apps.add(App(label, packageName, launchIntent))
                }
            } catch (e: Exception) {
                // App not installed, skip it
            }
        }

        // Sort apps alphabetically
        val sortedApps = apps.sortedBy { it.label.lowercase() }

        // Get font size from preferences
        val fontSize = prefsManager.getFontSize()

        // Update adapter
        val adapter = AppAdapter(sortedApps, fontSize) { app ->
            launchApp(app)
        }
        appRecyclerView.adapter = adapter
    }

    /**
     * Launch an app
     */
    private fun launchApp(app: App) {
        app.launchIntent?.let {
            try {
                startActivity(it)
            } catch (e: Exception) {
                // Failed to launch app
            }
        }
    }

    /**
     * Open settings activity
     */
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
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
        // Re-hide system UI when returning from other apps
        hideSystemUI()
        // Reload apps in case settings changed
        loadHomepageApps()
    }
}
