package com.xevrae.expect

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.net.toUri
import com.xevrae.ui.AppGlobalContext

fun openUrl(url: String) {
    val context = AppGlobalContext.get() ?: return
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    } catch (_: Exception) {}
}

fun shareUrl(
    title: String,
    url: String,
) {
    val context = AppGlobalContext.get() ?: return
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        val chooserIntent = Intent.createChooser(shareIntent, title).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooserIntent)
    } catch (_: Exception) {}
}