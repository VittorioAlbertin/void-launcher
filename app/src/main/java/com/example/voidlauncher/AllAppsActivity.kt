package com.example.voidlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * All Apps screen showing full list of installed apps
 */
class AllAppsActivity : AppCompatActivity() {

    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        // Hide system UI for minimal look
        hideSystemUI()

        prefsManager = PreferencesManager(this)

        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)

        // Setup RecyclerView
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load all installed apps
        loadAllApps()
    }

    /**
     * Load all installed launchable apps (excluding hidden ones)
     */
    private fun loadAllApps() {
        val pm = packageManager
        val apps = mutableListOf<App>()
        val hiddenPackages = prefsManager.getHiddenApps()

        // Get all apps with a launcher intent
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (resolveInfo in activities) {
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName

            // Skip hidden apps
            if (hiddenPackages.contains(packageName)) {
                continue
            }

            val launchIntent = pm.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                apps.add(App(label, packageName, launchIntent))
            }
        }

        // Sort alphabetically by label
        val sortedApps = apps.sortedBy { it.label.lowercase() }

        // Get font size from preferences
        val fontSize = prefsManager.getFontSize()

        // Update adapter
        val adapter = AppAdapter(sortedApps, fontSize) { app ->
            launchApp(app)
        }
        allAppsRecyclerView.adapter = adapter
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
        // Reload apps in case hidden apps changed
        loadAllApps()
    }
}
