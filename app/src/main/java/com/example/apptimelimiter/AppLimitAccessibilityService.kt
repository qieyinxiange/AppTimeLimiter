package com.example.apptimelimiter

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppLimitAccessibilityService : AccessibilityService() {
    private var lastPackage: String? = null
    private var lastBlockAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName) return

        val rule = RuleStore.get(this, packageName) ?: return
        val now = System.currentTimeMillis()

        // 防止同一个窗口事件短时间内重复拉起拦截页。
        if (lastPackage == packageName && now - lastBlockAt < 1_500) return

        val usedMs = AppUsage.todayUsageMs(this, packageName)
        val limitMs = rule.limitMinutes * 60_000L
        if (usedMs >= limitMs) {
            lastPackage = packageName
            lastBlockAt = now
            val intent = Intent(this, BlockedActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("label", rule.label)
                putExtra("limitMinutes", rule.limitMinutes)
                putExtra("usedMinutes", usedMs / 60_000L)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() = Unit
}
