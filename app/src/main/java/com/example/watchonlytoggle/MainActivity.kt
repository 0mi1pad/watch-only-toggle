package com.example.watchonlytoggle

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import android.widget.Toast

/**
 * This is the screen that opens when you tap the app icon on the watch.
 *
 * - First time ever: the accessibility service isn't enabled yet, so we send you
 *   straight to the Accessibility settings page to turn it on (one-time step).
 * - Every time after that: we tell the accessibility service "go do the Watch Only
 *   sequence" and open Settings ourselves. The service takes it from there.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isOurServiceEnabled()) {
            setContentView(buildSetupView())
            Toast.makeText(
                this,
                "One-time setup: turn on the Watch Only service, then tap this app again",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            finish()
            return
        }

        WatchOnlyAccessibilityService.requestActivation()
        startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    private fun buildSetupView(): TextView {
        val tv = TextView(this)
        tv.text = "One-time setup:\n\nTurn on the \"Watch Only\" accessibility service on the next screen, then open this app again."
        tv.setPadding(32, 48, 32, 32)
        return tv
    }

    private fun isOurServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == packageName &&
                it.resolveInfo.serviceInfo.name == WatchOnlyAccessibilityService::class.java.name
        }
    }
}
