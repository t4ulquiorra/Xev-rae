package com.xevrae.data.di

import android.content.Context
import com.xevrae.common.Config
import com.xevrae.data.mediaservice.MediaServiceHandlerImpl
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler
import com.xevrae.domain.mediaservice.player.MediaPlayerInterface
import com.xevrae.domain.repository.AnalyticsRepository
import com.xevrae.domain.repository.LocalPlaylistRepository
import com.xevrae.domain.repository.SongRepository
import com.xevrae.domain.repository.StreamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaHandlerModule {

    @Provides
    @Singleton
    fun provideMediaPlayerHandler(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager,
        songRepository: SongRepository,
        streamRepository: StreamRepository,
        localPlaylistRepository: LocalPlaylistRepository,
        analyticsRepository: AnalyticsRepository,
        @Named(Config.SERVICE_SCOPE) coroutineScope: CoroutineScope,
        player: MediaPlayerInterface,
    ): MediaPlayerHandler {
        return MediaServiceHandlerImpl(
            context = context,
            dataStoreManager = dataStoreManager,
            songRepository = songRepository,
            streamRepository = streamRepository,
            localPlaylistRepository = localPlaylistRepository,
            analyticsRepository = analyticsRepository,
            coroutineScope = coroutineScope,
            player = player,
        )
    }
}
