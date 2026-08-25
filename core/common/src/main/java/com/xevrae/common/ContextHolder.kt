package com.xevrae.common

import android.annotation.SuppressLint
import android.content.Context
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
object ContextHolder {
    private var contextRef: WeakReference<Context>? = null

    var context: Context
        get() = contextRef?.get() ?: error("ContextHolder context not initialized")
        set(value) {
            contextRef = WeakReference(value.applicationContext)
        }

    fun init(ctx: Context) {
        contextRef = WeakReference(ctx.applicationContext)
    }

    fun get(): Context? = contextRef?.get()
}
