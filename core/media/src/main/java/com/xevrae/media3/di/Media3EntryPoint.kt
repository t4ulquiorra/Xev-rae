package com.xevrae.media3.di

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import com.xevrae.common.Config.CANVAS_CACHE
import com.xevrae.common.Config.MAIN_PLAYER
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@UnstableApi
@EntryPoint
@InstallIn(SingletonComponent::class)
interface Media3EntryPoint {
    @Named(CANVAS_CACHE)
    fun getCanvasCache(): SimpleCache

    @Named(MAIN_PLAYER)
    fun getMainPlayer(): Player

    fun getMediaPlayerHandler(): MediaPlayerHandler
}
