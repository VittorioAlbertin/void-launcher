package com.example.voidlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * All Apps screen showing full list of installed apps
 */
class AllAppsActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var searchBar: EditText
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private var allApps: List<App> = emptyList()
    private var filteredApps: List<App> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        // Hide system UI for minimal look
        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        searchBar = findViewById(R.id.searchBar)
        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)

        // Apply font size scaling
        applyFontSizes()

        // Setup RecyclerView
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load all installed apps
        loadAllApps()

        // Setup search functionality
        setupSearch()

        // Auto-focus search bar
        searchBar.requestFocus()
    }

    /**
     * Apply font size scaling to all text elements
     */
    private fun applyFontSizes() {
        val fontSize = prefsManager.getFontSize()

        // Base sizes from layout XML
        val headerBaseSize = 16f
        val searchBaseSize = 14f

        // Apply scaled sizes
        headerText.textSize = fontSize * headerBaseSize / 16f
        searchBar.textSize = fontSize * searchBaseSize / 16f
    }

    /**
     * Load all installed launchable apps (excluding hidden ones)
     */
    private fun loadAllApps() {
        // Check cache first for instant display
        val cachedApps = AppCache.getCachedApps()
        if (cachedApps != null) {
            // Use cached apps - instant display!
            allApps = cachedApps
            filteredApps = allApps
            updateAppList()
            return
        }

        // Fallback: load apps if cache is not available (shouldn't happen normally)
        lifecycleScope.launch {
            AppCache.loadAndCacheApps(this@AllAppsActivity, prefsManager)
            val apps = AppCache.getCachedApps() ?: emptyList()

            // Update UI on main thread
            allApps = apps
            filteredApps = allApps
            updateAppList()
        }
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

        // Handle Enter key press to launch first result
        searchBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                if (filteredApps.isNotEmpty()) {
                    launchApp(filteredApps[0])
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * Filter apps based on search query
     */
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            val matches = allApps.filter { app ->
                app.label.contains(query, ignoreCase = true)
            }

            // Sort by last opened time if 3 or fewer results
            if (matches.size <= 3) {
                matches.sortedByDescending { app ->
                    UsageTrackingManager.getLastTimeUsed(this, app.packageName)
                }
            } else {
                matches
            }
        }
        updateAppList()

        // Auto-open if exactly one result
        if (query.isNotEmpty() && filteredApps.size == 1) {
            // Small delay to allow user to see the match
            searchBar.postDelayed({
                if (filteredApps.size == 1) {
                    launchApp(filteredApps[0])
                }
            }, 300)
        }
    }

    /**
     * Update the RecyclerView with current filtered apps
     */
    private fun updateAppList() {
        val fontSize = prefsManager.getFontSize()

        val adapter = AppAdapter(
            apps = filteredApps,
            fontSize = fontSize,
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> showAddToHomepageDialog(app) }
        )
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
     * Show app stats dialog with pin/hide options
     */
    private fun showAddToHomepageDialog(app: App) {
        val isOnHomepage = prefsManager.isAppOnHomepage(app.packageName)
        val isHidden = prefsManager.isAppHidden(app.packageName)

        val dialog = AppStatsDialog(
            context = this,
            app = app,
            isOnHomepage = isOnHomepage,
            isHidden = isHidden,
            onPinToggle = { toggleHomepage(it) },
            onHideToggle = { toggleHidden(it) }
        )
        dialog.show()
    }

    /**
     * Toggle app on/off homepage
     */
    private fun toggleHomepage(app: App) {
        val currentHomepageApps = prefsManager.getHomepageApps().toMutableSet()
        if (currentHomepageApps.contains(app.packageName)) {
            currentHomepageApps.remove(app.packageName)
        } else {
            currentHomepageApps.add(app.packageName)
        }
        prefsManager.saveHomepageApps(currentHomepageApps)
    }

    /**
     * Toggle app hidden status
     */
    private fun toggleHidden(app: App) {
        val currentHiddenApps = prefsManager.getHiddenApps().toMutableSet()
        if (currentHiddenApps.contains(app.packageName)) {
            currentHiddenApps.remove(app.packageName)
        } else {
            currentHiddenApps.add(app.packageName)
        }
        prefsManager.saveHiddenApps(currentHiddenApps)

        // Reload apps to reflect the change
        loadAllApps()
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
        // Reload apps in case hidden apps changed (uses cache if available)
        loadAllApps()
    }
}
