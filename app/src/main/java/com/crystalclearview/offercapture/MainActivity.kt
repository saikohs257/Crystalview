package com.crystalclearview.offercapture

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = TextView(this).apply {
            textSize = 18f
            setPadding(32, 48, 32, 24)
            text = "CrystalClearview\n\n" +
                "Capture Fabric V1\n" +
                "Observation-only capture for configured driver apps.\n\n" +
                "Enable the CrystalClearview Accessibility Service in Android Settings.\n\n" +
                "Accessibility status: ${accessibilityEnabled()}"
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(status)
        })
    }

    private fun accessibilityEnabled(): Boolean {
        return try {
            val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            enabled?.contains(packageName, ignoreCase = true) == true
        } catch (_: Throwable) {
            false
        }
    }
}
