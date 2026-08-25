package com.xevrae.data.repository

import com.xevrae.data.parser.parseChart
import com.xevrae.data.parser.parseGenreObject
import com.xevrae.data.parser.parseMixedContent
import com.xevrae.data.parser.parseMoodsMomentObject
import com.xevrae.data.parser.parseNewRelease
import com.xevrae.domain.data.model.home.HomeItem
import com.xevrae.domain.data.model.home.chart.Chart
import com.xevrae.domain.data.model.mood.Mood
import com.xevrae.domain.data.model.mood.MoodItem
import com.xevrae.domain.data.model.mood.MoodSection
import com.xevrae.domain.data.model.mood.genre.GenreObject
import com.xevrae.domain.data.model.mood.moodmoments.MoodsMomentObject
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.repository.HomeRepository
import com.xevrae.domain.utils.Resource
import com.xevrae.kotlinytmusicscraper.YouTube
import com.xevrae.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
private data class MoodArtwork(
    val url: String,
    val cachedAt: Long,
) {
    @OptIn(ExperimentalTime::class)
    fun isStale(): Boolean = Clock.System.now().toEpochMilliseconds() - cachedAt > MOOD_ARTWORK_TTL_MILLIS
}

private const val MOOD_ARTWORK_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000

@OptIn(ExperimentalTime::class)
class HomeRepositoryImpl(
    private val dataStoreManager: DataStoreManager,
    private val youTube: YouTube,
) : HomeRepository {
    private val moodJson =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    override fun getHomeData(
        params: String?,
        viewString: String,
        songString: String,
    ): Flow<Resource<Pair<String?, List<HomeItem>>>> =
        flow {
            runCatching {
                val limit = dataStoreManager.homeLimit.first()
                youTube
                    .customQuery(browseId = "FEmusic_home", params = params)
                    .onSuccess { result ->
                        val list: ArrayList<HomeItem> = arrayListOf()
                        if (result.contents
                                ?.singleColumnBrowseResultsRenderer
                                ?.tabs
                                ?.get(
                                    0,
                                )?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.get(
                                    0,
                                )?.musicCarouselShelfRenderer
                                ?.header
                                ?.musicCarouselShelfBasicHeaderRenderer
                                ?.strapline
                                ?.runs
                                ?.get(
                                    0,
                                )?.text != null
                        ) {
                            val accountName =
                                result.contents
                                    ?.singleColumnBrowseResultsRenderer
                                    ?.tabs
                                    ?.get(
                                        0,
                                    )?.tabRenderer
                                    ?.content
                                    ?.sectionListRenderer
                                    ?.contents
                                    ?.get(
                                        0,
                                    )?.musicCarouselShelfRenderer
                                    ?.header
                                    ?.musicCarouselShelfBasicHeaderRenderer
                                    ?.strapline
                                    ?.runs
                                    ?.get(
                                        0,
                                    )?.text ?: ""
                            val accountThumbUrl =
                                result.contents
                                    ?.singleColumnBrowseResultsRenderer
                                    ?.tabs
                                    ?.get(
                                        0,
                                    )?.tabRenderer
                                    ?.content
                                    ?.sectionListRenderer
                                    ?.contents
                                    ?.get(
                                        0,
                                    )?.musicCarouselShelfRenderer
                                    ?.header
                                    ?.musicCarouselShelfBasicHeaderRenderer
                                    ?.thumbnail
                                    ?.musicThumbnailRenderer
                                    ?.thumbnail
                                    ?.thumbnails
                                    ?.get(
                                        0,
                                    )?.url
                                    ?.replace("s88", "s352") ?: ""
                            if (accountName != "" && accountThumbUrl != "") {
                                dataStoreManager.putString("AccountName", accountName)
                                dataStoreManager.putString("AccountThumbUrl", accountThumbUrl)
                            }
                        }
                        val continueParam =
                            result.contents
                                ?.singleColumnBrowseResultsRenderer
                                ?.tabs
                                ?.get(
                                    0,
                                )?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.continuations
                                ?.get(
                                    0,
                                )?.nextContinuationData
                                ?.continuation
                        val data =
                            result.contents
                                ?.singleColumnBrowseResultsRenderer
                                ?.tabs
                                ?.get(
                                    0,
                                )?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                        list.addAll(
                            parseMixedContent(
                                data,
                                viewString,
                                songString,
                            ),
                        )
//                        var count = 0
//                        while (count < limit && continueParam != null) {
//                            youTube
//                                .customQuery(browseId = "", continuation = continueParam)
//                                .onSuccess { response ->
//                                    continueParam =
//                                        response.continuationContents
//                                            ?.sectionListContinuation
//                                            ?.continuations
//                                            ?.get(
//                                                0,
//                                            )?.nextContinuationData
//                                            ?.continuation
//                                    Logger.d("Repository", "continueParam: $continueParam")
//                                    val dataContinue =
//                                        response.continuationContents?.sectionListContinuation?.contents
//                                    list.addAll(
//                                        parseMixedContent(
//                                            dataContinue,
//                                            viewString,
//                                            songString,
//                                        ),
//                                    )
//                                    count++
//                                    Logger.d("Repository", "count: $count")
//                                }.onFailure {
//                                    Logger.e("Repository", "Error: ${it.message}")
//                                    count++
//                                }
//                        }
                        Logger.d("Repository", "List size: ${list.size}")
                        emit(Resource.Success(continueParam to list.toList()))
                    }.onFailure { error ->
                        emit(Resource.Error<Pair<String?, List<HomeItem>>>(error.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getHomeDataContinue(
        continueParam: String,
        viewString: String,
        songString: String
    ): Flow<Resource<Pair<String?, List<HomeItem>>>> = flow {
        youTube
            .customQuery(browseId = "", continuation = continueParam)
            .onSuccess { response ->
                val newContinueParam =
                    response.continuationContents
                        ?.sectionListContinuation
                        ?.continuations
                        ?.get(
                            0,
                        )?.nextContinuationData
                        ?.continuation
                Logger.d("Repository", "continueParam: $continueParam")
                val dataContinue =
                    response.continuationContents?.sectionListContinuation?.contents
                val list =
                    parseMixedContent(
                        dataContinue,
                        viewString,
                        songString,
                    )
                emit(Resource.Success(newContinueParam to list))
            }.onFailure {
                emit(Resource.Error<Pair<String?, List<HomeItem>>>(it.message.toString()))
            }
    }.flowOn(Dispatchers.IO)

    override fun getNewRelease(
        newReleaseString: String,
        musicVideoString: String,
    ): Flow<Resource<List<HomeItem>>> =
        flow {
            youTube
                .newRelease()
                .onSuccess { result ->
                    emit(Resource.Success<List<HomeItem>>(parseNewRelease(result, newReleaseString, musicVideoString)))
                }.onFailure { error ->
                    emit(Resource.Error<List<HomeItem>>(error.message.toString()))
                }
        }.flowOn(Dispatchers.IO)

    override fun getChartData(countryCode: String): Flow<Resource<Chart>> =
        flow {
            runCatching {
                youTube
                    .customQuery("FEmusic_charts", country = countryCode)
                    .onSuccess { result ->
                        val data =
                            result.contents
                                ?.singleColumnBrowseResultsRenderer
                                ?.tabs
                                ?.get(
                                    0,
                                )?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                        val chart = parseChart(data)
                        if (chart != null) {
                            emit(Resource.Success<Chart>(chart))
                        } else {
                            emit(Resource.Error<Chart>("Error"))
                        }
                    }.onFailure { error ->
                        emit(Resource.Error<Chart>(error.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getMoodAndMomentsData(): Flow<Resource<Mood>> =
        flow {
            val cached =
                dataStoreManager.moodAndGenresCache
                    .first()
                    ?.let { runCatching { moodJson.decodeFromString<Mood>(it) }.getOrNull() }
            if (cached != null) {
                emit(Resource.Success<Mood>(cached))
            }
            runCatching {
                youTube
                    .moodAndGenres()
                    .onSuccess { result ->
                        val sections =
                            result.map { section ->
                                MoodSection(
                                    title = section.title,
                                    items =
                                        section.items.map { item ->
                                            MoodItem(
                                                title = item.title,
                                                params = item.endpoint.params ?: "",
                                                stripeColor = item.stripeColor,
                                            )
                                        },
                                )
                            }
                        val mood = Mood(sections)
                        emit(Resource.Success<Mood>(mood))
                        dataStoreManager.setMoodAndGenresCache(moodJson.encodeToString(mood))
                    }.onFailure { e ->
                        if (cached == null) {
                            emit(Resource.Error<Mood>(e.message.toString()))
                        }
                    }
            }
        }.flowOn(Dispatchers.IO)

    private val artworkCacheMutex = Mutex()
    private var memoryArtworkCache: Map<String, MoodArtwork>? = null

    private suspend fun getArtworkFromCache(params: String): String? =
        artworkCacheMutex.withLock {
            val cache = memoryArtworkCache ?: readMoodArtworkCache().also { memoryArtworkCache = it }
            val hit = cache[params]
            if (hit != null && !hit.isStale()) {
                hit.url
            } else {
                null
            }
        }

    private suspend fun saveMoodArtwork(params: String, url: String) {
        val entry = MoodArtwork(url, Clock.System.now().toEpochMilliseconds())
        artworkCacheMutex.withLock {
            val currentCache = memoryArtworkCache ?: readMoodArtworkCache()
            val updated = currentCache + (params to entry)
            memoryArtworkCache = updated
            dataStoreManager.setMoodArtworkCache(moodJson.encodeToString(updated))
        }
    }

    override fun getMoodCategoryArtwork(params: String): Flow<String?> =
        flow {
            val cachedUrl = getArtworkFromCache(params)
            if (cachedUrl != null) {
                emit(cachedUrl)
                return@flow
            }
            val resolved =
                youTube
                    .customQuery(
                        browseId = "FEmusic_moods_and_genres_category",
                        params = params,
                    ).getOrNull()
                    ?.let { parseMoodsMomentObject(it) }
                    ?.items
                    ?.firstNotNullOfOrNull { section ->
                        section.contents
                            .firstNotNullOfOrNull { it.thumbnails?.lastOrNull()?.url }
                    }
            emit(resolved)
            if (resolved != null) {
                saveMoodArtwork(params, resolved)
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun readMoodArtworkCache(): Map<String, MoodArtwork> =
        dataStoreManager.moodArtworkCache
            .first()
            ?.let { runCatching { moodJson.decodeFromString<Map<String, MoodArtwork>>(it) }.getOrNull() }
            .orEmpty()

    override fun getMoodData(params: String): Flow<Resource<MoodsMomentObject>> =
        flow {
            runCatching {
                youTube
                    .customQuery(
                        browseId = "FEmusic_moods_and_genres_category",
                        params = params,
                    ).onSuccess { result ->
                        val data = parseMoodsMomentObject(result)
                        if (data != null) {
                            emit(Resource.Success<MoodsMomentObject>(data))
                        } else {
                            emit(Resource.Error<MoodsMomentObject>("Error"))
                        }
                    }.onFailure { e ->
                        emit(Resource.Error<MoodsMomentObject>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getGenreData(params: String): Flow<Resource<GenreObject>> =
        flow {
            runCatching {
                youTube
                    .customQuery(
                        browseId = "FEmusic_moods_and_genres_category",
                        params = params,
                    ).onSuccess { result ->
                        val data = parseGenreObject(result)
                        if (data != null) {
                            emit(Resource.Success<GenreObject>(data))
                        } else {
                            emit(Resource.Error<GenreObject>("Error"))
                        }
                    }.onFailure { e ->
                        emit(Resource.Error<GenreObject>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)
}