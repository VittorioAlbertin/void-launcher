package com.example.voidlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity for selecting an app for a specific gesture direction
 */
class GestureAppSelectionActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var searchBar: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var direction: String
    private var allApps: List<App> = emptyList()
    private var filteredApps: List<App> = emptyList()

    companion object {
        const val EXTRA_DIRECTION = "direction"
        const val ALL_APPS_IDENTIFIER = "com.voidlauncher.ALL_APPS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_app_selection)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        // Get direction from intent
        direction = intent.getStringExtra(EXTRA_DIRECTION) ?: "up"

        headerText = findViewById(R.id.headerText)
        searchBar = findViewById(R.id.searchBar)
        recyclerView = findViewById(R.id.gestureAppRecyclerView)

        // Set header text based on direction
        headerText.text = "Select App for Swipe ${direction.capitalize()}"

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load all apps with "None" option
        allApps = loadAllApps()
        filteredApps = allApps

        // Setup search functionality
        setupSearch()

        // Display apps
        updateAppList()
    }

    /**
     * Setup search bar functionality
     */
    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Filter apps based on search query
     */
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            // Keep "None" and "All Apps" options at the top, and filter the rest
            val noneOption = allApps.firstOrNull { it.packageName == "none" }
            val allAppsOption = allApps.firstOrNull { it.packageName == ALL_APPS_IDENTIFIER }
            val filteredList = allApps.filter { app ->
                app.packageName != "none" &&
                app.packageName != ALL_APPS_IDENTIFIER &&
                app.label.contains(query, ignoreCase = true)
            }

            val topOptions = listOfNotNull(noneOption, allAppsOption)
            topOptions + filteredList
        }
        updateAppList()
    }

    /**
     * Update the RecyclerView with current filtered apps
     */
    private fun updateAppList() {
        val fontSize = prefsManager.getFontSize()

        val adapter = AppAdapter(
            apps = filteredApps,
            fontSize = fontSize,
            onAppClick = { app ->
                // Save selection and finish
                when (app.packageName) {
                    "none" -> {
                        // Disable gesture
                        prefsManager.saveGestureApp(direction, "")
                    }
                    ALL_APPS_IDENTIFIER -> {
                        // Set to open All Apps
                        prefsManager.saveGestureApp(direction, ALL_APPS_IDENTIFIER)
                    }
                    else -> {
                        // Launch regular app
                        prefsManager.saveGestureApp(direction, app.packageName)
                    }
                }
                finish()
            }
        )
        recyclerView.adapter = adapter
    }

    /**
     * Load all installed apps plus "None" and "All Apps" options
     */
    private fun loadAllApps(): List<App> {
        val pm = packageManager
        val apps = mutableListOf<App>()

        // Add "None (Disable)" option at the top
        apps.add(App("None (Disable)", "none", null))

        // Add "All Apps" option
        apps.add(App("All Apps", ALL_APPS_IDENTIFIER, null))

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

        // Sort alphabetically (but keep "None" and "All Apps" at the top)
        val noneOption = apps[0]
        val allAppsOption = apps[1]
        val sortedApps = apps.drop(2).sortedBy { it.label.lowercase() }

        return listOf(noneOption, allAppsOption) + sortedApps
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
