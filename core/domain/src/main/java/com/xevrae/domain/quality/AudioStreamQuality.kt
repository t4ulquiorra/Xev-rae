package com.xevrae.domain.quality

/**
 * Typed audio quality modes.
 * Mirrors XiaoRi's AudioQuality enum (OPUS/YOUTUBE, SAAVN, LOSSLESS).
 */
enum class AudioStreamQuality {
    YOUTUBE,  // Standard YouTube stream (default)
    SAAVN,    // JioSaavn 320kbps
    LOSSLESS; // Qobuz FLAC (falls back to Saavn, then YouTube)

    companion object {
        // Quality item strings must match QUALITY.items in common/Config.kt
        private const val SAAVN_ITEM  = "Saavn (320kbps)"
        private const val LOSSLESS_ITEM = "Qobuz (Lossless)"

        fun from(qualityItem: String?): AudioStreamQuality = when (qualityItem) {
            SAAVN_ITEM   -> SAAVN
            LOSSLESS_ITEM -> LOSSLESS
            else          -> YOUTUBE
        }
    }
}
