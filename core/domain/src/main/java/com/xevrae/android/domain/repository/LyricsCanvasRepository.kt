package com.xevrae.android.domain.repository

interface LyricsCanvasRepository {
    suspend fun getLyrics(songId: String) // TODO: Implement
}
