package com.xevrae.domain.data.model.metadata

import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    val error: Boolean = false,
    val lines: List<Line>?,
    val syncType: String?,
    val xevraeLyrics: XevraeLyrics? = null,
)

@Serializable
data class XevraeLyrics(
    val id: String,
    val vote: Int,
)