package com.xevrae.android.domain.repository

interface AnalyticsRepository {
    suspend fun logEvent(eventName: String) // TODO: Implement
}
