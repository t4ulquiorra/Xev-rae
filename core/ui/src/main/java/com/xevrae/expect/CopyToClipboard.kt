package com.xevrae.expect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.xevrae.ui.AppGlobalContext

fun copyToClipboard(
    label: String,
    text: String,
) {
    val context: Context = AppGlobalContext.get() ?: return
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
}