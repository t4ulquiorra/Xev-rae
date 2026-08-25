package com.xevrae.media3.service.quality

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

// ─── Data models ─────────────────────────────────────────────────────────────

data class SaavnDownloadUrl(
    @SerializedName("quality") val quality: String = "",
    @SerializedName("url")     val url: String     = ""
)

data class SaavnArtistItem(
    @SerializedName("id")   val id: String   = "",
    @SerializedName("name") val name: String = ""
)

data class SaavnArtists(
    @SerializedName("primary")  val primary:  List<SaavnArtistItem> = emptyList(),
    @SerializedName("featured") val featured: List<SaavnArtistItem> = emptyList(),
    @SerializedName("all")      val all:      List<SaavnArtistItem> = emptyList()
)

data class SaavnSong(
    @SerializedName("id")              val id:              String                 = "",
    @SerializedName("name")            val name:            String                 = "",
    @SerializedName("duration")        val duration:        Int?                   = null,
    @SerializedName("explicitContent") val explicitContent: Boolean                = false,
    @SerializedName("artists")         val artists:         SaavnArtists           = SaavnArtists(),
    @SerializedName("downloadUrl")     val downloadUrl:     List<SaavnDownloadUrl> = emptyList()
)

data class SaavnSearchSongsResult(
    @SerializedName("total")   val total: Int               = 0,
    @SerializedName("results") val results: List<SaavnSong> = emptyList()
)

data class SaavnSearchResponse(
    @SerializedName("success") val success: Boolean                 = false,
    @SerializedName("data")    val data:    SaavnSearchSongsResult? = null
)

data class SaavnSongResponse(
    @SerializedName("success") val success: Boolean         = false,
    @SerializedName("data")    val data:    List<SaavnSong> = emptyList()
)

// ─── Service ─────────────────────────────────────────────────────────────────

internal object SaavnService {

    private const val BASE_URL = "https://meloapi.vercel.app/api/"

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .callTimeout(4, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Xevrae/1.0")
                    .build()
            )
        }
        .build()

    suspend fun searchSongs(query: String): Result<List<SaavnSong>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "${BASE_URL}search/songs?query=${java.net.URLEncoder.encode(query, "UTF-8")}&limit=5"
            val request = Request.Builder().url(url).get().build()
            val body = client.newCall(request).execute().use { it.body?.string().orEmpty() }
            val response = gson.fromJson(body, SaavnSearchResponse::class.java)
            if (!response.success) throw NoSuchElementException("No results for: $query")
            response.data?.results.orEmpty().ifEmpty { throw NoSuchElementException("Empty results for: $query") }
        }
    }

    suspend fun getBestStreamUrl(saavnSongId: String, quality: String = "320kbps"): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "${BASE_URL}songs/$saavnSongId"
            val request = Request.Builder().url(url).get().build()
            val body = client.newCall(request).execute().use { it.body?.string().orEmpty() }
            val response = gson.fromJson(body, SaavnSongResponse::class.java)
            if (!response.success) return@runCatching null
            val urls = response.data.firstOrNull()?.downloadUrl.orEmpty().filter { it.url.isNotBlank() }
            urls.firstOrNull { it.quality.equals(quality, ignoreCase = true) }?.url
                ?: urls.firstOrNull { it.quality.equals("320kbps", ignoreCase = true) }?.url
                ?: urls.lastOrNull()?.url
        }.getOrNull()
    }
}
