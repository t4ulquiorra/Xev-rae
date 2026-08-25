package com.xevrae.ui

import android.annotation.SuppressLint
import android.content.Context
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object AppGlobalContext {
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    fun get(): Context? = contextRef?.get()
}
