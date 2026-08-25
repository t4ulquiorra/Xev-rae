package com.xevrae.domain.data.model.browse.artist

import com.xevrae.domain.data.model.searchResult.songs.Thumbnail
import com.xevrae.domain.data.type.HomeContentType

data class ResultSingle(
    val browseId: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType