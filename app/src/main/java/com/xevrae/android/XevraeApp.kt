package com.xevrae.android

import android.app.Application
import com.xevrae.common.ContextHolder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XevraeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ContextHolder.context = this
        com.xevrae.ui.AppGlobalContext.init(this)
    }
}
