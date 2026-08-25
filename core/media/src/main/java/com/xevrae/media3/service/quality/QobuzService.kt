package com.xevrae.media3.service.quality

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// ─── Models ──────────────────────────────────────────────────────────────────

data class QobuzEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: String? = null,
)

data class QobuzSearchData(
    val tracks: QobuzTrackList? = null,
)

data class QobuzTrackList(
    val total: Int = 0,
    val items: List<QobuzTrack> = emptyList(),
)

data class QobuzTrack(
    val id: Long = 0,
    val title: String = "",
    val duration: Int = 0,
    val performer: QobuzPerformer? = null,
    @SerializedName("maximum_bit_depth") val maximumBitDepth: Int = 0,
    @SerializedName("maximum_sampling_rate") val maximumSamplingRate: Float = 0f,
    val streamable: Boolean = true,
)

data class QobuzPerformer(
    val name: String = "",
)

data class QobuzDownloadData(
    val url: String? = null,
)

// ─── Service ─────────────────────────────────────────────────────────────────

internal object QobuzService {

    private const val BASE_URL = "https://qobuz.kennyy.com.br/api"

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "Xevrae/1.0")
                    .build()
            )
        }
        .build()

    suspend fun search(query: String): QobuzSearchData = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/get-music?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val request = Request.Builder().url(url).get().build()
        val body = client.newCall(request).execute().use { it.body?.string().orEmpty() }
        val envelope = gson.fromJson(body, QobuzEnvelopeSearch::class.java)
        if (!envelope.success || envelope.data == null) throw Exception(envelope.error ?: "Qobuz search failed")
        envelope.data
    }

    suspend fun getFileUrl(trackId: Long, quality: Int = 27): QobuzDownloadData = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/download-music?track_id=$trackId&quality=$quality"
        val request = Request.Builder().url(url).get().build()
        val body = client.newCall(request).execute().use { it.body?.string().orEmpty() }
        val envelope = gson.fromJson(body, QobuzEnvelopeDownload::class.java)
        if (!envelope.success || envelope.data == null) throw Exception(envelope.error ?: "Qobuz download failed")
        envelope.data
    }

    // Gson needs concrete types for generic deserialization
    private data class QobuzEnvelopeSearch(
        val success: Boolean = false,
        val data: QobuzSearchData? = null,
        val error: String? = null,
    )
    private data class QobuzEnvelopeDownload(
        val success: Boolean = false,
        val data: QobuzDownloadData? = null,
        val error: String? = null,
    )
}
