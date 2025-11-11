package com.example.voidlauncher

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service that allows locking the screen without requiring PIN/password
 * This enables biometric unlock after double-tap lock
 */
class LockScreenAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events
        // This service is only used for the lock screen action
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt the feedback
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service is ready
    }

    companion object {
        const val ACTION_LOCK_SCREEN = "com.example.voidlauncher.ACTION_LOCK_SCREEN"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_LOCK_SCREEN) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
        return START_NOT_STICKY
    }
}
