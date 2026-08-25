package com.xevrae.expect

import android.content.res.Configuration
import com.xevrae.ui.AppGlobalContext

enum class Orientation {
    PORTRAIT, LANDSCAPE, UNSPECIFIED
}

fun currentOrientation(): Orientation {
    val context = AppGlobalContext.get() ?: return Orientation.PORTRAIT
    val orientation = context.resources.configuration.orientation
    return when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
        Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
        else -> Orientation.UNSPECIFIED
    }
}