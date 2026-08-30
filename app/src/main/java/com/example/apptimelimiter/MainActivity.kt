package com.example.apptimelimiter

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    data class AppEntry(val label: String, val packageName: String) {
        override fun toString(): String = "$label  ($packageName)"
    }

    private lateinit var statusText: TextView
    private lateinit var appSpinner: Spinner
    private lateinit var minutesInput: EditText
    private lateinit var rulesContainer: LinearLayout
    private var apps: List<AppEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        appSpinner = findViewById(R.id.appSpinner)
        minutesInput = findViewById(R.id.minutesInput)
        rulesContainer = findViewById(R.id.rulesContainer)

        findViewById<Button>(R.id.usageAccessButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.addRuleButton).setOnClickListener { addRule() }

        loadApps()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        renderRules()
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(launcherIntent, 0)
            .map {
                AppEntry(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        appSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            apps
        )
    }

    private fun addRule() {
        val app = appSpinner.selectedItem as? AppEntry ?: return
        val minutes = minutesInput.text.toString().toIntOrNull()
        if (minutes == null || minutes !in 1..1440) {
            Toast.makeText(this, "请输入 1–1440 分钟", Toast.LENGTH_SHORT).show()
            return
        }

        RuleStore.upsert(
            this,
            AppRule(app.packageName, app.label, minutes)
        )
        minutesInput.text.clear()
        renderRules()
        Toast.makeText(this, "已保存：${app.label} 每天 $minutes 分钟", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatus() {
        val usage = if (AppUsage.hasUsageAccess(this)) "✅ 使用情况访问：已开启" else "❌ 使用情况访问：未开启"
        val accessibility = if (isAccessibilityEnabled()) "✅ 无障碍服务：已开启" else "❌ 无障碍服务：未开启"
        statusText.text = "$usage\n$accessibility"
    }

    private fun renderRules() {
        rulesContainer.removeAllViews()
        val rules = RuleStore.getAll(this)
        if (rules.isEmpty()) {
            rulesContainer.addView(TextView(this).apply { text = "还没有设置限制。" })
            return
        }

        rules.forEach { rule ->
            val usedMinutes = AppUsage.todayUsageMs(this, rule.packageName) / 60_000
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 12)
            }
            val info = TextView(this).apply {
                text = "${rule.label}\n今天 ${usedMinutes} / ${rule.limitMinutes} 分钟"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val delete = Button(this).apply {
                text = "删除"
                setOnClickListener {
                    RuleStore.remove(this@MainActivity, rule.packageName)
                    renderRules()
                }
            }
            row.addView(info)
            row.addView(delete)
            rulesContainer.addView(row)
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val component = ComponentName(this, AppLimitAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(component, ignoreCase = true) }
    }
}
