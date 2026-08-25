package com.xevrae.data.di

import DatabaseDao
import com.xevrae.common.Config.SERVICE_SCOPE
import com.xevrae.data.io.fileDir
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
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.xevrae.aiservice.AiClient
import org.xevrae.lyrics.XevraeLyricsClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAccountRepository(
        dataStoreManager: DataStoreManager,
        youTube: YouTube,
    ): AccountRepository = AccountRepositoryImpl(dataStoreManager, youTube)

    @Provides
    @Singleton
    fun provideAlbumRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): AlbumRepository = AlbumRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideArtistRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): ArtistRepository = ArtistRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideCommonRepository(
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
        dataStoreManager: DataStoreManager,
        databaseDao: DatabaseDao,
        httpClient: HttpClient,
        youTube: YouTube,
        spotify: Spotify,
    ): CommonRepository {
        return CommonRepositoryImpl(coroutineScope, dataStoreManager, databaseDao, httpClient, youTube, spotify).apply {
            this.init("${fileDir()}/ytdlp-cookie.txt", databaseDao)
        }
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): HomeRepository = HomeRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideLocalPlaylistRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): LocalPlaylistRepository = LocalPlaylistRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideLyricsCanvasRepository(
        databaseDao: DatabaseDao,
        lyricsClient: XevraeLyricsClient,
        spotify: Spotify,
        dataStoreManager: DataStoreManager,
        aiClient: AiClient,
    ): LyricsCanvasRepository = LyricsCanvasRepositoryImpl(databaseDao, lyricsClient, spotify, dataStoreManager, aiClient)

    @Provides
    @Singleton
    fun providePlaylistRepository(
        databaseDao: DatabaseDao,
        dataStoreManager: DataStoreManager,
        youTube: YouTube,
    ): PlaylistRepository = PlaylistRepositoryImpl(databaseDao, dataStoreManager, youTube)

    @Provides
    @Singleton
    fun providePodcastRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): PodcastRepository = PodcastRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideSearchRepository(
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): SearchRepository = SearchRepositoryImpl(databaseDao, youTube)

    @Provides
    @Singleton
    fun provideSongRepository(
        databaseDao: DatabaseDao,
        dataStoreManager: DataStoreManager,
        youTube: YouTube,
    ): SongRepository = SongRepositoryImpl(databaseDao, dataStoreManager, youTube)

    @Provides
    @Singleton
    fun provideStreamRepository(
        dataStoreManager: DataStoreManager,
        databaseDao: DatabaseDao,
        youTube: YouTube,
    ): StreamRepository = StreamRepositoryImpl(dataStoreManager, databaseDao, null, youTube)

    @Provides
    @Singleton
    fun provideUpdateRepository(
        httpClient: HttpClient,
    ): UpdateRepository = UpdateRepositoryImpl(httpClient)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        databaseDao: DatabaseDao,
    ): AnalyticsRepository = AnalyticsRepositoryImpl(databaseDao)
}
