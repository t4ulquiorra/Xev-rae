package com.xevrae.android.service.lyrics

import io.ktor.client.HttpClient

class XevraeLyricsClient(
    private val client: HttpClient? = null,
) {
    suspend fun getLyrics(trackName: String, artistName: String): String? {
        return null
    }

    suspend fun getSyncedLyrics(trackName: String, artistName: String): List<Pair<Long, String>> {
        return emptyList()
    }
}
