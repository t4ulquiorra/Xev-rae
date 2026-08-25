package com.xevrae.media3.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
import androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.flac.FlacExtractor
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import com.xevrae.common.Config.CANVAS_CACHE
import com.xevrae.common.Config.DOWNLOAD_CACHE
import com.xevrae.common.Config.MAIN_PLAYER
import com.xevrae.common.Config.PLAYER_CACHE
import com.xevrae.common.Config.SERVICE_SCOPE
import com.xevrae.common.MERGING_DATA_TYPE
import com.xevrae.domain.extension.now
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.mediaservice.handler.DownloadHandler
import com.xevrae.domain.mediaservice.handler.MediaPlayerHandler
import com.xevrae.domain.mediaservice.player.MediaPlayerInterface
import com.xevrae.domain.quality.HighQualityStreamRepository
import com.xevrae.domain.repository.CacheRepository
import com.xevrae.domain.repository.HomeRepository
import com.xevrae.domain.repository.LocalPlaylistRepository
import com.xevrae.domain.repository.PlaylistRepository
import com.xevrae.domain.repository.SearchRepository
import com.xevrae.domain.repository.SongRepository
import com.xevrae.domain.repository.StreamRepository
import com.xevrae.logger.Logger
import com.xevrae.media3.cache.StreamUrlCache
import com.xevrae.media3.extension.isFullyCached
import com.xevrae.media3.exoplayer.CrossfadeExoPlayerAdapter
import com.xevrae.media3.repository.CacheRepositoryImpl
import com.xevrae.media3.service.SimpleMediaService
import com.xevrae.media3.service.callback.SimpleMediaSessionCallback
import com.xevrae.media3.service.download.DownloadUtils
import com.xevrae.media3.service.mediasourcefactory.MergingMediaSourceFactory
import com.xevrae.media3.service.quality.QualityStreamResolver
import com.xevrae.media3.utils.CoilBitmapLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@UnstableApi
@Module
@InstallIn(SingletonComponent::class)
object Media3Module {

    @Provides
    @Singleton
    @Named(SERVICE_SCOPE)
    fun provideServiceScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Provides
    @Singleton
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider =
        StandaloneDatabaseProvider(context)

    @Provides
    @Singleton
    @Named(PLAYER_CACHE)
    fun providePlayerCache(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager,
        databaseProvider: DatabaseProvider,
    ): SimpleCache = SimpleCache(
        context.filesDir.resolve("exoplayer"),
        when (val size = runBlocking { dataStoreManager.maxSongCacheSize.first() }) {
            -1 -> NoOpCacheEvictor()
            else -> LeastRecentlyUsedCacheEvictor(size * 1024 * 1024L)
        },
        databaseProvider,
    )

    @Provides
    @Singleton
    @Named(DOWNLOAD_CACHE)
    fun provideDownloadCache(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider,
    ): SimpleCache = SimpleCache(
        context.filesDir.resolve("download"),
        NoOpCacheEvictor(),
        databaseProvider,
    )

    @Provides
    @Singleton
    @Named(CANVAS_CACHE)
    fun provideCanvasCache(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider,
    ): SimpleCache = SimpleCache(
        context.filesDir.resolve("spotifyCanvas"),
        NoOpCacheEvictor(),
        databaseProvider,
    )

    @Provides
    @Singleton
    fun provideDownloadHandler(
        @ApplicationContext context: Context,
        @Named(PLAYER_CACHE) playerCache: SimpleCache,
        @Named(DOWNLOAD_CACHE) downloadCache: SimpleCache,
        dataStoreManager: DataStoreManager,
        databaseProvider: DatabaseProvider,
        streamRepository: StreamRepository,
        songRepository: SongRepository,
    ): DownloadHandler = DownloadUtils(
        context = context,
        playerCache = playerCache,
        downloadCache = downloadCache,
        dataStoreManager = dataStoreManager,
        databaseProvider = databaseProvider,
        streamRepository = streamRepository,
        songRepository = songRepository,
    )

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    fun provideExtractorFactory(): ExtractorsFactory = ExtractorsFactory {
        arrayOf(
            FlacExtractor(FlacExtractor.FLAG_DISABLE_ID3_METADATA),
            MatroskaExtractor(DefaultSubtitleParserFactory()),
            FragmentedMp4Extractor(DefaultSubtitleParserFactory()),
            Mp4Extractor(DefaultSubtitleParserFactory()),
        )
    }

    @Provides
    @Singleton
    fun provideDefaultRenderersFactory(@ApplicationContext context: Context): DefaultRenderersFactory =
        object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean,
            ): AudioSink = DefaultAudioSink.Builder(context)
                .setEnableFloatOutput(enableFloatOutput)
                .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                .setAudioProcessorChain(
                    DefaultAudioSink.DefaultAudioProcessorChain(
                        emptyArray(),
                        SilenceSkippingAudioProcessor(
                            2_000_000,
                            (20_000 / 2_000_000).toFloat(),
                            2_000_000,
                            0,
                            256,
                        ),
                        SonicAudioProcessor(),
                    ),
                ).build()
        }

    @Provides
    @Singleton
    fun provideCoilBitmapLoader(
        @ApplicationContext context: Context,
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
    ): CoilBitmapLoader = CoilBitmapLoader(context, coroutineScope)

    @Provides
    @Singleton
    fun provideHighQualityStreamRepository(): HighQualityStreamRepository = QualityStreamResolver()

    @Provides
    @Singleton
    fun provideCacheRepository(
        @Named(PLAYER_CACHE) playerCache: SimpleCache,
        @Named(DOWNLOAD_CACHE) downloadCache: SimpleCache,
        @Named(CANVAS_CACHE) canvasCache: SimpleCache,
    ): CacheRepository = CacheRepositoryImpl(playerCache, downloadCache, canvasCache)

    @Provides
    @Singleton
    fun provideMergingMediaSourceFactory(
        @ApplicationContext context: Context,
        @Named(DOWNLOAD_CACHE) downloadCache: SimpleCache,
        @Named(PLAYER_CACHE) playerCache: SimpleCache,
        streamRepository: StreamRepository,
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
        dataStoreManager: DataStoreManager,
        extractorsFactory: ExtractorsFactory,
    ): MergingMediaSourceFactory {
        val resolvingFactory = provideResolvingDataSourceFactory(
            provideCacheDataSource(
                downloadCache,
                playerCache,
                context,
                dataStoreManager.getJVMProxy()?.let {
                    Proxy(
                        when (it.type) {
                            DataStoreManager.ProxyType.PROXY_TYPE_HTTP -> Proxy.Type.HTTP
                            DataStoreManager.ProxyType.PROXY_TYPE_SOCKS -> Proxy.Type.SOCKS
                        },
                        java.net.InetSocketAddress(it.host, it.port),
                    )
                },
            ),
            downloadCache,
            playerCache,
            dataStoreManager,
            streamRepository,
            coroutineScope,
        )
        return MergingMediaSourceFactory(
            DefaultMediaSourceFactory(resolvingFactory, extractorsFactory),
            dataStoreManager,
        )
    }

    @Provides
    @Singleton
    fun provideMediaPlayerInterface(
        @ApplicationContext context: Context,
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
        dataStoreManager: DataStoreManager,
        mediaSourceFactory: MergingMediaSourceFactory,
        audioAttributes: AudioAttributes,
        streamRepository: StreamRepository,
    ): MediaPlayerInterface = CrossfadeExoPlayerAdapter(
        context = context,
        coroutineScope = coroutineScope,
        dataStoreManager = dataStoreManager,
        mediaSourceFactory = mediaSourceFactory,
        audioAttributes = audioAttributes,
        streamRepository = streamRepository,
    )

    @Provides
    @Singleton
    @Named(MAIN_PLAYER)
    fun provideMainPlayer(mediaPlayerInterface: MediaPlayerInterface): Player {
        return (mediaPlayerInterface as CrossfadeExoPlayerAdapter).forwardingPlayer
    }

    @Provides
    @Singleton
    fun provideMediaLibrarySessionCallback(
        application: Application,
        @Named(SERVICE_SCOPE) coroutineScope: CoroutineScope,
        mediaPlayerHandler: MediaPlayerHandler,
        searchRepository: SearchRepository,
        songRepository: SongRepository,
        localPlaylistRepository: LocalPlaylistRepository,
        playlistRepository: PlaylistRepository,
        homeRepository: HomeRepository,
        streamRepository: StreamRepository,
    ): MediaLibrarySession.Callback = SimpleMediaSessionCallback(
        application,
        coroutineScope,
        mediaPlayerHandler,
        searchRepository,
        songRepository,
        localPlaylistRepository,
        playlistRepository,
        homeRepository,
        streamRepository,
    )
}

@UnstableApi
private fun provideResolvingDataSourceFactory(
    cacheDataSourceFactory: CacheDataSource.Factory,
    downloadCache: SimpleCache,
    playerCache: SimpleCache,
    dataStoreManager: DataStoreManager,
    streamRepository: StreamRepository,
    coroutineScope: CoroutineScope,
): DataSource.Factory {
    val chunkLength = 10 * 512 * 1024L
    return ResolvingDataSource.Factory(cacheDataSourceFactory) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")
        Logger.w("Stream", mediaId)
        Logger.w("Stream", mediaId.startsWith(MERGING_DATA_TYPE.VIDEO).toString())
        if (downloadCache.isFullyCached(mediaId, dataSpec.position)) {
            if (dataSpec.position == 0L) {
                coroutineScope.launch(Dispatchers.IO) {
                    streamRepository.updateFormat(
                        if (mediaId.contains(MERGING_DATA_TYPE.VIDEO)) {
                            mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO)
                        } else {
                            mediaId
                        },
                    )
                }
            }
            Logger.w("Stream", "Downloaded $mediaId")
            return@Factory dataSpec.subrange(dataSpec.uriPositionOffset, chunkLength)
        }
        if (playerCache.isFullyCached(mediaId, dataSpec.position)) {
            if (dataSpec.position == 0L) {
                coroutineScope.launch(Dispatchers.IO) {
                    streamRepository.updateFormat(
                        if (mediaId.contains(MERGING_DATA_TYPE.VIDEO)) {
                            mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO)
                        } else {
                            mediaId
                        },
                    )
                }
            }
            Logger.w("Stream", "Cached $mediaId")
            return@Factory dataSpec.subrange(dataSpec.uriPositionOffset, chunkLength)
        }
        var dataSpecReturn: DataSpec = dataSpec
        var resolved = false
        runBlocking(Dispatchers.IO) {
            if (mediaId.contains(MERGING_DATA_TYPE.VIDEO)) {
                val id = mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO)
                streamRepository.getNewFormat(id).lastOrNull()?.let {
                    val videoUrl = it.videoUrl
                    if (videoUrl != null && it.expiredTime > now()) {
                        Logger.d("Stream", videoUrl)
                        Logger.w("Stream", "Video from format")
                        val is403Url = streamRepository.is403Url(videoUrl).firstOrNull() != false
                        Logger.d("Stream", "is 403 $is403Url")
                        if (!is403Url) {
                            dataSpecReturn = dataSpec.withUri(videoUrl.toUri()).subrange(dataSpec.uriPositionOffset, chunkLength)
                            resolved = true
                            return@runBlocking
                        }
                    }
                }
                streamRepository
                    .getStream(
                        dataStoreManager,
                        id,
                        isDownloading = false,
                        isVideo = true,
                    ).lastOrNull()
                    ?.let {
                        Logger.d("Stream", it)
                        Logger.w("Stream", "Video")
                        dataSpecReturn = dataSpec.withUri(it.toUri()).subrange(dataSpec.uriPositionOffset, chunkLength)
                        resolved = true
                    }
            } else {
                streamRepository.getNewFormat(mediaId).lastOrNull()?.let {
                    val audioUrl = it.audioUrl
                    if (audioUrl != null && it.expiredTime > now()) {
                        Logger.d("Stream", audioUrl)
                        Logger.w("Stream", "Audio from format")
                        val is403Url = streamRepository.is403Url(audioUrl).firstOrNull() != false
                        Logger.d("Stream", "is 403 $is403Url")
                        if (!is403Url) {
                            dataSpecReturn = dataSpec.withUri(audioUrl.toUri()).subrange(dataSpec.uriPositionOffset, chunkLength)
                            resolved = true
                            return@runBlocking
                        }
                    }
                }
                streamRepository
                    .getStream(
                        dataStoreManager,
                        mediaId,
                        isDownloading = false,
                        isVideo = false,
                    ).lastOrNull()
                    ?.let {
                        Logger.d("Stream", it)
                        Logger.w("Stream", "Audio")
                        dataSpecReturn = dataSpec.withUri(it.toUri()).subrange(dataSpec.uriPositionOffset, chunkLength)
                        resolved = true
                    }
            }
        }
        if (!resolved) {
            Logger.e("Stream", "Failed to resolve stream URL for $mediaId")
            throw java.io.IOException("Failed to resolve stream URL for $mediaId")
        }
        return@Factory dataSpecReturn
    }
}

@UnstableApi
private fun provideCacheDataSource(
    downloadCache: SimpleCache,
    playerCache: SimpleCache,
    context: Context,
    proxy: Proxy? = null,
): CacheDataSource.Factory =
    CacheDataSource
        .Factory()
        .setCache(downloadCache)
        .setUpstreamDataSourceFactory(
            CacheDataSource
                .Factory()
                .setCache(playerCache)
                .setUpstreamDataSourceFactory(
                    DefaultDataSource
                        .Factory(
                            context,
                            OkHttpDataSource.Factory(
                                OkHttpClient
                                    .Builder()
                                    .connectTimeout(30.seconds)
                                    .readTimeout(30.seconds)
                                    .proxy(proxy)
                                    .addInterceptor(
                                        HttpLoggingInterceptor().apply {
                                            level = HttpLoggingInterceptor.Level.HEADERS
                                        },
                                    ).build(),
                            ),
                        ),
                ),
        ).setCacheWriteDataSinkFactory(null)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

@OptIn(UnstableApi::class)
fun startService(
    context: Context,
    serviceConnection: ServiceConnection,
) {
    val intent = Intent(context, SimpleMediaService::class.java)
    try {
        context.startService(intent)
    } catch (e: IllegalStateException) {
        ContextCompat.startForegroundService(context, intent)
    }
    context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    Logger.d("Service", "Service started")
}

@OptIn(UnstableApi::class)
fun stopService(context: Context) {
    context.stopService(Intent(context, SimpleMediaService::class.java))
}

@OptIn(UnstableApi::class)
fun setServiceActivitySession(
    context: Context,
    cls: Class<out Activity>,
    musicService: IBinder?,
) {
    (musicService as? SimpleMediaService.MusicBinder)?.setActivitySession(context, cls)
}
