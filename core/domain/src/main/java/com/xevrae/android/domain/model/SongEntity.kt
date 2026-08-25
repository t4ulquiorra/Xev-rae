package com.xevrae.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SongEntity(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long,
    val videoId: String
)
