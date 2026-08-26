package com.xevrae.kotlinytmusicscraper.pages

import com.xevrae.kotlinytmusicscraper.models.AlbumItem
import com.xevrae.kotlinytmusicscraper.models.VideoItem

data class ExplorePage(
    val released: List<AlbumItem>,
    val musicVideo: List<VideoItem>,
)