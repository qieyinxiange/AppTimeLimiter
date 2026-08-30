package com.example.apptimelimiter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class BlockedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)
        render()

        findViewById<Button>(R.id.homeButton).setOnClickListener {
            goHome()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        render()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        goHome()
    }

    private fun render() {
        val label = intent?.getStringExtra("label") ?: "该应用"
        val limit = intent?.getIntExtra("limitMinutes", 0) ?: 0
        val used = intent?.getLongExtra("usedMinutes", 0L) ?: 0L
        findViewById<TextView>(R.id.blockedMessage).text =
            "$label 今天已使用约 $used 分钟，已达到 $limit 分钟的每日上限。"
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        finish()
    }
}
