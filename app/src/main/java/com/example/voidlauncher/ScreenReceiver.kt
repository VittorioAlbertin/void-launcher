package com.example.voidlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver for tracking screen on/off events
 */
class ScreenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                UsageTrackingManager.trackScreenOn(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                UsageTrackingManager.trackScreenOff(context)
            }
        }
    }
}
