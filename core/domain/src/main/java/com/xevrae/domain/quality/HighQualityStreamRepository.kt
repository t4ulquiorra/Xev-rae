package com.xevrae.domain.quality

/**
 * Domain interface for resolving high-quality streams (Saavn/Qobuz).
 * Implemented in the media3 module.
 */
interface HighQualityStreamRepository {
    suspend fun resolveHighQualityUrl(
        quality: AudioStreamQuality,
        title: String,
        artist: String,
        durationMs: Long?,
    ): String?
}
