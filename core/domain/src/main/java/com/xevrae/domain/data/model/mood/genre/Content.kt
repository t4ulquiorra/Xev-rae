package com.xevrae.domain.data.model.mood.genre

import com.xevrae.domain.data.model.searchResult.songs.Thumbnail
import com.xevrae.domain.data.type.HomeContentType

data class Content(
    val playlistBrowseId: String,
    val thumbnail: List<Thumbnail>?,
    val title: Title,
) : HomeContentType