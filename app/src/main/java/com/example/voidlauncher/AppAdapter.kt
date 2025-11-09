package com.example.voidlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for displaying apps in a minimal text list
 */
class AppAdapter(
    private val apps: List<App>,
    private val fontSize: Float = 16f,
    private val onAppClick: (App) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameText: TextView = itemView.findViewById(R.id.appNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appNameText.text = app.label
        holder.appNameText.textSize = fontSize

        holder.itemView.setOnClickListener {
            onAppClick(app)
        }
    }

    override fun getItemCount(): Int = apps.size
}
