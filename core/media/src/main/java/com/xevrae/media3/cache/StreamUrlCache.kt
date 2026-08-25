package com.xevrae.media3.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory stream URL cache shared between ResolvingDataSource and ExoPlayerAdapter.
 * videoId -> Pair(url, expiryEpochMs)
 * Mirrors XiaoRi's songUrlCache for sub-millisecond URL lookups.
 */
internal object StreamUrlCache {
    private val cache = ConcurrentHashMap<String, Pair<String, Long>>()

    fun get(mediaId: String): String? =
        cache[mediaId]?.takeIf { it.second > System.currentTimeMillis() }?.first

    fun put(mediaId: String, url: String, expiryMs: Long) {
        cache[mediaId] = Pair(url, expiryMs)
    }

    fun remove(videoId: String) {
        cache.remove(videoId)
    }

    fun clear() {
        cache.clear()
    }
}
