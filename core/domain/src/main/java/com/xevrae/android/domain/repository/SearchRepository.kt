package com.xevrae.android.domain.repository

interface SearchRepository {
    suspend fun search(query: String) // TODO: Implement
}
