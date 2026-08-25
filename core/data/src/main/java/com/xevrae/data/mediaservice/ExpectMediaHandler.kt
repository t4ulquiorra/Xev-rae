package com.xevrae.data.mediaservice

import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler
import com.xevrae.domain.mediaservice.player.MediaPlayerInterface
import com.xevrae.domain.repository.AnalyticsRepository
import com.xevrae.domain.repository.LocalPlaylistRepository
import com.xevrae.domain.repository.SongRepository
import com.xevrae.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: CoroutineScope,
    player: MediaPlayerInterface,
): MediaPlayerHandler =
    MediaServiceHandlerImpl(
        dataStoreManager,
        songRepository,
        streamRepository,
        localPlaylistRepository,
        analyticsRepository,
        coroutineScope,
        player,
    )
