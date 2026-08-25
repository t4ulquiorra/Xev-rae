package com.xevrae

enum class Platform {
    Android,
    Desktop,
    Ios,
    Web,
}

fun getPlatform(): Platform = Platform.Android
