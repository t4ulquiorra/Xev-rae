package com.xevrae.data.repository

import com.music.innertube.YouTube as FastYouTube
import com.music.innertube.models.YouTubeClient as FastYouTubeClient
import com.music.innertube.utils.cipher.CipherDeobfuscator as FastCipherDeobfuscator
import com.music.innertube.utils.potoken.PoTokenGenerator as FastPoTokenGenerator
import com.xevrae.logger.Logger

internal suspend fun getFastStreamUrl(videoId: String, isVideo: Boolean): String? {
    return try {
        val sessionId = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
        val poTokenResult = FastPoTokenGenerator().getWebClientPoToken(videoId, sessionId)
        val poToken = poTokenResult?.playerRequestPoToken
        
        Logger.w("FastStream", "Requesting stream with PO Token: ${poToken != null}")
        
        val response = FastYouTube.player(
            videoId = videoId,
            client = FastYouTubeClient.WEB_REMIX,
            signatureTimestamp = null,
            poToken = poToken
        ).getOrThrow()
        
        val formatList = (response.streamingData?.formats ?: emptyList()) + (response.streamingData?.adaptiveFormats ?: emptyList())
        val format = if (isVideo) {
            formatList.filter { !it.isAudio }.maxByOrNull { it.bitrate ?: 0 }
        } else {
            formatList.filter { it.isAudio }.maxByOrNull { it.bitrate ?: 0 }
        } ?: return null
        
        var finalUrl: String? = null
        val sigCipher = format.signatureCipher
        if (!sigCipher.isNullOrEmpty()) {
            val deobfuscated = FastCipherDeobfuscator.deobfuscateStreamUrl(sigCipher, videoId)
            if (deobfuscated != null) {
                finalUrl = FastCipherDeobfuscator.transformNParamInUrl(deobfuscated)
            }
        } else {
            val url = format.url
            if (!url.isNullOrEmpty()) {
                finalUrl = FastCipherDeobfuscator.transformNParamInUrl(url)
            }
        }
        
        // Return the clean URL. ExoPlayer will handle range requests dynamically.
        finalUrl
    } catch (e: Exception) {
        Logger.e("FastStream", "Fast engine failed: ${e.message}")
        null
    }
}
