package com.xevrae.android.domain.quality

interface HighQualityStreamRepository {
    suspend fun getHighQualityStream(songId: String) // TODO: Implement
}
