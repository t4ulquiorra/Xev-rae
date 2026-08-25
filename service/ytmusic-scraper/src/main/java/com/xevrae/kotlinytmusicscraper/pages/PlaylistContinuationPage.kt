package com.xevrae.kotlinytmusicscraper.pages

import com.xevrae.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)