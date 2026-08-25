package com.xevrae.android.service.kizzy

import io.ktor.client.HttpClient

class DiscordRpc(
    private val client: HttpClient? = null,
) {
    suspend fun setActivity(
        details: String,
        state: String,
        largeImageKey: String? = null,
        largeImageText: String? = null,
        smallImageKey: String? = null,
        smallImageText: String? = null,
        timestampsStart: Long? = null,
        timestampsEnd: Long? = null,
    ): Boolean {
        return true
    }

    suspend fun clearActivity(): Boolean {
        return true
    }
}
