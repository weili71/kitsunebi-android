package com.weilizan.kitsunebi.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject


fun getClipboardContents(context: Context): String? {
    val clipboard =
        context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = clipboard.primaryClip

    if (clipData != null && clipData.itemCount > 0) {
        return try {
            clipData.getItemAt(0).text.toString()
        } catch (e: Exception) {
            null
        }
    }
    return null
}

fun base64Encode(content: String): String {
    return Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP)
}

fun base64Decode(content: String): String = String(Base64.decode(content, Base64.NO_WRAP))

fun formatJsonString(json: String): String? {
    return JSONObject(json).toString(2)
}

fun copyToClipboard(context: Context,clip: ClipData){
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(clip)
}
