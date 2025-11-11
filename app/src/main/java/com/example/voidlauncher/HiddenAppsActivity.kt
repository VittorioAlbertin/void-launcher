package com.example.voidlauncher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * Activity showing only hidden apps - for unhiding them
 */
class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var searchBar: EditText
    private lateinit var hiddenAppsRecyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private var allHiddenApps: List<App> = emptyList()
    private var filteredHiddenApps: List<App> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        searchBar = findViewById(R.id.searchBar)
        hiddenAppsRecyclerView = findViewById(R.id.hiddenAppsRecyclerView)

        // Apply font size scaling
        applyFontSizes()

        // Setup RecyclerView
        hiddenAppsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load hidden apps
        loadHiddenApps()

        // Setup search functionality
        setupSearch()
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
     * Load only hidden apps
     */
    private fun loadHiddenApps() {
        lifecycleScope.launch {
            // Load ALL apps including hidden ones
            val allApps = AppCache.loadAllAppsIncludingHidden(this@HiddenAppsActivity)
            val hiddenPackages = prefsManager.getHiddenApps()

            // Filter to show only hidden apps
            allHiddenApps = allApps.filter { hiddenPackages.contains(it.packageName) }
            filteredHiddenApps = allHiddenApps
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
    }

    /**
     * Filter apps based on search query
     */
    private fun filterApps(query: String) {
        filteredHiddenApps = if (query.isEmpty()) {
            allHiddenApps
        } else {
            allHiddenApps.filter { app ->
                app.label.contains(query, ignoreCase = true)
            }
        }
        updateAppList()
    }

    /**
     * Update the RecyclerView with current filtered apps
     */
    private fun updateAppList() {
        val fontSize = prefsManager.getFontSize()

        val adapter = AppAdapter(
            apps = filteredHiddenApps,
            fontSize = fontSize,
            onAppClick = { app -> showUnhideDialog(app) }, // Click to unhide
            onAppLongClick = { app -> showUnhideDialog(app) } // Long press also unhides
        )
        hiddenAppsRecyclerView.adapter = adapter
    }

    /**
     * Show dialog to confirm unhiding app
     */
    private fun showUnhideDialog(app: App) {
        AlertDialog.Builder(this)
            .setTitle("Unhide App")
            .setMessage("Show ${app.label} in All Apps again?")
            .setPositiveButton("Unhide") { _, _ ->
                unhideApp(app)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Unhide an app
     */
    private fun unhideApp(app: App) {
        val currentHiddenApps = prefsManager.getHiddenApps().toMutableSet()
        currentHiddenApps.remove(app.packageName)
        prefsManager.saveHiddenApps(currentHiddenApps)

        // Reload the list to reflect the change
        loadHiddenApps()
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
        // Reload apps in case hidden apps changed
        loadHiddenApps()
    }
}
