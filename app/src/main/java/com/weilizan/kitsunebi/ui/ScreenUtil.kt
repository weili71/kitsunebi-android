package com.weilizan.kitsunebi.ui

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager

fun getScreenWidth(context: Context): Int {
    val resources = context.resources
    val dm = resources.displayMetrics
    return dm.widthPixels
}

fun getScreenHeight(context: Context): Int {
    val resources = context.resources
    val dm = resources.displayMetrics
    return dm.heightPixels
}

fun getFullScreenHeight(context: Context): Int {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var height = 0
    val display = wm.defaultDisplay
    val dm = DisplayMetrics()
    val c: Class<*>
    try {
        c = Class.forName("android.view.Display")
        val method = c.getMethod(
            "getRealMetrics",
            DisplayMetrics::class.java
        )
        method.invoke(display, dm)
        height = dm.heightPixels
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return height
}

fun getStatusBarHeight(context: Context): Int {
    val resources = context.resources
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

public fun getNavigationBarHeight(context: Context): Int {
    val result = 0
    var resourceId = 0
    val rid = context.resources.getIdentifier("config_showNavigationBar", "bool", "android");
    if (rid != 0) {
        resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return context.resources.getDimensionPixelSize(resourceId)
    } else
        return 0
}