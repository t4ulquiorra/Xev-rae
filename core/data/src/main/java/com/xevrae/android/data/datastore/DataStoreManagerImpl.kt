package com.xevrae.android.data.datastore

import com.xevrae.android.domain.manager.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DataStoreManagerImpl @Inject constructor() : DataStoreManager {
    override val maxSongCacheSize: Flow<Int>
        get() = flowOf(100) // Stub

    override suspend fun setMaxSongCacheSize(size: Int) {
        // Stub implementation
    }
}
