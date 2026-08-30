package com.example.apptimelimiter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AppRule(
    val packageName: String,
    val label: String,
    val limitMinutes: Int
)

object RuleStore {
    private const val PREFS = "app_time_limiter"
    private const val KEY_RULES = "rules"

    fun getAll(context: Context): List<AppRule> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RULES, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    add(
                        AppRule(
                            packageName = item.getString("packageName"),
                            label = item.optString("label", item.getString("packageName")),
                            limitMinutes = item.getInt("limitMinutes")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun get(context: Context, packageName: String): AppRule? =
        getAll(context).firstOrNull { it.packageName == packageName }

    fun upsert(context: Context, rule: AppRule) {
        val rules = getAll(context).filterNot { it.packageName == rule.packageName } + rule
        save(context, rules)
    }

    fun remove(context: Context, packageName: String) {
        save(context, getAll(context).filterNot { it.packageName == packageName })
    }

    private fun save(context: Context, rules: List<AppRule>) {
        val arr = JSONArray()
        rules.sortedBy { it.label.lowercase() }.forEach { rule ->
            arr.put(
                JSONObject()
                    .put("packageName", rule.packageName)
                    .put("label", rule.label)
                    .put("limitMinutes", rule.limitMinutes)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RULES, arr.toString())
            .apply()
    }
}
