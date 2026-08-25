package com.xevrae.android.service.ai

class AiClient(
    private val apiKey: String? = null,
) {
    suspend fun generateRecommendations(prompt: String): List<String> {
        return emptyList()
    }

    suspend fun analyzeMood(text: String): String {
        return "neutral"
    }
}
