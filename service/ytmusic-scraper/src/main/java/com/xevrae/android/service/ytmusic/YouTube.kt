package com.xevrae.android.service.ytmusic

import io.ktor.client.HttpClient

class YouTube(
    private val client: HttpClient? = null,
) {
    suspend fun search(query: String): List<String> {
        return emptyList()
    }

    suspend fun getSongDetails(videoId: String): Map<String, String> {
        return emptyMap()
    }
}
