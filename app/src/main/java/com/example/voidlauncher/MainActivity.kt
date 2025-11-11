package com.example.voidlauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import android.widget.TextClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main launcher screen showing curated list of essential apps
 */
class MainActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var digitalClock: TextClock
    private lateinit var dateText: TextView
    private lateinit var appRecyclerView: RecyclerView
    private lateinit var allAppsText: TextView
    private lateinit var settingsText: TextView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var gestureDetector: GestureDetector
    private lateinit var clockGestureDetector: GestureDetector
    private lateinit var screenReceiver: ScreenReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide system UI for minimal look
        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        digitalClock = findViewById(R.id.digitalClock)
        dateText = findViewById(R.id.dateText)
        appRecyclerView = findViewById(R.id.appRecyclerView)
        allAppsText = findViewById(R.id.allAppsText)
        settingsText = findViewById(R.id.settingsText)

        // Apply font size scaling to all text elements
        applyFontSizes()

        // Apply clock settings
        applyClockSettings()

        // Update date display
        updateDateDisplay()

        // Setup gesture detector
        gestureDetector = GestureDetector(this, GestureListener())

        // Setup clock gesture detector (single tap and double tap)
        clockGestureDetector = GestureDetector(this, ClockGestureListener())
        digitalClock.setOnTouchListener { _, event ->
            clockGestureDetector.onTouchEvent(event)
            true
        }

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

        // Register screen on/off receiver
        registerScreenReceiver()

        // Check for daily reset
        UsageTrackingManager.checkAndPerformDailyReset(this)
    }

    /**
     * Apply font size scaling to all text elements
     */
    private fun applyFontSizes() {
        val fontSize = prefsManager.getFontSize()

        // Base sizes from layout XML
        val headerBaseSize = 16f
        val dateBaseSize = 14f
        val bottomTextBaseSize = 16f

        // Apply scaled sizes
        headerText.textSize = fontSize * headerBaseSize / 16f
        // Clock size is independent and set by applyClockSettings()
        dateText.textSize = fontSize * dateBaseSize / 16f
        allAppsText.textSize = fontSize * bottomTextBaseSize / 16f
        settingsText.textSize = fontSize * bottomTextBaseSize / 16f
    }

    /**
     * Apply clock customization settings
     */
    private fun applyClockSettings() {
        val clockSize = prefsManager.getClockSize()
        val use24h = prefsManager.getClock24h()

        // Apply clock size
        digitalClock.textSize = clockSize

        // Apply 12h/24h format
        digitalClock.format12Hour = if (use24h) null else "hh:mm a"
        digitalClock.format24Hour = if (use24h) "HH:mm" else null
    }

    /**
     * Update date display based on settings
     */
    private fun updateDateDisplay() {
        val showDate = prefsManager.getShowDate()
        val showDayOfWeek = prefsManager.getShowDayOfWeek()

        // Show if either date or day of week is enabled
        dateText.visibility = if (showDate || showDayOfWeek) View.VISIBLE else View.GONE

        if (showDate || showDayOfWeek) {
            val format = when {
                showDate && showDayOfWeek -> "EEEE, MMMM d" // "Monday, January 15"
                showDayOfWeek -> "EEEE" // "Monday"
                else -> "MMMM d" // "January 15"
            }
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateText.text = dateFormat.format(Date())
        }
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
     * Check if any gesture is set to open All Apps and hide/show button accordingly
     */
    private fun updateAllAppsButtonVisibility() {
        val directions = listOf("up", "down", "left", "right")
        val hasAllAppsGesture = directions.any { direction ->
            prefsManager.getGestureApp(direction) == GestureAppSelectionActivity.ALL_APPS_IDENTIFIER
        }

        // Hide button if any gesture opens All Apps, show otherwise
        allAppsText.visibility = if (hasAllAppsGesture) View.GONE else View.VISIBLE
    }

    /**
     * Register screen receiver for tracking screen on/off
     */
    private fun registerScreenReceiver() {
        screenReceiver = ScreenReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
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
        val touchListener = View.OnTouchListener { view, event ->
            val gestureHandled = gestureDetector.onTouchEvent(event)
            // For app list, allow other touch events if gesture wasn't handled
            // This allows scrolling and clicking apps while still detecting swipes/double tap
            if (view == appRecyclerView) {
                !gestureHandled
            } else {
                true
            }
        }

        // Apply gesture detection to app list (not clock, it has its own handler)
        appRecyclerView.setOnTouchListener(touchListener)
    }

    /**
     * Handle gesture action - either open All Apps or launch an app
     */
    private fun handleGestureAction(direction: String) {
        val gestureApp = prefsManager.getGestureApp(direction)
        if (gestureApp != null) {
            if (gestureApp == GestureAppSelectionActivity.ALL_APPS_IDENTIFIER) {
                // Open All Apps activity
                val intent = Intent(this, AllAppsActivity::class.java)
                startActivity(intent)
            } else {
                // Launch regular app
                launchAppByPackageName(gestureApp)
            }
        }
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
     * Gesture listener for detecting single tap and double tap on clock
     */
    private inner class ClockGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            openClockApp()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            lockScreen()
            return true
        }
    }

    /**
     * Open the configured clock app or default clock app
     */
    private fun openClockApp() {
        val clockAppPackage = prefsManager.getClockApp()

        if (clockAppPackage != null) {
            // Open specific clock app selected by user
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(clockAppPackage)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                    return
                }
            } catch (e: Exception) {
                // Fall through to default
            }
        }

        // Open default system clock app (AlarmClock intent)
        try {
            val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
            startActivity(intent)
        } catch (e: Exception) {
            // No clock app available, try generic intent
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                intent.`package` = "com.google.android.deskclock"
                startActivity(intent)
            } catch (e2: Exception) {
                // No clock app found
            }
        }
    }

    /**
     * Lock the screen using accessibility service (allows biometric unlock)
     */
    private fun lockScreen() {
        if (isAccessibilityServiceEnabled()) {
            // Use accessibility service to lock screen (allows biometric unlock)
            val intent = Intent(this, LockScreenAccessibilityService::class.java)
            intent.action = LockScreenAccessibilityService.ACTION_LOCK_SCREEN
            startService(intent)
        } else {
            // Prompt user to enable accessibility service
            AlertDialog.Builder(this)
                .setTitle("Enable Accessibility Service")
                .setMessage("To lock the screen with biometric unlock enabled, please enable Void Launcher accessibility service in Settings.\n\nGo to Settings > Accessibility > Void Launcher and turn it on.")
                .setPositiveButton("Open Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /**
     * Check if the accessibility service is enabled
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val colonSplitter = enabledServices?.split(":")
        return colonSplitter?.any {
            it.contains(packageName) && it.contains(LockScreenAccessibilityService::class.java.simpleName)
        } ?: false
    }

    /**
     * Gesture listener for detecting swipes and double tap
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Check if double tap to lock is enabled
            if (prefsManager.getDoubleTapToLock()) {
                lockScreen()
                return true
            }
            return false
        }

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
                        handleGestureAction("right")
                    } else {
                        // Swipe left
                        handleGestureAction("left")
                    }
                    return true
                }
            } else {
                // Vertical swipe
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // Swipe down
                        handleGestureAction("down")
                    } else {
                        // Swipe up
                        handleGestureAction("up")
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
        // Update All Apps button visibility based on gestures
        updateAllAppsButtonVisibility()
        // Reapply font sizes in case they changed in settings
        applyFontSizes()
        // Reapply clock settings
        applyClockSettings()
        // Update date display in case setting changed
        updateDateDisplay()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister screen receiver
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Receiver already unregistered
        }
    }
}
