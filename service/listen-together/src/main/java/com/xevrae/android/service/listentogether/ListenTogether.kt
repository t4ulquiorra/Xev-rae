package com.xevrae.android.service.listentogether

import io.ktor.client.HttpClient

class ListenTogether(
    private val client: HttpClient? = null,
) {
    suspend fun createSession(): String {
        return "session-id"
    }

    suspend fun joinSession(sessionId: String): Boolean {
        return true
    }

    suspend fun leaveSession(): Boolean {
        return true
    }
}
