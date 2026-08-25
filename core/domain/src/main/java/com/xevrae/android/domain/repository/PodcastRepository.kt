package com.xevrae.android.domain.repository

interface PodcastRepository {
    suspend fun getPodcast(id: String) // TODO: Implement
}
