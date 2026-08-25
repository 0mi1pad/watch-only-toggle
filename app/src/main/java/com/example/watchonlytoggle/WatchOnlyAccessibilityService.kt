package com.example.watchonlytoggle

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Drives the on-screen automation: Settings -> "Battery" -> "Watch only" -> "Turn on".
 *
 * How it works: each time the screen changes, we look at everything currently on
 * screen for the text we're waiting for. If it's not there yet, we scroll down a
 * notch and check again. When we find it, we tap it and move to the next target.
 *
 * ---- IF SOMETHING DOESN'T GET TAPPED ----
 * The three strings below are the ONLY place you should need to edit if Samsung
 * changes the wording on your watch, or if it turns out slightly different from
 * what was tested. Match the text exactly as it appears on screen (capitalization
 * included -- Android accessibility text matching is case-sensitive for the exact
 * match, though there's a fallback below that's case-insensitive too).
 */
class WatchOnlyAccessibilityService : AccessibilityService() {

    companion object {
        // Edit these three if Samsung ever changes the wording on your watch.
        private const val TARGET_BATTERY = "Battery"
        private const val TARGET_WATCH_ONLY = "Watch only"
        private const val TARGET_TURN_ON = "Turn on"

        @Volatile private var instance: WatchOnlyAccessibilityService? = null
        @Volatile private var awaitingRun = false

        fun requestActivation() {
            awaitingRun = true
            instance?.beginSequence()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var step = 0 // 0 = find Battery, 1 = find Watch only, 2 = find Turn on, 3 = done
    private var scrollAttempts = 0
    private val maxScrollAttemptsPerStep = 8 // raise this if your screens need more scrolling
    private var running = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        if (awaitingRun) beginSequence()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
    }

    override fun onInterrupt() {}

    private fun beginSequence() {
        if (running) return
        running = true
        step = 0
        scrollAttempts = 0
        awaitingRun = false
        // Small delay to let the Settings app actually start drawing.
        handler.postDelayed({ attemptStep() }, 400)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!running) return
        val type = event?.eventType
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({ attemptStep() }, 300)
        }
    }

    private fun attemptStep() {
        if (!running) return

        val root = rootInActiveWindow
        if (root == null) {
            giveUpForNow()
            return
        }

        val targetText = when (step) {
            0 -> TARGET_BATTERY
            1 -> TARGET_WATCH_ONLY
            2 -> TARGET_TURN_ON
            else -> null
        }

        if (targetText == null) {
            running = false // done -- Turn on was tapped, nothing left to do
            return
        }

        val node = findNodeByText(root, targetText)
        if (node != null) {
            scrollAttempts = 0
            clickNode(node)
            step++
            // Give the next screen a moment to appear before we look again.
            handler.postDelayed({ attemptStep() }, 700)
            return
        }

        // Not on screen yet -- scroll down a notch and look again.
        if (scrollAttempts < maxScrollAttemptsPerStep) {
            scrollAttempts++
            val scrolled = scrollDown(root)
            handler.postDelayed({ attemptStep() }, if (scrolled) 450 else 600)
        } else {
            giveUpForNow()
        }
    }

    private fun giveUpForNow() {
        // We couldn't find the expected text after several scroll attempts.
        // Rather than tapping something wrong, we just stop here quietly.
        // (Nothing harmful happens -- worst case you're left looking at whatever
        // Settings screen it stalled on, same as if you'd navigated there yourself.)
        running = false
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val matches = root.findAccessibilityNodeInfosByText(text)
        // Prefer an exact, case-sensitive match first.
        for (n in matches) {
            if (n.text?.toString()?.trim() == text) return n
        }
        // Fall back to a case-insensitive / "starts with" match.
        for (n in matches) {
            if (n.text?.toString()?.trim()?.startsWith(text, ignoreCase = true) == true) return n
        }
        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var n: AccessibilityNodeInfo? = node
        while (n != null) {
            if (n.isClickable) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
            n = n.parent
        }
        // Last resort: click the node itself even if it wasn't marked clickable.
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun scrollDown(root: AccessibilityNodeInfo): Boolean {
        val scrollable = findScrollableNode(root) ?: return false
        return scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }
}
