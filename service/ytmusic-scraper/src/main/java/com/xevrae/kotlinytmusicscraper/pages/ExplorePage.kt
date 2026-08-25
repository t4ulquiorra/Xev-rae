package com.xevrae.kotlinytmusicscraper.pages

import com.xevrae.kotlinytmusicscraper.models.PlaylistItem
import com.xevrae.kotlinytmusicscraper.models.VideoItem

data class ExplorePage(
    val released: List<PlaylistItem>,
    val musicVideo: List<VideoItem>,
)