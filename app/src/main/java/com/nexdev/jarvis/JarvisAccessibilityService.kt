package com.nexdev.jarvis

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Bundle

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Screen events are available only while the user enables this service.
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun goBack(): Boolean =
        performGlobalAction(GLOBAL_ACTION_BACK)

    fun goHome(): Boolean =
        performGlobalAction(GLOBAL_ACTION_HOME)

    fun clickVisibleText(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val matches = root.findAccessibilityNodeInfosByText(target)
        for (node in matches) {
            var current: AccessibilityNodeInfo? = node
            while (current != null) {
                if (current.isClickable &&
                    current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ) return true
                current = current.parent
            }
        }
        return false
    }

    fun typeIntoFocusedField(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val field = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: return false

        if (!field.isEditable) return false

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                value
            )
        }
        return field.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT, args
        )
    }

    companion object {
        @Volatile
        var instance: JarvisAccessibilityService? = null
            private set
    }
}
