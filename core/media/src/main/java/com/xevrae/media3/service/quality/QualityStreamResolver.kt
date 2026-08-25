package com.xevrae.media3.service.quality

import com.xevrae.domain.quality.AudioStreamQuality
import com.xevrae.domain.quality.HighQualityStreamRepository
import com.xevrae.logger.Logger
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "QualityStreamResolver"

internal class QualityStreamResolver : HighQualityStreamRepository {

    override suspend fun resolveHighQualityUrl(
        quality: AudioStreamQuality,
        title: String,
        artist: String,
        durationMs: Long?,
    ): String? {
        return when (quality) {
            AudioStreamQuality.LOSSLESS -> resolveLossless(title, artist, durationMs)
                ?: resolveSaavn(title, artist, durationMs)
            AudioStreamQuality.SAAVN -> resolveSaavn(title, artist, durationMs)
            AudioStreamQuality.YOUTUBE -> null
        }
    }

    private suspend fun resolveLossless(title: String, artist: String, durationMs: Long?): String? {
        return withTimeoutOrNull(15_000L) {
            runCatching {
                val query = "$artist $title"
                val results = QobuzService.search(query).tracks?.items.orEmpty()
                val valid = results.filter { it.streamable && it.maximumBitDepth >= 16 }
                val best = valid.maxByOrNull { confidence(artist, title, durationMs, it) }
                    ?.takeIf { confidence(artist, title, durationMs, it) >= 0.5f }
                    ?: return@runCatching null
                Logger.d(TAG, "Qobuz match: ${best.title} by ${best.performer?.name}")
                QobuzService.getFileUrl(best.id).url
            }.getOrElse {
                Logger.e(TAG, "Qobuz failed: ${it.message}")
                null
            }
        }
    }

    private suspend fun resolveSaavn(title: String, artist: String, durationMs: Long?): String? {
        return withTimeoutOrNull(15_000L) {
            runCatching {
                val query = "$title $artist"
                    .replace("&", " ").replace(",", " ")
                    .replace(Regex("(?i)\\s*-\\s*topic\\b"), "")
                    .replace(Regex("\\s+"), " ").trim()
                val songs = SaavnService.searchSongs(query).getOrNull().orEmpty()
                if (songs.isEmpty()) return@runCatching null
                val ytDuration = durationMs?.let { it / 1000L } ?: 0L
                val best = songs.maxByOrNull { saavnScore(title, artist, ytDuration, it) }
                    ?.takeIf { saavnScore(title, artist, ytDuration, it) >= 40 }
                    ?: return@runCatching null
                Logger.d(TAG, "Saavn match: ${best.name} id=${best.id}")
                SaavnService.getBestStreamUrl(best.id, "320kbps")
            }.getOrElse {
                Logger.e(TAG, "Saavn failed: ${it.message}")
                null
            }
        }
    }

    private fun saavnScore(title: String, artist: String, ytDurationSec: Long, song: SaavnSong): Int {
        var score = wordOverlap(title, song.name, 50)
        val diff = kotlin.math.abs(ytDurationSec - (song.duration?.toLong() ?: 0L))
        score += when {
            ytDurationSec > 0 && diff <= 5  -> 30
            ytDurationSec > 0 && diff <= 15 -> 15
            else -> 0
        }
        score += wordOverlap(artist, song.artists.primary.joinToString(" ") { it.name }, 20)
        return score
    }

    private fun confidence(artist: String, title: String, durationMs: Long?, track: QobuzTrack): Float {
        if (!track.streamable) return 0f
        val titleSim = jaccard(normalize(title), normalize(track.title))
        val artistSim = jaccard(normalize(artist), normalize(track.performer?.name.orEmpty()))
        val durationFactor = durationMs?.let { qMs ->
            if (track.duration <= 0) return@let 1f
            val drift = kotlin.math.abs(qMs - track.duration * 1000L).toDouble() / qMs
            when {
                drift < 0.05 -> 1f
                drift < 0.10 -> 0.85f
                drift < 0.20 -> 0.6f
                else -> 0.3f
            }
        } ?: 1f
        return titleSim * artistSim * durationFactor
    }

    private fun normalize(s: String): String =
        s.lowercase()
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("(?i)\\b(feat\\.?|ft\\.?)\\b.*"), " ")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ").trim()

    private fun jaccard(a: String, b: String): Float {
        val setA = a.split(" ").filter { it.isNotEmpty() }.toSet()
        val setB = b.split(" ").filter { it.isNotEmpty() }.toSet()
        if (setA.isEmpty() || setB.isEmpty()) return 0f
        return setA.intersect(setB).size.toFloat() / setA.union(setB).size.toFloat()
    }

    private fun wordOverlap(a: String, b: String, maxPts: Int): Int {
        val setA = a.lowercase().split(Regex("\\s+")).filter { it.length > 1 }.toSet()
        val setB = b.lowercase().split(Regex("\\s+")).filter { it.length > 1 }.toSet()
        if (setA.isEmpty() || setB.isEmpty()) return 0
        return (setA.intersect(setB).size.toDouble() / maxOf(setA.size, setB.size) * maxPts).toInt()
    }
}
