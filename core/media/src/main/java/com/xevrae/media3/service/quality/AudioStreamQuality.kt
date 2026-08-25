package com.xevrae.media3.service.quality

import com.xevrae.common.QUALITY
import com.xevrae.domain.quality.AudioStreamQuality

/**
 * Maps Xevrae's QUALITY.items strings to domain AudioStreamQuality enum.
 */
fun audioStreamQualityFrom(qualityItem: String?): AudioStreamQuality = when (qualityItem) {
    QUALITY.items.getOrNull(2)?.toString() -> AudioStreamQuality.SAAVN
    QUALITY.items.getOrNull(3)?.toString() -> AudioStreamQuality.LOSSLESS
    else -> AudioStreamQuality.YOUTUBE
}
