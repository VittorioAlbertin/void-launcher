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
 * Activity for selecting apps (either for homepage or to hide)
 */
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var adapter: SelectableAppAdapter

    private var selectionMode: SelectionMode = SelectionMode.HOMEPAGE

    enum class SelectionMode {
        HOMEPAGE,
        HIDDEN
    }

    companion object {
        const val EXTRA_SELECTION_MODE = "selection_mode"
        const val MODE_HOMEPAGE = "homepage"
        const val MODE_HIDDEN = "hidden"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        recyclerView = findViewById(R.id.appSelectionRecyclerView)

        // Determine selection mode from intent
        val mode = intent.getStringExtra(EXTRA_SELECTION_MODE)
        selectionMode = when (mode) {
            MODE_HIDDEN -> SelectionMode.HIDDEN
            else -> SelectionMode.HOMEPAGE
        }

        // Set header text based on mode
        headerText.text = when (selectionMode) {
            SelectionMode.HOMEPAGE -> getString(R.string.select_homepage_apps)
            SelectionMode.HIDDEN -> getString(R.string.select_hidden_apps)
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load all apps with selection state
        val selectableApps = loadSelectableApps()
        adapter = SelectableAppAdapter(selectableApps)
        recyclerView.adapter = adapter
    }

    /**
     * Load all installed apps with their selection state
     */
    private fun loadSelectableApps(): List<SelectableApp> {
        val pm = packageManager
        val allApps = mutableListOf<App>()

        // Get all apps with a launcher intent
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (resolveInfo in activities) {
            val label = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                allApps.add(App(label, packageName, launchIntent))
            }
        }

        // Sort alphabetically
        val sortedApps = allApps.sortedBy { it.label.lowercase() }

        // Get currently selected package names based on mode
        val selectedPackages = when (selectionMode) {
            SelectionMode.HOMEPAGE -> prefsManager.getHomepageApps()
            SelectionMode.HIDDEN -> prefsManager.getHiddenApps()
        }

        // Create SelectableApp objects
        return sortedApps.map { app ->
            SelectableApp(
                app = app,
                isSelected = selectedPackages.contains(app.packageName)
            )
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
    }

    override fun onPause() {
        super.onPause()
        // Save selections when leaving the activity
        saveSelections()
    }

    /**
     * Save the current selections to preferences
     */
    private fun saveSelections() {
        val selectedApps = adapter.getSelectedApps()
        val packageNames = selectedApps.map { it.packageName }.toSet()

        when (selectionMode) {
            SelectionMode.HOMEPAGE -> prefsManager.saveHomepageApps(packageNames)
            SelectionMode.HIDDEN -> prefsManager.saveHiddenApps(packageNames)
        }
    }
}
