package ijk.player.videoview.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RawRes
import androidx.annotation.StringRes

private var toast: Toast? = null
private var view1: View? = null

fun toast(context: Context,text: String,duration: Int=LENGTH_SHORT) {
    createToast(context, text, duration)
}

fun toast(context: Context,@StringRes resId:Int,duration: Int=LENGTH_SHORT) {
    createToast(context, context.getString(resId), duration)
}

@SuppressLint("ShowToast")
private fun createToast(context: Context, text: String, duration: Int) {
    toast?.cancel()
    toast = Toast(context)
    if (view1 == null) view1 = Toast.makeText(context, "", LENGTH_SHORT).view
    toast!!.apply {
        this.view = view1
        this.duration = duration
        setText(text)
        show()
    }
}