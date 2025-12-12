package com.example.app4.features.security

import android.app.Activity
import android.view.WindowManager

object SecurityHelper {

    fun enableScreenCaptureProtection(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disableScreenCaptureProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun isScreenCaptureProtected(activity: Activity): Boolean {
        return (activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
}
