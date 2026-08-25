package com.xevrae.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import com.xevrae.expect.pressClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.xevrae.common.Config
import com.xevrae.domain.data.entities.AlbumEntity
import com.xevrae.domain.data.entities.ArtistEntity
import com.xevrae.domain.data.entities.LocalPlaylistEntity
import com.xevrae.domain.data.entities.PlaylistEntity
import com.xevrae.domain.data.entities.PodcastsEntity
import com.xevrae.domain.data.entities.SongEntity
import com.xevrae.domain.data.model.browse.album.Track
import com.xevrae.domain.data.model.searchResult.playlists.PlaylistsResult
import com.xevrae.domain.data.type.LibraryType
import com.xevrae.domain.data.type.PlaylistType
import com.xevrae.domain.data.type.RecentlyType
import com.xevrae.domain.mediaservice.handler.QueueData
import com.xevrae.domain.utils.connectArtists
import com.xevrae.domain.utils.toTrack
import com.xevrae.extension.getScreenSizeInfo
import com.xevrae.ui.navigation.destination.list.AlbumDestination
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.navigation.destination.list.LocalPlaylistDestination
import com.xevrae.ui.navigation.destination.list.PlaylistDestination
import com.xevrae.ui.navigation.destination.list.PodcastDestination
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.LibraryViewModel
import com.xevrae.viewModel.SharedViewModel
import kotlinx.coroutines.runBlocking
import xevrae.composeapp.generated.resources.getString
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import com.xevrae.ui.di.hiltInject
import androidx.hilt.navigation.compose.hiltViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.app_icon
import xevrae.composeapp.generated.resources.holder
import xevrae.composeapp.generated.resources.most_played
import xevrae.composeapp.generated.resources.no_favorite_playlists
import xevrae.composeapp.generated.resources.no_playlists_downloaded
import xevrae.composeapp.generated.resources.radio
import xevrae.composeapp.generated.resources.recently_added
import com.xevrae.domain.mediaservice.handler.PlaylistType as DomainPlaylistType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItem(
    state: LibraryItemState,
    viewModel: LibraryViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(),
    navController: NavController,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var songEntity by remember { mutableStateOf<SongEntity?>(null) }
    val density = LocalDensity.current
    val screenInfo = getScreenSizeInfo()

    val title =
        when (state.type) {
            is LibraryItemType.RecentlyAdded -> stringResource(Res.string.recently_added)
            is LibraryItemType.CanvasSong -> stringResource(Res.string.most_played)
            else -> return
        }
    val noPlaylistTitle =
        when (state.type) {
            LibraryItemType.DownloadedPlaylist -> stringResource(Res.string.no_playlists_downloaded)
            LibraryItemType.FavoritePlaylist -> stringResource(Res.string.no_favorite_playlists)
            is LibraryItemType.RecentlyAdded -> stringResource(Res.string.recently_added)
            is LibraryItemType.CanvasSong -> stringResource(Res.string.most_played)
            else -> return
        }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val scaleRatio =
            if (screenInfo.wDP > 0 && maxWidth > 0.dp) {
                (maxWidth.value / screenInfo.wDP).coerceIn(0.4f, 1.2f)
            } else {
                1f
            }
        val dynamicThumbSize = (125.dp * scaleRatio).coerceAtLeast(120.dp)
        val dynamicCanvasWidth = (170.dp * scaleRatio).coerceAtLeast(130.dp)

        if (showBottomSheet) {
            NowPlayingBottomSheet(
                onDismiss = {
                    showBottomSheet = false
                    songEntity = null
                },
                navController = navController,
                song = songEntity ?: return@BoxWithConstraints,
                onLibraryDelete = {
                    songEntity?.videoId?.let { viewModel.deleteSong(it) }
                },
            )
        }
        Column {
            Row(
                modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = typo().headlineMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .weight(1f)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable(),
                )
            }
            Crossfade(targetState = state.isLoading, label = "Loading") { isLoading ->
                if (!isLoading) {
                    if (state.type is LibraryItemType.RecentlyAdded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            state.data.filterIsInstance<RecentlyType>().forEach { item ->
                                when (item.objectType()) {
                                    RecentlyType.Type.SONG -> {
                                        SongFullWidthItems(
                                            songEntity = item as SongEntity,
                                            isPlaying = item.videoId == state.type.playingVideoId,
                                            modifier = Modifier,
                                            onMoreClickListener = {
                                                songEntity = item
                                                showBottomSheet = true
                                            },
                                            onClickListener = {
                                                viewModel.setQueueData(
                                                    QueueData.Data(
                                                        listTracks = arrayListOf(item.toTrack()),
                                                        firstPlayedTrack = item.toTrack(),
                                                        playlistId = "RDAMVM${item.videoId}",
                                                        playlistName = item.title,
                                                        playlistType = DomainPlaylistType.RADIO,
                                                        continuation = null,
                                                    ),
                                                )
                                                viewModel.loadMediaItem(
                                                    item,
                                                    type = Config.SONG_CLICK,
                                                    index = 0,
                                                )
                                            },
                                            onAddToQueue = { videoId ->
                                                sharedViewModel.addListToQueue(
                                                    arrayListOf(item.toTrack()),
                                                )
                                            },
                                        )
                                    }

                                    RecentlyType.Type.ARTIST -> {
                                        ArtistFullWidthItems(
                                            data = item as? ArtistEntity ?: return@forEach,
                                            onClickListener = {
                                                navController.navigate(
                                                    ArtistDestination(
                                                        channelId = item.channelId,
                                                    ),
                                                )
                                            },
                                        )
                                    }

                                    else -> {
                                        if (item is PlaylistType) {
                                            PlaylistFullWidthItems(
                                                data = item,
                                                onClickListener = {
                                                    when (item) {
                                                        is AlbumEntity -> {
                                                            navController.navigate(
                                                                AlbumDestination(
                                                                    item.browseId,
                                                                ),
                                                            )
                                                        }

                                                        is PlaylistEntity -> {
                                                            navController.navigate(
                                                                PlaylistDestination(
                                                                    item.id,
                                                                ),
                                                            )
                                                        }

                                                        is PodcastsEntity -> {
                                                            navController.navigate(
                                                                PodcastDestination(
                                                                    podcastId = item.podcastId,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.type is LibraryItemType.CanvasSong) {
                        LazyRow(
                            Modifier.padding(
                                top = 10.dp,
                            ),
                        ) {
                            items(state.data) { item ->
                                val song = item as? SongEntity ?: return@items
                                Box(
                                    Modifier
                                        .padding(horizontal = 10.dp)
                                        .height(300.dp)
                                        .width(dynamicCanvasWidth)
                                        .pressClickable {
                                            val firstQueue: Track = song.toTrack()
                                            viewModel.setQueueData(
                                                QueueData.Data(
                                                    listTracks = arrayListOf(firstQueue),
                                                    firstPlayedTrack = firstQueue,
                                                    playlistId = "RDAMVM${firstQueue.videoId}",
                                                    playlistName = "\"${song.title}\" ${runBlocking { getString(Res.string.radio) }}",
                                                    playlistType = DomainPlaylistType.RADIO,
                                                    continuation = null,
                                                ),
                                            )
                                            viewModel.loadMediaItem(
                                                firstQueue,
                                                type = Config.SONG_CLICK,
                                            )
                                        },
                                ) {
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(item.canvasThumbUrl)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(item.canvasThumbUrl)
                                                .crossfade(true)
                                                .build(),
                                        placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                        error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(
                                                    RoundedCornerShape(8.dp),
                                                ),
                                    )
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .align(Alignment.BottomStart),
                                    ) {
                                        Text(
                                            text = song.title,
                                            style = typo().labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(
                                                        align = Alignment.CenterVertically,
                                                    ).basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        animationMode = MarqueeAnimationMode.Immediately,
                                                    ).focusable(),
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.animation.AnimatedVisibility(visible = song.isExplicit) {
                                                ExplicitBadge(
                                                    modifier =
                                                        Modifier
                                                            .size(20.dp)
                                                            .padding(end = 4.dp)
                                                            .weight(1f),
                                                )
                                            }
                                            Text(
                                                text = (song.artistName?.connectArtists() ?: ""),
                                                style = typo().bodySmall,
                                                maxLines = 1,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(
                                                            align = Alignment.CenterVertically,
                                                        ).basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        if (state.data.isNotEmpty()) {
                            LazyRow {
                                items(items = state.data) { item ->
                                    Box(modifier = Modifier.animateItem()) {
                                        HomeItemContentPlaylist(
                                            onClick = {
                                                when (item) {
                                                    is LocalPlaylistEntity -> {
                                                        navController.navigate(
                                                            LocalPlaylistDestination(
                                                                item.id,
                                                            ),
                                                        )
                                                    }

                                                    is PlaylistsResult -> {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                item.browseId,
                                                                isYourYouTubePlaylist = true,
                                                            ),
                                                        )
                                                    }

                                                    is AlbumEntity -> {
                                                        navController.navigate(
                                                            AlbumDestination(
                                                                item.browseId,
                                                            ),
                                                        )
                                                    }

                                                    is PlaylistEntity -> {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                item.id,
                                                            ),
                                                        )
                                                    }

                                                    is PodcastsEntity -> {
                                                        navController.navigate(
                                                            PodcastDestination(
                                                                podcastId = item.podcastId,
                                                            ),
                                                        )
                                                    }
                                                }
                                            },
                                            data = item as? PlaylistType ?: return@items,
                                            thumbSize = dynamicThumbSize,
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(noPlaylistTitle, style = typo().bodyMedium)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CenterLoadingBox(Modifier.wrapContentSize())
                    }
                }
            }
        }
    }
}

sealed class LibraryItemType {
    data object CanvasSong : LibraryItemType()

    data class YouTubePlaylist(
        val isLoggedIn: Boolean,
        val onReload: () -> Unit = {},
    ) : LibraryItemType()

    data class LocalPlaylist(
        // Create new local playlist
        val onAddClick: (String) -> Unit,
    ) : LibraryItemType()

    data object FavoritePlaylist : LibraryItemType()

    data object DownloadedPlaylist : LibraryItemType()

    data object FavoritePodcasts : LibraryItemType()

    data class RecentlyAdded(
        val playingVideoId: String,
    ) : LibraryItemType()
}

data class LibraryItemState(
    val type: LibraryItemType,
    val data: List<LibraryType>,
    val isLoading: Boolean = true,
)