package com.xevrae.data.repository

import com.xevrae.common.MERGING_DATA_TYPE
import com.xevrae.common.QUALITY
import com.xevrae.common.VIDEO_QUALITY
import com.xevrae.data.db.datasource.LocalDataSource
import com.xevrae.data.mapping.toSponsorSkipSegments
import com.xevrae.data.mapping.toTrack
import com.xevrae.domain.data.entities.NewFormatEntity
import com.xevrae.domain.data.model.browse.album.Track
import com.xevrae.domain.data.model.mediaService.SponsorSkipSegments
import com.xevrae.domain.extension.isBefore
import com.xevrae.domain.extension.now
import com.xevrae.domain.extension.plusSeconds
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.repository.StreamRepository
import com.xevrae.domain.utils.Resource
import com.xevrae.kotlinytmusicscraper.YouTube
import com.xevrae.domain.quality.AudioStreamQuality
import com.xevrae.domain.quality.HighQualityStreamRepository
import com.xevrae.domain.repository.SongRepository
import com.xevrae.kotlinytmusicscraper.models.MediaType
import com.xevrae.kotlinytmusicscraper.models.response.PlayerResponse
import com.xevrae.kotlinytmusicscraper.utils.decodeBase64
import com.xevrae.kotlinytmusicscraper.utils.decodeTidalManifest
import com.xevrae.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class StreamRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
    private val highQualityStreamRepository: HighQualityStreamRepository? = null,
    private val songRepository: SongRepository? = null,
) : StreamRepository {
    override suspend fun insertNewFormat(newFormat: NewFormatEntity) =
        withContext(Dispatchers.IO) {
            localDataSource.insertNewFormat(newFormat)
        }

    override fun getNewFormat(videoId: String): Flow<NewFormatEntity?> = flow { emit(localDataSource.getNewFormat(videoId)) }.flowOn(Dispatchers.Main)

    override suspend fun getFormatFlow(videoId: String) = localDataSource.getNewFormatAsFlow(videoId)

    override suspend fun updateFormat(videoId: String) {
        localDataSource.getNewFormat(videoId)?.let { oldFormat ->
            if (oldFormat.expiredTime.isBefore(now())) {
                youTube.player(videoId).onSuccess { triple ->
                    val response = triple.second
                    localDataSource.updateNewFormat(
                        oldFormat.copy(
                            expiredTime = now().plusSeconds(response.streamingData?.expiresInSeconds?.toLong() ?: 0L),
                            playbackTrackingVideostatsPlaybackUrl = response.playbackTracking?.videostatsPlaybackUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                            playbackTrackingAtrUrl = response.playbackTracking?.atrUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                            playbackTrackingVideostatsWatchtimeUrl = response.playbackTracking?.videostatsWatchtimeUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                            cpn = triple.first,
                        ),
                    )
                }
            }
        }
    }

    override fun getStream(
        dataStoreManager: DataStoreManager,
        videoId: String,
        isDownloading: Boolean,
        isVideo: Boolean,
        muxed: Boolean,
    ): Flow<String?> =
        flow {
            // Fast Path Bypass (Calls the expect function)
            val fastUrl = getFastStreamUrl(videoId, isVideo)
            if (fastUrl != null) {
                Logger.w("Stream", "Fast URL obtained via XiaoRi engine")
                insertNewFormat(
                    NewFormatEntity(
                        videoId = videoId,
                        itag = 0,
                        mimeType = "audio/mp4",
                        codecs = "mp4a.40.2",
                        bitrate = 0,
                        sampleRate = null,
                        contentLength = null,
                        loudnessDb = null,
                        lengthSeconds = null,
                        playbackTrackingVideostatsPlaybackUrl = null,
                        playbackTrackingAtrUrl = null,
                        playbackTrackingVideostatsWatchtimeUrl = null,
                        cpn = null,
                        expiredTime = now().plusSeconds(3600L),
                        audioUrl = if (!isVideo) fastUrl else null,
                        videoUrl = if (isVideo) fastUrl else null,
                    )
                )
                emit(fastUrl)
                return@flow
            }

            val itag = if (isDownloading) QUALITY.itags.getOrNull(QUALITY.items.indexOf(dataStoreManager.downloadQuality.first())) else QUALITY.itags.getOrNull(QUALITY.items.indexOf(dataStoreManager.quality.first()))
            val videoItag = if (!muxed) VIDEO_QUALITY.itags.getOrNull(VIDEO_QUALITY.items.indexOf(if (isDownloading) dataStoreManager.videoDownloadQuality.first() else dataStoreManager.videoQuality.first())) ?: 134 else 18
            val qualityMode = AudioStreamQuality.from(if (isDownloading) dataStoreManager.downloadQuality.first() else dataStoreManager.quality.first())
            
            if (!isVideo && !muxed && qualityMode != AudioStreamQuality.YOUTUBE) {
                val dbSong = songRepository?.getSongById(videoId)?.first()
                val title = dbSong?.title.orEmpty()
                val artist = dbSong?.artistName?.joinToString(", ").orEmpty().replace(" - Topic", "")
                val durationMs = dbSong?.durationSeconds?.let { if (it > 0) it * 1000L else null }
                val highQualityUrl = highQualityStreamRepository?.resolveHighQualityUrl(qualityMode, title, artist, durationMs)
                if (highQualityUrl != null) {
                    insertNewFormat(
                        NewFormatEntity(
                            videoId = videoId,
                            itag = 0,
                            mimeType = if (qualityMode == AudioStreamQuality.LOSSLESS) "audio/flac" else "audio/mp4",
                            codecs = if (qualityMode == AudioStreamQuality.LOSSLESS) "flac" else "mp4a.40.2",
                            bitrate = if (qualityMode == AudioStreamQuality.SAAVN) 320000 else null,
                            sampleRate = null,
                            contentLength = null,
                            loudnessDb = null,
                            lengthSeconds = dbSong?.durationSeconds,
                            playbackTrackingVideostatsPlaybackUrl = null,
                            playbackTrackingAtrUrl = null,
                            playbackTrackingVideostatsWatchtimeUrl = null,
                            cpn = null,
                            expiredTime = now().plusSeconds(3600L),
                            audioUrl = highQualityUrl,
                            videoUrl = null,
                        )
                    )
                    emit(highQualityUrl)
                    return@flow
                }
            }

            youTube.player(videoId, noLogIn = muxed).onSuccess { data ->
                val response = data.second
                val formatList = mutableListOf<PlayerResponse.StreamingData.Format>()
                formatList.addAll(response.streamingData?.formats?.filter { it.url.isNullOrEmpty().not() } ?: emptyList())
                formatList.addAll(response.streamingData?.adaptiveFormats?.filter { it.url.isNullOrEmpty().not() } ?: emptyList())
                
                val videoFormat = formatList.find { it.itag == videoItag } ?: formatList.find { it.itag == 136 } ?: formatList.find { it.itag == 134 } ?: formatList.find { !it.isAudio && it.url.isNullOrEmpty().not() }
                val audioFormat = formatList.find { it.itag == itag } ?: formatList.find { it.itag == 141 } ?: formatList.find { it.isAudio && it.url.isNullOrEmpty().not() }
                var format = if (isVideo) videoFormat else audioFormat
                if (format == null) format = formatList.lastOrNull { it.url.isNullOrEmpty().not() }
                
                val superFormat = formatList.filter { it.audioQuality == "AUDIO_QUALITY_HIGH" }.let { highFormat -> highFormat.firstOrNull { it.itag == 774 && it.url.isNullOrEmpty().not() } ?: highFormat.firstOrNull { it.url.isNullOrEmpty().not() } }
                if (!isVideo && superFormat != null) format = superFormat
                if (muxed) format = formatList.filter { val url = it.url; url != null && youTube.isManifestUrl(url) }.maxByOrNull { it.width ?: 0 } ?: formatList.find { it.itag == videoItag }
                
                val prefer320kbps = dataStoreManager.prefer320kbpsStream.first() == DataStoreManager.TRUE
                val durationSecond = response.videoDetails?.lengthSeconds?.toIntOrNull()
                var tidalBpm: Int? = null; var tidalMusicKey: String? = null; var tidalKeyScale: String? = null
                
                if (prefer320kbps && !isVideo && durationSecond != null && data.third == MediaType.Song) {
                    val q = "${response.videoDetails?.title ?: ""} ${response.videoDetails?.author ?: ""}".replace(Regex("\\((feat\\.|ft.|cùng với|con|mukana|com|avec|合作音乐人: ) "), " ").replace(Regex("( và | & | и | e | und |, |和| dan)"), " ").replace("  ", " ").replace(Regex("([()])"), "").replace(".", " ").replace("  ", " ")
                    val tidalResult = youTube.getTidalStream(dataStoreManager.your320kbpsUrl.first(), q, durationSecond).getOrNull()
                    tidalBpm = tidalResult?.bpm; tidalMusicKey = tidalResult?.musicKey; tidalKeyScale = tidalResult?.keyScale
                    val audioData = tidalResult?.stream?.data?.manifest?.decodeTidalManifest()
                    if (audioData != null) format = format?.copy(itag = 0, url = audioData.urls.firstOrNull() ?: format.url, mimeType = "${audioData.mimeType}; codecs=\"${audioData.codecs}\"", bitrate = 320000)
                    else if (tidalResult?.stream?.data?.manifest?.decodeBase64()?.contains("MPD") == true) format = format?.copy(itag = 0, url = tidalResult.stream.data?.manifest?.decodeBase64(), bitrate = 320000)
                } else if (!isVideo && durationSecond != null && data.third == MediaType.Song) {
                    val q = "${response.videoDetails?.title ?: ""} ${response.videoDetails?.author ?: ""}".replace(Regex("\\((feat\\.|ft.|cùng với|con|mukana|com|avec|合作音乐人: ) "), " ").replace(Regex("( và | & | и | e | und |, |和| dan)"), " ").replace("  ", " ").replace(Regex("([()])"), "").replace(".", " ").replace("  ", " ")
                    youTube.searchTidalMetadata(dataStoreManager.your320kbpsUrl.first(), q, durationSecond).onSuccess { tidalBpm = it.bpm; tidalMusicKey = it.musicKey; tidalKeyScale = it.keyScale }
                }
                
                insertNewFormat(
                    NewFormatEntity(
                        videoId = if (VIDEO_QUALITY.itags.contains(format?.itag)) "${MERGING_DATA_TYPE.VIDEO}$videoId" else videoId,
                        itag = format?.itag ?: itag ?: 141,
                        mimeType = Regex("""([^;]+);\s*codecs=["']([^"']+)["']""").find(format?.mimeType ?: "")?.groupValues?.getOrNull(1) ?: format?.mimeType ?: "",
                        codecs = Regex("""([^;]+);\s*codecs=["']([^"']+)["']""").find(format?.mimeType ?: "")?.groupValues?.getOrNull(2) ?: format?.mimeType ?: "",
                        bitrate = format?.bitrate,
                        sampleRate = format?.audioSampleRate,
                        contentLength = format?.contentLength,
                        loudnessDb = response.playerConfig?.audioConfig?.loudnessDb?.toFloat(),
                        lengthSeconds = response.videoDetails?.lengthSeconds?.toInt(),
                        playbackTrackingVideostatsPlaybackUrl = response.playbackTracking?.videostatsPlaybackUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                        playbackTrackingAtrUrl = response.playbackTracking?.atrUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                        playbackTrackingVideostatsWatchtimeUrl = response.playbackTracking?.videostatsWatchtimeUrl?.baseUrl?.replace("https://s.youtube.com", "https://music.youtube.com"),
                        cpn = data.first,
                        expiredTime = now().plusSeconds(response.streamingData?.expiresInSeconds?.toLong() ?: 0L),
                        audioUrl = if (muxed) response.streamingData?.hlsManifestUrl else format?.url,
                        videoUrl = if (muxed) response.streamingData?.hlsManifestUrl else videoFormat?.url,
                        bpm = tidalBpm,
                        musicKey = tidalMusicKey,
                        keyScale = tidalKeyScale,
                    )
                )
                
                if (data.first != null) {
                    emit(
                        if (prefer320kbps) format?.url
                        else if (muxed) response.streamingData?.hlsManifestUrl
                        else format?.url?.let { url -> if (youTube.isManifestUrl(url)) url.plus("&cpn=${data.first}") else url.plus("&cpn=${data.first}&range=0-${format.contentLength ?: 10000000}") }
                    )
                } else {
                    emit(
                        if (prefer320kbps) format?.url
                        else if (muxed) response.streamingData?.hlsManifestUrl
                        else format?.url?.let { url -> if (youTube.isManifestUrl(url)) url else url.plus("&range=0-${format.contentLength ?: 10000000}") }
                    )
                }
            }.onFailure {
                Logger.e("Stream", "Error: ${it.message}")
                emit(null)
            }
        }.flowOn(Dispatchers.IO)

    override fun initPlayback(playback: String, atr: String, watchTime: String, cpn: String, playlistId: String?): Flow<Pair<Int, Float>> = flow { youTube.initPlayback(playback, atr, watchTime, cpn, playlistId).onSuccess { emit(it) }.onFailure { emit(Pair(0, 0f)) } }.flowOn(Dispatchers.IO)
    override fun updateWatchTimeFull(watchTime: String, cpn: String, playlistId: String?): Flow<Int> = flow { runCatching { youTube.updateWatchTimeFull(watchTime, cpn, playlistId).onSuccess { emit(it) }.onFailure { emit(0) } } }.flowOn(Dispatchers.IO)
    override fun updateWatchTime(playbackTrackingVideostatsWatchtimeUrl: String, watchTimeList: ArrayList<Float>, cpn: String, playlistId: String?): Flow<Int> = flow { runCatching { youTube.updateWatchTime(playbackTrackingVideostatsWatchtimeUrl, watchTimeList, cpn, playlistId).onSuccess { emit(it) }.onFailure { emit(0) } } }.flowOn(Dispatchers.IO)
    override fun getSkipSegments(videoId: String): Flow<Resource<List<SponsorSkipSegments>>> = flow { youTube.getSkipSegments(videoId).onSuccess { emit(Resource.Success(it.map { it.toSponsorSkipSegments() })) }.onFailure { emit(Resource.Error(it.message ?: "Unknown error")) } }.flowOn(Dispatchers.IO)
    override fun getFullMetadata(videoId: String): Flow<Resource<Track>> = flow { youTube.getFullMetadata(videoId).onSuccess { emit(Resource.Success(it.toTrack())) }.onFailure { emit(Resource.Error(it.message ?: "Unknown error")) } }.flowOn(Dispatchers.IO)
    override fun is403Url(url: String) = flow { emit(youTube.is403Url(url)) }.flowOn(Dispatchers.IO)

    override suspend fun invalidateFormat(videoId: String) {
        withContext(Dispatchers.IO) {
            localDataSource.getNewFormat(videoId)?.let { localDataSource.updateNewFormat(it.copy(expiredTime = now().plusSeconds(-1))) }
        }
    }
}
