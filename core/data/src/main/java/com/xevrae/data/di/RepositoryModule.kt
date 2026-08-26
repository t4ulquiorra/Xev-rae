package com.xevrae.data.di

import android.content.Context
import com.xevrae.common.Config.SERVICE_SCOPE
import com.xevrae.data.db.MusicDatabase
import com.xevrae.data.db.datasource.AnalyticsDatasource
import com.xevrae.data.db.datasource.LocalDataSource
import com.xevrae.data.repository.AccountRepositoryImpl
import com.xevrae.data.repository.AlbumRepositoryImpl
import com.xevrae.data.repository.AnalyticsRepositoryImpl
import com.xevrae.data.repository.ArtistRepositoryImpl
import com.xevrae.data.repository.CommonRepositoryImpl
import com.xevrae.data.repository.HomeRepositoryImpl
import com.xevrae.data.repository.LocalPlaylistRepositoryImpl
import com.xevrae.data.repository.LyricsCanvasRepositoryImpl
import com.xevrae.data.repository.PlaylistRepositoryImpl
import com.xevrae.data.repository.PodcastRepositoryImpl
import com.xevrae.data.repository.SearchRepositoryImpl
import com.xevrae.data.repository.SongRepositoryImpl
import com.xevrae.data.repository.StreamRepositoryImpl
import com.xevrae.data.repository.UpdateRepositoryImpl
import com.xevrae.domain.manager.DataStoreManager
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
import com.xevrae.kotlinytmusicscraper.YouTube
import com.xevrae.spotify.Spotify
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import org.simpmusic.aiservice.AiClient
import org.simpmusic.lyrics.SimpMusicLyricsClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAccountRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): AccountRepository = AccountRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideAlbumRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): AlbumRepository = AlbumRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideArtistRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): ArtistRepository = ArtistRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideCommonRepository(
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
        database: MusicDatabase,
        localDataSource: LocalDataSource,
        youTube: YouTube,
        spotify: Spotify,
        aiClient: AiClient,
        dataStoreManager: DataStoreManager,
        @ApplicationContext context: Context,
    ): CommonRepository {
        return CommonRepositoryImpl(
            coroutineScope = coroutineScope,
            database = database,
            localDataSource = localDataSource,
            youTube = youTube,
            spotify = spotify,
            aiClient = aiClient,
        ).apply {
            this.init("${context.filesDir.absolutePath}/ytdlp-cookie.txt", dataStoreManager)
        }
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        dataStoreManager: DataStoreManager,
        youTube: YouTube,
    ): HomeRepository = HomeRepositoryImpl(dataStoreManager, youTube)

    @Provides
    @Singleton
    fun provideLocalPlaylistRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): LocalPlaylistRepository = LocalPlaylistRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideLyricsCanvasRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
        spotify: Spotify,
        lyricsClient: SimpMusicLyricsClient,
        aiClient: AiClient,
    ): LyricsCanvasRepository = LyricsCanvasRepositoryImpl(
        localDataSource = localDataSource,
        youTube = youTube,
        spotify = spotify,
        simpMusicLyrics = lyricsClient,
        aiClient = aiClient,
    )

    @Provides
    @Singleton
    fun providePlaylistRepository(
        dataStoreManager: DataStoreManager,
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): PlaylistRepository = PlaylistRepositoryImpl(dataStoreManager, localDataSource, youTube)

    @Provides
    @Singleton
    fun providePodcastRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): PodcastRepository = PodcastRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideSearchRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): SearchRepository = SearchRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideSongRepository(
        dataStoreManager: DataStoreManager,
        localDataSource: LocalDataSource,
        youTube: YouTube,
        downloadHandlerLazy: dagger.Lazy<com.xevrae.domain.mediaservice.handler.DownloadHandler>,
        mediaPlayerHandlerLazy: dagger.Lazy<com.xevrae.domain.mediaservice.handler.MediaPlayerHandler>,
    ): SongRepository = SongRepositoryImpl(
        dataStoreManager = dataStoreManager,
        localDataSource = localDataSource,
        youTube = youTube,
        downloadHandlerProvider = downloadHandlerLazy,
        mediaPlayerHandlerProvider = mediaPlayerHandlerLazy,
    )

    @Provides
    @Singleton
    fun provideStreamRepository(
        localDataSource: LocalDataSource,
        youTube: YouTube,
    ): StreamRepository = StreamRepositoryImpl(localDataSource, youTube)

    @Provides
    @Singleton
    fun provideUpdateRepository(
        youTube: YouTube,
    ): UpdateRepository = UpdateRepositoryImpl(youTube)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        analyticsDatasource: AnalyticsDatasource,
    ): AnalyticsRepository = AnalyticsRepositoryImpl(analyticsDatasource)
}
