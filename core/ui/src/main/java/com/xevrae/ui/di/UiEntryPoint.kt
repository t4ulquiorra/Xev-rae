package com.xevrae.ui.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler
import com.xevrae.domain.repository.AccountRepository
import com.xevrae.domain.repository.AlbumRepository
import com.xevrae.domain.repository.AnalyticsRepository
import com.xevrae.domain.repository.ArtistRepository
import com.xevrae.domain.repository.CommonRepository
import com.xevrae.domain.repository.HomeRepository
import com.xevrae.domain.repository.LocalPlaylistRepository
import com.xevrae.domain.repository.LyricsCanvasRepository
import com.xevrae.domain.repository.PlaylistRepository
import com.xevrae.domain.repository.PodcastRepository
import com.xevrae.domain.repository.SearchRepository
import com.xevrae.domain.repository.SongRepository
import com.xevrae.domain.repository.StreamRepository
import com.xevrae.domain.repository.UpdateRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UiEntryPoint {
    fun mediaPlayerHandler(): MediaPlayerHandler
    fun dataStoreManager(): DataStoreManager
    fun songRepository(): SongRepository
    fun localPlaylistRepository(): LocalPlaylistRepository
    fun albumRepository(): AlbumRepository
    fun artistRepository(): ArtistRepository
    fun playlistRepository(): PlaylistRepository
    fun searchRepository(): SearchRepository
    fun homeRepository(): HomeRepository
    fun analyticsRepository(): AnalyticsRepository
    fun accountRepository(): AccountRepository
    fun podcastRepository(): PodcastRepository
    fun streamRepository(): StreamRepository
    fun updateRepository(): UpdateRepository
    fun commonRepository(): CommonRepository
    fun lyricsCanvasRepository(): LyricsCanvasRepository
}

@Composable
inline fun <reified T> hiltInject(): T {
    val context = LocalContext.current.applicationContext
    val entryPoint = EntryPointAccessors.fromApplication(context, UiEntryPoint::class.java)
    return when (T::class) {
        MediaPlayerHandler::class -> entryPoint.mediaPlayerHandler() as T
        DataStoreManager::class -> entryPoint.dataStoreManager() as T
        SongRepository::class -> entryPoint.songRepository() as T
        LocalPlaylistRepository::class -> entryPoint.localPlaylistRepository() as T
        AlbumRepository::class -> entryPoint.albumRepository() as T
        ArtistRepository::class -> entryPoint.artistRepository() as T
        PlaylistRepository::class -> entryPoint.playlistRepository() as T
        SearchRepository::class -> entryPoint.searchRepository() as T
        HomeRepository::class -> entryPoint.homeRepository() as T
        AnalyticsRepository::class -> entryPoint.analyticsRepository() as T
        AccountRepository::class -> entryPoint.accountRepository() as T
        PodcastRepository::class -> entryPoint.podcastRepository() as T
        StreamRepository::class -> entryPoint.streamRepository() as T
        UpdateRepository::class -> entryPoint.updateRepository() as T
        CommonRepository::class -> entryPoint.commonRepository() as T
        LyricsCanvasRepository::class -> entryPoint.lyricsCanvasRepository() as T
        else -> throw IllegalArgumentException("Unknown type for hiltInject: ${T::class}")
    }
}
