package com.xevrae.android.crashlytics.noop

object CrashReporter {
    fun init() {
        // No-op
    }
    
    fun reportException(t: Throwable) {
        // No-op
    }
}
