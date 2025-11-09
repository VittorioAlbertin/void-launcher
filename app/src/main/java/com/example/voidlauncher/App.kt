package com.example.voidlauncher

import android.content.Intent

/**
 * Data class representing an app that can be launched
 */
data class App(
    val label: String,
    val packageName: String,
    val launchIntent: Intent?
)
