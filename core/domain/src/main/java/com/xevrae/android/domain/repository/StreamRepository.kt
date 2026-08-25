package com.xevrae.android.domain.repository

interface StreamRepository {
    suspend fun getStreamUrl(songId: String) // TODO: Implement
}
