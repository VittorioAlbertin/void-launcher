package com.example.voidlauncher

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for configuring automatic app hiding based on time and usage
 */
class AutoHideSettingsActivity : AppCompatActivity() {

    private lateinit var headerText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var searchBar: EditText
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager

    private var allApps: List<AppWithRules> = emptyList()
    private var filteredApps: List<AppWithRules> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_hide_settings)

        hideSystemUI()

        prefsManager = PreferencesManager(this)

        headerText = findViewById(R.id.headerText)
        descriptionText = findViewById(R.id.descriptionText)
        searchBar = findViewById(R.id.searchBar)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        applyFontSizes()

        appsRecyclerView.layoutManager = LinearLayoutManager(this)

        setupSearch()
        loadApps()
    }

    private fun applyFontSizes() {
        val fontSize = prefsManager.getFontSize()

        val headerBaseSize = 16f
        val descBaseSize = 12f
        val searchBaseSize = 14f

        headerText.textSize = fontSize * headerBaseSize / 16f
        descriptionText.textSize = fontSize * descBaseSize / 16f
        searchBar.textSize = fontSize * searchBaseSize / 16f
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.app.label.contains(query, ignoreCase = true) }
        }
        updateAppList()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
                intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                val packages = pm.queryIntentActivities(intent, 0)

                packages.mapNotNull { resolveInfo ->
                    try {
                        val packageName = resolveInfo.activityInfo.packageName
                        val label = resolveInfo.loadLabel(pm).toString()
                        val launchIntent = pm.getLaunchIntentForPackage(packageName)

                        if (launchIntent != null) {
                            val rulesJson = prefsManager.getAutoHideRules(packageName)
                            val rules = if (rulesJson != null) {
                                AutoHideManager.parseRules(rulesJson)
                            } else {
                                AutoHideRules(emptyList(), 0, 0)
                            }

                            val isCurrentlyHidden = AutoHideManager.shouldHideApp(
                                this@AutoHideSettingsActivity,
                                packageName,
                                prefsManager
                            )

                            AppWithRules(
                                App(label, packageName, launchIntent),
                                rules,
                                isCurrentlyHidden
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.app.label.lowercase() }
            }

            allApps = apps
            filteredApps = allApps
            updateAppList()
        }
    }

    private fun updateAppList() {
        val fontSize = prefsManager.getFontSize()
        val adapter = AutoHideAdapter(
            apps = filteredApps,
            fontSize = fontSize,
            onClick = { appWithRules -> showEditRulesDialog(appWithRules) }
        )
        appsRecyclerView.adapter = adapter
    }

    private fun showEditRulesDialog(appWithRules: AppWithRules) {
        val options = arrayOf(
            "Add Time Schedule",
            "Set Max Opens/Day",
            "Set Max Time/Day",
            "View Current Rules",
            "Remove All Rules"
        )

        // Create custom adapter with white text
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            options
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView?.setTextColor(resources.getColor(R.color.void_white, null))
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle(appWithRules.app.label)
            .setAdapter(adapter) { _, which ->
                when (which) {
                    0 -> showAddTimeScheduleDialog(appWithRules)
                    1 -> showSetMaxOpensDialog(appWithRules)
                    2 -> showSetMaxTimeDialog(appWithRules)
                    3 -> showViewRulesDialog(appWithRules)
                    4 -> removeAllRules(appWithRules)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddTimeScheduleDialog(appWithRules: AppWithRules) {
        val view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 20)

        // Start time inputs
        val startHourInput = EditText(this)
        startHourInput.hint = "Start hour (0-23)"
        startHourInput.inputType = InputType.TYPE_CLASS_NUMBER
        startHourInput.setTextColor(resources.getColor(R.color.void_white, null))
        startHourInput.setHintTextColor(resources.getColor(R.color.void_white, null))

        val startMinuteInput = EditText(this)
        startMinuteInput.hint = "Start minute (0-59)"
        startMinuteInput.inputType = InputType.TYPE_CLASS_NUMBER
        startMinuteInput.setTextColor(resources.getColor(R.color.void_white, null))
        startMinuteInput.setHintTextColor(resources.getColor(R.color.void_white, null))

        // End time inputs
        val endHourInput = EditText(this)
        endHourInput.hint = "End hour (0-23)"
        endHourInput.inputType = InputType.TYPE_CLASS_NUMBER
        endHourInput.setTextColor(resources.getColor(R.color.void_white, null))
        endHourInput.setHintTextColor(resources.getColor(R.color.void_white, null))

        val endMinuteInput = EditText(this)
        endMinuteInput.hint = "End minute (0-59)"
        endMinuteInput.inputType = InputType.TYPE_CLASS_NUMBER
        endMinuteInput.setTextColor(resources.getColor(R.color.void_white, null))
        endMinuteInput.setHintTextColor(resources.getColor(R.color.void_white, null))

        container.addView(startHourInput)
        container.addView(startMinuteInput)
        container.addView(endHourInput)
        container.addView(endMinuteInput)

        AlertDialog.Builder(this)
            .setTitle("Add Time Schedule")
            .setMessage("App will be hidden during this time")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                try {
                    val startHour = startHourInput.text.toString().toInt()
                    val startMinute = startMinuteInput.text.toString().toInt()
                    val endHour = endHourInput.text.toString().toInt()
                    val endMinute = endMinuteInput.text.toString().toInt()

                    if (startHour in 0..23 && startMinute in 0..59 &&
                        endHour in 0..23 && endMinute in 0..59) {

                        val newRule = TimeRule(startHour, startMinute, endHour, endMinute)
                        val newRules = appWithRules.rules.copy(
                            timeRules = appWithRules.rules.timeRules + newRule
                        )

                        saveRules(appWithRules.app.packageName, newRules)
                        loadApps()
                    }
                } catch (e: Exception) {
                    // Invalid input
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetMaxOpensDialog(appWithRules: AppWithRules) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(if (appWithRules.rules.maxOpens > 0) appWithRules.rules.maxOpens.toString() else "")
        input.hint = "Max opens per day (0 = unlimited)"
        input.setTextColor(resources.getColor(R.color.void_white, null))
        input.setHintTextColor(resources.getColor(R.color.void_white, null))

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 20)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Max Opens Per Day")
            .setMessage("Hide app after this many opens today")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                try {
                    val maxOpens = input.text.toString().toIntOrNull() ?: 0
                    val newRules = appWithRules.rules.copy(maxOpens = maxOpens)
                    saveRules(appWithRules.app.packageName, newRules)
                    loadApps()
                } catch (e: Exception) {
                    // Invalid input
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetMaxTimeDialog(appWithRules: AppWithRules) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        val currentMinutes = (appWithRules.rules.maxTimeMs / 1000 / 60).toInt()
        input.setText(if (currentMinutes > 0) currentMinutes.toString() else "")
        input.hint = "Max minutes per day (0 = unlimited)"
        input.setTextColor(resources.getColor(R.color.void_white, null))
        input.setHintTextColor(resources.getColor(R.color.void_white, null))

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 20)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Max Time Per Day")
            .setMessage("Hide app after this much usage today")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                try {
                    val minutes = input.text.toString().toIntOrNull() ?: 0
                    val maxTimeMs = minutes * 60L * 1000L
                    val newRules = appWithRules.rules.copy(maxTimeMs = maxTimeMs)
                    saveRules(appWithRules.app.packageName, newRules)
                    loadApps()
                } catch (e: Exception) {
                    // Invalid input
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showViewRulesDialog(appWithRules: AppWithRules) {
        val rules = appWithRules.rules
        val message = buildString {
            appendLine("Current Rules:")
            appendLine()

            if (rules.timeRules.isNotEmpty()) {
                appendLine("Time Schedules:")
                rules.timeRules.forEach { rule ->
                    appendLine("  • ${AutoHideManager.formatTimeRule(rule)}")
                }
                appendLine()
            }

            if (rules.maxOpens > 0) {
                appendLine("Max Opens: ${rules.maxOpens}/day")
            }

            if (rules.maxTimeMs > 0) {
                appendLine("Max Time: ${UsageTrackingManager.formatTime(rules.maxTimeMs)}/day")
            }

            if (rules.timeRules.isEmpty() && rules.maxOpens == 0 && rules.maxTimeMs == 0L) {
                append("No rules configured")
            }
        }

        AlertDialog.Builder(this)
            .setTitle(appWithRules.app.label)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun removeAllRules(appWithRules: AppWithRules) {
        AlertDialog.Builder(this)
            .setTitle("Remove All Rules")
            .setMessage("Remove all auto-hide rules for ${appWithRules.app.label}?")
            .setPositiveButton("Remove") { _, _ ->
                prefsManager.saveAutoHideRules(appWithRules.app.packageName, null)
                loadApps()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveRules(packageName: String, rules: AutoHideRules) {
        val json = AutoHideManager.rulesToJson(rules)
        prefsManager.saveAutoHideRules(packageName, json)
    }

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
        loadApps()
    }
}

/**
 * Data class combining an app with its auto-hide rules
 */
data class AppWithRules(
    val app: App,
    val rules: AutoHideRules,
    val isCurrentlyHidden: Boolean
)

/**
 * Adapter for auto-hide settings list
 */
class AutoHideAdapter(
    private val apps: List<AppWithRules>,
    private val fontSize: Float,
    private val onClick: (AppWithRules) -> Unit
) : RecyclerView.Adapter<AutoHideAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameText: TextView = view.findViewById(R.id.appNameText)
        val rulesSummaryText: TextView = view.findViewById(R.id.rulesSummaryText)
        val statusText: TextView = view.findViewById(R.id.statusText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auto_hide_rule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appWithRules = apps[position]

        // Apply font size scaling
        val appNameBaseSize = 16f
        val summaryBaseSize = 12f
        val statusBaseSize = 12f

        holder.appNameText.textSize = fontSize * appNameBaseSize / 16f
        holder.rulesSummaryText.textSize = fontSize * summaryBaseSize / 16f
        holder.statusText.textSize = fontSize * statusBaseSize / 16f

        holder.appNameText.text = appWithRules.app.label
        holder.rulesSummaryText.text = AutoHideManager.getRulesSummary(appWithRules.rules)

        if (appWithRules.isCurrentlyHidden) {
            holder.statusText.visibility = View.VISIBLE
            holder.statusText.text = "Currently hidden"
        } else {
            holder.statusText.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onClick(appWithRules)
        }
    }

    override fun getItemCount() = apps.size
}
