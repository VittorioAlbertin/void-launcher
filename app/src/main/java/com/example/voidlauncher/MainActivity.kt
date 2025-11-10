package com.example.voidlauncher

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import android.widget.TextClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Main launcher screen showing curated list of essential apps
 */
class MainActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var digitalClock: TextClock
    private lateinit var appRecyclerView: RecyclerView
    private lateinit var allAppsText: TextView
    private lateinit var settingsText: TextView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide system UI for minimal look
        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        digitalClock = findViewById(R.id.digitalClock)
        appRecyclerView = findViewById(R.id.appRecyclerView)
        allAppsText = findViewById(R.id.allAppsText)
        settingsText = findViewById(R.id.settingsText)

        // Setup gesture detector
        gestureDetector = GestureDetector(this, GestureListener())

        // Setup RecyclerView
        appRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup gesture detection on clock and app list
        setupGestureDetection()

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

        // Preload all apps in background for instant "all apps" display
        preloadAllApps()
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
        val adapter = AppAdapter(
            apps = sortedApps,
            fontSize = fontSize,
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> showRemoveFromHomepageDialog(app) }
        )
        appRecyclerView.adapter = adapter
    }

    /**
     * Preload all apps in background for instant "all apps" display
     */
    private fun preloadAllApps() {
        lifecycleScope.launch {
            AppCache.loadAndCacheApps(this@MainActivity, prefsManager)
        }
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
     * Show dialog to confirm removing app from homepage
     */
    private fun showRemoveFromHomepageDialog(app: App) {
        AlertDialog.Builder(this)
            .setTitle("Remove from Homepage")
            .setMessage("Remove ${app.label} from homepage?")
            .setPositiveButton("Remove") { _, _ ->
                removeFromHomepage(app)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Remove an app from the homepage
     */
    private fun removeFromHomepage(app: App) {
        val currentHomepageApps = prefsManager.getHomepageApps().toMutableSet()
        currentHomepageApps.remove(app.packageName)
        prefsManager.saveHomepageApps(currentHomepageApps)
        // Reload the homepage to reflect the change
        loadHomepageApps()
    }

    /**
     * Open settings activity
     */
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Setup gesture detection on clock and app list areas
     */
    private fun setupGestureDetection() {
        val touchListener = View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Allow other touch events to pass through
        }

        // Apply gesture detection to clock and app list
        digitalClock.setOnTouchListener(touchListener)
        appRecyclerView.setOnTouchListener(touchListener)
    }

    /**
     * Launch an app by package name (for gestures)
     */
    private fun launchAppByPackageName(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        } catch (e: Exception) {
            // Failed to launch app
        }
    }

    /**
     * Gesture listener for detecting swipes
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            // Determine if it's a horizontal or vertical swipe
            if (abs(diffX) > abs(diffY)) {
                // Horizontal swipe
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swipe right
                        prefsManager.getGestureApp("right")?.let { launchAppByPackageName(it) }
                    } else {
                        // Swipe left
                        prefsManager.getGestureApp("left")?.let { launchAppByPackageName(it) }
                    }
                    return true
                }
            } else {
                // Vertical swipe
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // Swipe down
                        prefsManager.getGestureApp("down")?.let { launchAppByPackageName(it) }
                    } else {
                        // Swipe up
                        prefsManager.getGestureApp("up")?.let { launchAppByPackageName(it) }
                    }
                    return true
                }
            }

            return false
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
        // Reload apps in case settings changed
        loadHomepageApps()
        // Refresh app cache for newly installed/uninstalled apps
        preloadAllApps()
    }
}
