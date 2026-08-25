package com.xevrae.android.service.spotify

import io.ktor.client.HttpClient

class Spotify(
    private val client: HttpClient? = null,
) {
    suspend fun getAccessToken(authCode: String): String? {
        return null
    }

    suspend fun getUserProfile(): Map<String, String> {
        return emptyMap()
    }
}
