package com.example.voidlauncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Data class for apps that can be selected/deselected
 */
data class SelectableApp(
    val app: App,
    var isSelected: Boolean
)

/**
 * RecyclerView adapter for selecting apps with checkboxes
 */
class SelectableAppAdapter(
    private val apps: List<SelectableApp>
) : RecyclerView.Adapter<SelectableAppAdapter.SelectableAppViewHolder>() {

    class SelectableAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.appCheckbox)
        val appNameText: TextView = itemView.findViewById(R.id.appNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_app, parent, false)
        return SelectableAppViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectableAppViewHolder, position: Int) {
        val selectableApp = apps[position]

        holder.appNameText.text = selectableApp.app.label
        holder.checkbox.isChecked = selectableApp.isSelected

        // Toggle selection on item click
        holder.itemView.setOnClickListener {
            selectableApp.isSelected = !selectableApp.isSelected
            holder.checkbox.isChecked = selectableApp.isSelected
        }

        // Toggle selection on checkbox click
        holder.checkbox.setOnClickListener {
            selectableApp.isSelected = holder.checkbox.isChecked
        }
    }

    override fun getItemCount(): Int = apps.size

    /**
     * Get list of selected apps
     */
    fun getSelectedApps(): List<App> {
        return apps.filter { it.isSelected }.map { it.app }
    }
}
