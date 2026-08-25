package com.xevrae.android.domain.manager

import kotlinx.coroutines.flow.Flow

interface DataStoreManager {
    val maxSongCacheSize: Flow<Int>
    suspend fun setMaxSongCacheSize(size: Int)
}
