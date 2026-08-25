package com.xevrae.data.parser

import com.xevrae.domain.data.model.mood.genre.GenreObject
import com.xevrae.domain.data.model.mood.genre.ItemsPlaylist
import com.xevrae.domain.data.model.mood.genre.ItemsSong
import com.xevrae.domain.data.model.mood.genre.Title
import com.xevrae.domain.data.model.mood.moodmoments.Content
import com.xevrae.domain.data.model.mood.moodmoments.Item
import com.xevrae.domain.data.model.mood.moodmoments.MoodsMomentObject
import com.xevrae.domain.data.model.searchResult.songs.Artist
import com.xevrae.domain.data.model.searchResult.songs.Thumbnail
import com.xevrae.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer
import com.xevrae.kotlinytmusicscraper.models.MusicShelfRenderer
import com.xevrae.kotlinytmusicscraper.models.MusicTwoRowItemRenderer
import com.xevrae.kotlinytmusicscraper.models.ThumbnailRenderer
import com.xevrae.kotlinytmusicscraper.models.response.BrowseResponse
import com.xevrae.domain.data.model.mood.genre.Content as GenreContent

private fun ThumbnailRenderer?.extractThumbnails(): List<Thumbnail> {
    if (this == null) return emptyList()
    val thumbs =
        this.musicThumbnailRenderer?.thumbnail?.thumbnails
            ?: this.croppedSquareThumbnailRenderer?.thumbnail?.thumbnails
            ?: this.musicAnimatedThumbnailRenderer?.backupRenderer?.thumbnail?.thumbnails
    return thumbs?.toListThumbnail() ?: emptyList()
}

private fun parseSongResponsiveItem(renderer: MusicResponsiveListItemRenderer): Content? {
    val flex0 = renderer.flexColumns.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer
    val songTitle = flex0?.text?.runs?.firstOrNull()?.text
    val videoId = flex0?.text?.runs?.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId
    val artistRuns = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
    val songSubtitle =
        artistRuns
            ?.filter { it.navigationEndpoint?.browseEndpoint != null }
            ?.joinToString(", ") { it.text }
            ?.takeIf { it.isNotBlank() }
            ?: artistRuns?.firstOrNull()?.text
    val songThumbnails =
        renderer.thumbnail
            .extractThumbnails()
            .map { thumb ->
                thumb.copy(url = Regex("([wh])120").replace(thumb.url, "$1544"))
            }
    if (videoId != null && songTitle != null) {
        return Content(
            playlistBrowseId = "",
            subtitle = songSubtitle ?: "",
            thumbnails = songThumbnails,
            title = songTitle,
            videoId = videoId,
        )
    }
    return null
}

private fun parseTwoRowItem(renderer: MusicTwoRowItemRenderer): Content {
    val thumbnails = renderer.thumbnailRenderer.extractThumbnails()
    val subtitle = renderer.subtitle?.runs?.joinToString("") { it.text } ?: ""
    val contentTitle = renderer.title.runs?.getOrNull(0)?.text ?: ""
    val playlistBrowseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: ""
    return Content(
        playlistBrowseId = playlistBrowseId,
        subtitle = subtitle,
        thumbnails = thumbnails,
        title = contentTitle,
    )
}

private fun parseMultiRowItem(multiRow: MusicShelfRenderer.Content.MusicMultiRowListItemRenderer): Content? {
    val contentTitle = multiRow.title?.runs?.getOrNull(0)?.text
    val subtitle = multiRow.subtitle?.runs?.joinToString("") { it.text } ?: ""
    val thumbnails = multiRow.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.toListThumbnail() ?: emptyList()
    if (contentTitle != null) {
        return Content(
            playlistBrowseId = "",
            subtitle = subtitle,
            thumbnails = thumbnails,
            title = contentTitle,
        )
    }
    return null
}

internal fun parseMoodsMomentObject(data: BrowseResponse?): MoodsMomentObject? {
    if (data == null) return null

    val header = data.header
    val visualHeader = header?.musicVisualHeaderRenderer
    val immersiveHeader = header?.musicImmersiveHeaderRenderer
    val detailHeader = header?.musicDetailHeaderRenderer
    val normalHeader = header?.musicHeaderRenderer

    val title =
        normalHeader?.title?.runs?.getOrNull(0)?.text
            ?: visualHeader?.title?.runs?.getOrNull(0)?.text
            ?: immersiveHeader?.title?.runs?.getOrNull(0)?.text
            ?: detailHeader?.title?.runs?.getOrNull(0)?.text
            ?: ""

    val singleTab = data.contents?.singleColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
    val tabSectionList = singleTab?.tabRenderer?.content?.sectionListRenderer
    val rootSectionList = data.contents?.sectionListRenderer
    val secondarySectionList = data.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
    val items = tabSectionList?.contents ?: rootSectionList?.contents ?: secondarySectionList?.contents

    val listItem: MutableList<Item> = mutableListOf()
    if (items != null) {
        for (item in items) {
            val carousel = item.musicCarouselShelfRenderer
            val grid = item.gridRenderer
            val shelf = item.musicShelfRenderer
            val card = item.musicCardShelfRenderer
            val respHeader = item.musicResponsiveHeaderRenderer
            val playlistShelf = item.musicPlaylistShelfRenderer

            if (carousel != null) {
                val headerTitle = carousel.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.getOrNull(0)?.text
                val listContent: MutableList<Content> = mutableListOf()
                for (content in carousel.contents) {
                    val responsive = content.musicResponsiveListItemRenderer
                    val twoRow = content.musicTwoRowItemRenderer
                    val multiRow = content.musicMultiRowListItemRenderer
                    if (responsive != null) {
                        parseSongResponsiveItem(responsive)?.let { listContent.add(it) }
                    } else if (twoRow != null) {
                        listContent.add(parseTwoRowItem(twoRow))
                    } else if (multiRow != null) {
                        parseMultiRowItem(multiRow)?.let { listContent.add(it) }
                    }
                }
                listItem.add(Item(contents = listContent, header = headerTitle ?: ""))
            } else if (grid != null) {
                val headerTitle = grid.header?.gridHeaderRenderer?.title?.runs?.getOrNull(0)?.text
                val listContent: MutableList<Content> = mutableListOf()
                for (content in grid.items) {
                    val twoRow = content.musicTwoRowItemRenderer
                    if (twoRow != null) {
                        listContent.add(parseTwoRowItem(twoRow))
                    }
                }
                listItem.add(Item(contents = listContent, header = headerTitle ?: ""))
            } else if (shelf != null) {
                val headerTitle = shelf.title?.runs?.getOrNull(0)?.text
                val listContent: MutableList<Content> = mutableListOf()
                val shelfContents = shelf.contents
                if (shelfContents != null) {
                    for (content in shelfContents) {
                        val responsive = content.musicResponsiveListItemRenderer
                        val multiRow = content.musicMultiRowListItemRenderer
                        if (responsive != null) {
                            parseSongResponsiveItem(responsive)?.let { listContent.add(it) }
                        } else if (multiRow != null) {
                            parseMultiRowItem(multiRow)?.let { listContent.add(it) }
                        }
                    }
                }
                listItem.add(Item(contents = listContent, header = headerTitle ?: ""))
            } else if (card != null) {
                val headerTitle = card.header.musicCardShelfHeaderBasicRenderer.title.runs?.getOrNull(0)?.text
                val cardTitle = card.title.runs?.getOrNull(0)?.text
                val cardSubtitle = card.subtitle.runs?.joinToString("") { it.text } ?: ""
                val cardThumbnails = card.thumbnail.extractThumbnails()
                val listContent: MutableList<Content> = mutableListOf()
                if (cardTitle != null || cardThumbnails.isNotEmpty()) {
                    listContent.add(
                        Content(
                            playlistBrowseId = card.onTap.browseEndpoint?.browseId ?: "",
                            subtitle = cardSubtitle,
                            thumbnails = cardThumbnails,
                            title = cardTitle ?: "",
                        ),
                    )
                }
                val cardContents = card.contents
                if (cardContents != null) {
                    for (c in cardContents) {
                        val responsive = c.musicResponsiveListItemRenderer
                        if (responsive != null) {
                            parseSongResponsiveItem(responsive)?.let { listContent.add(it) }
                        }
                    }
                }
                if (listContent.isNotEmpty()) {
                    listItem.add(Item(contents = listContent, header = headerTitle ?: ""))
                }
            } else if (respHeader != null) {
                val headerTitle = respHeader.title?.runs?.getOrNull(0)?.text
                val thumbnails = respHeader.thumbnail.extractThumbnails()
                if (thumbnails.isNotEmpty() || headerTitle != null) {
                    listItem.add(
                        Item(
                            contents =
                                listOf(
                                    Content(
                                        playlistBrowseId = "",
                                        subtitle = "",
                                        thumbnails = thumbnails,
                                        title = headerTitle ?: "",
                                    ),
                                ),
                            header = headerTitle ?: "",
                        ),
                    )
                }
            } else if (playlistShelf != null) {
                val listContent: MutableList<Content> = mutableListOf()
                val playlistContents = playlistShelf.contents
                if (playlistContents != null) {
                    for (content in playlistContents) {
                        val responsive = content.musicResponsiveListItemRenderer
                        val multiRow = content.musicMultiRowListItemRenderer
                        if (responsive != null) {
                            parseSongResponsiveItem(responsive)?.let { listContent.add(it) }
                        } else if (multiRow != null) {
                            parseMultiRowItem(multiRow)?.let { listContent.add(it) }
                        }
                    }
                }
                if (listContent.isNotEmpty()) {
                    listItem.add(Item(contents = listContent, header = ""))
                }
            }
        }
    }

    if (listItem.isEmpty() || listItem.all { it.contents.isEmpty() }) {
        val editableHeader = header?.musicEditablePlaylistDetailHeaderRenderer?.header
        val headerThumbs =
            visualHeader?.foregroundThumbnail.extractThumbnails().ifEmpty {
                visualHeader?.thumbnail.extractThumbnails()
            }.ifEmpty {
                immersiveHeader?.thumbnail.extractThumbnails()
            }.ifEmpty {
                detailHeader?.thumbnail.extractThumbnails()
            }.ifEmpty {
                editableHeader?.musicDetailHeaderRenderer?.thumbnail.extractThumbnails()
            }.ifEmpty {
                editableHeader?.musicResponsiveHeaderRenderer?.thumbnail.extractThumbnails()
            }.ifEmpty {
                data.background?.musicThumbnailRenderer?.thumbnail?.thumbnails?.toListThumbnail() ?: emptyList()
            }
        if (headerThumbs.isNotEmpty()) {
            listItem.add(
                Item(
                    contents =
                        listOf(
                            Content(
                                playlistBrowseId = "",
                                subtitle = "",
                                thumbnails = headerThumbs,
                                title = title,
                            ),
                        ),
                    header = title,
                ),
            )
        }
    }

    return MoodsMomentObject(
        endpoint = "FEmusic_moods_and_genres_category",
        header = title,
        items = listItem,
        params = "",
    )
}

internal fun parseGenreObject(data: BrowseResponse?): GenreObject? {
    if (data == null) return null

    val title =
        data.header
            ?.musicHeaderRenderer
            ?.title
            ?.runs
            ?.getOrNull(0)
            ?.text ?: ""

    val singleTab = data.contents?.singleColumnBrowseResultsRenderer?.tabs?.getOrNull(0)
    val tabSectionList = singleTab?.tabRenderer?.content?.sectionListRenderer
    val rootSectionList = data.contents?.sectionListRenderer
    val secondarySectionList = data.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
    val items = tabSectionList?.contents ?: rootSectionList?.contents ?: secondarySectionList?.contents

    val listItemsPlaylist: MutableList<ItemsPlaylist> = mutableListOf()
    val listItemsSong: MutableList<ItemsSong> = mutableListOf()
    if (items != null) {
        for (item in items) {
            val carousel = item.musicCarouselShelfRenderer
            val grid = item.gridRenderer

            if (carousel != null) {
                val header =
                    carousel
                        .header
                        ?.musicCarouselShelfBasicHeaderRenderer
                        ?.title
                        ?.runs
                        ?.getOrNull(0)
                        ?.text
                val listContent: MutableList<GenreContent> = mutableListOf()
                for (content in carousel.contents) {
                    val responsive = content.musicResponsiveListItemRenderer
                    val twoRow = content.musicTwoRowItemRenderer
                    if (responsive != null) {
                        val flex0 = responsive.flexColumns.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer
                        val songName = flex0?.text?.runs?.firstOrNull()?.text
                        val songArtists =
                            responsive
                                .flexColumns
                                .getOrNull(1)
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.filter { it.navigationEndpoint?.browseEndpoint != null }
                                ?.map { run ->
                                    Artist(
                                        id = run.navigationEndpoint?.browseEndpoint?.browseId,
                                        name = run.text,
                                    )
                                }?.takeIf { it.isNotEmpty() }
                        val videoId = flex0?.text?.runs?.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId
                        listItemsSong.add(
                            ItemsSong(
                                title = songName ?: "",
                                artist = songArtists ?: listOf(),
                                videoId = videoId ?: "",
                            ),
                        )
                    } else if (twoRow != null) {
                        val thumbnails = twoRow.thumbnailRenderer.extractThumbnails()
                        val subtitle = twoRow.subtitle?.runs?.joinToString("") { it.text } ?: ""
                        val contentTitle = twoRow.title.runs?.getOrNull(0)?.text ?: ""
                        val playlistBrowseId = twoRow.navigationEndpoint.browseEndpoint?.browseId ?: ""
                        listContent.add(
                            GenreContent(
                                playlistBrowseId = playlistBrowseId,
                                thumbnail = thumbnails,
                                title =
                                    Title(
                                        subtitle = subtitle,
                                        title = contentTitle,
                                    ),
                            ),
                        )
                    }
                }
                listItemsPlaylist.add(ItemsPlaylist(contents = listContent, header = header ?: "", type = "playlist"))
            } else if (grid != null) {
                val header =
                    grid
                        .header
                        ?.gridHeaderRenderer
                        ?.title
                        ?.runs
                        ?.getOrNull(0)
                        ?.text
                val listContent: MutableList<GenreContent> = mutableListOf()
                for (content in grid.items) {
                    val twoRow = content.musicTwoRowItemRenderer
                    if (twoRow != null) {
                        val thumbnails = twoRow.thumbnailRenderer.extractThumbnails()
                        val subtitle = twoRow.subtitle?.runs?.joinToString("") { it.text } ?: ""
                        val contentTitle = twoRow.title.runs?.getOrNull(0)?.text ?: ""
                        val playlistBrowseId = twoRow.navigationEndpoint.browseEndpoint?.browseId ?: ""
                        listContent.add(
                            GenreContent(
                                playlistBrowseId = playlistBrowseId,
                                thumbnail = thumbnails,
                                title =
                                    Title(
                                        subtitle = subtitle,
                                        title = contentTitle,
                                    ),
                            ),
                        )
                    }
                }
                listItemsPlaylist.add(ItemsPlaylist(contents = listContent, header = header ?: "", type = "playlist"))
            }
        }
    }
    return GenreObject(header = title, itemsPlaylist = listItemsPlaylist, itemsSong = listItemsSong)
}