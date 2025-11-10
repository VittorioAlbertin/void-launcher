package com.example.voidlauncher

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton cache for storing preloaded app list
 */
object AppCache {
    private var cachedApps: List<App>? = null
    private val lock = Any()

    /**
     * Get cached apps if available
     */
    fun getCachedApps(): List<App>? {
        synchronized(lock) {
            return cachedApps
        }
    }

    /**
     * Load all installed apps in background and cache them
     */
    suspend fun loadAndCacheApps(context: Context, prefsManager: PreferencesManager) {
        val apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val appsList = mutableListOf<App>()
            val hiddenPackages = prefsManager.getHiddenApps()

            // Get all apps with a launcher intent
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            val activities = pm.queryIntentActivities(intent, 0)

            for (resolveInfo in activities) {
                val label = resolveInfo.loadLabel(pm).toString()
                val packageName = resolveInfo.activityInfo.packageName

                // Skip hidden apps
                if (hiddenPackages.contains(packageName)) {
                    continue
                }

                val launchIntent = pm.getLaunchIntentForPackage(packageName)

                if (launchIntent != null) {
                    appsList.add(App(label, packageName, launchIntent))
                }
            }

            // Sort alphabetically by label
            appsList.sortedBy { it.label.lowercase() }
        }

        synchronized(lock) {
            cachedApps = apps
        }
    }

    /**
     * Clear the cache (useful when settings change)
     */
    fun clearCache() {
        synchronized(lock) {
            cachedApps = null
        }
    }
}
