package com.xevrae.domain.data.model.browse.artist

import com.xevrae.domain.data.model.searchResult.songs.Thumbnail
import com.xevrae.domain.data.type.HomeContentType

data class ResultAlbum(
    val browseId: String,
    val isExplicit: Boolean,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType