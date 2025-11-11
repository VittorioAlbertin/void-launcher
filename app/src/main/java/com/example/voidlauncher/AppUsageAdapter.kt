package com.example.voidlauncher

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for displaying app usage statistics
 */
class AppUsageAdapter(
    private val context: Context,
    private val usageData: List<AppUsageData>
) : RecyclerView.Adapter<AppUsageAdapter.AppUsageViewHolder>() {

    class AppUsageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameText: TextView = itemView.findViewById(R.id.appNameText)
        val appUsageText: TextView = itemView.findViewById(R.id.appUsageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_usage, parent, false)
        return AppUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppUsageViewHolder, position: Int) {
        val usage = usageData[position]

        // Get app name
        val appName = try {
            val appInfo = context.packageManager.getApplicationInfo(usage.packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            usage.packageName
        }

        holder.appNameText.text = appName

        // Format usage text
        val timeFormatted = UsageTrackingManager.formatTime(usage.timeSpent)
        val opensText = "${usage.openCount} open${if (usage.openCount == 1) "" else "s"}"
        holder.appUsageText.text = "$timeFormatted  •  $opensText"
    }

    override fun getItemCount(): Int = usageData.size
}
