package com.xevrae.ui.screen.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.xevrae.Platform
import com.xevrae.common.Config
import com.xevrae.domain.data.model.browse.album.Track
import com.xevrae.domain.data.model.home.Content
import com.xevrae.domain.data.model.searchResult.songs.Artist
import com.xevrae.domain.mediaservice.handler.PlaylistType
import com.xevrae.domain.mediaservice.handler.QueueData
import com.xevrae.domain.utils.toSongEntity
import com.xevrae.domain.utils.toTrack
import com.xevrae.expect.pressClickable
import com.xevrae.expect.ui.MediaPlayerView
import com.xevrae.expect.ui.drawBackdropCustomShape
import com.xevrae.expect.ui.layerBackdrop
import com.xevrae.expect.ui.rememberBackdrop
import com.xevrae.expect.ui.toImageBitmap
import com.xevrae.extension.artworkScrimBrush
import com.xevrae.extension.getColorFromPalette
import com.xevrae.extension.getScreenSizeInfo
import com.xevrae.extension.getStringBlocking
import com.xevrae.extension.rgbFactor
import com.xevrae.extension.toImmersiveBackground
import com.xevrae.extension.toSquareThumbnailUrl
import com.xevrae.getPlatform
import com.xevrae.ui.component.CenterLoadingBox
import com.xevrae.ui.component.DescriptionView
import com.xevrae.ui.component.EndOfPage
import com.xevrae.ui.component.HomeItemArtist
import com.xevrae.ui.component.HomeItemContentPlaylist
import com.xevrae.ui.component.HomeItemVideo
import com.xevrae.ui.component.NowPlayingBottomSheet
import com.xevrae.ui.component.RippleIconButton
import com.xevrae.ui.component.SongFullWidthItems
import com.xevrae.ui.navigation.destination.list.AlbumDestination
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.navigation.destination.list.MoreAlbumsDestination
import com.xevrae.ui.navigation.destination.list.PlaylistDestination
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.ArtistScreenState
import com.xevrae.viewModel.ArtistViewModel
import com.xevrae.viewModel.SharedViewModel
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import xevrae.composeapp.generated.resources.getString
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.albums
import xevrae.composeapp.generated.resources.baseline_arrow_back_ios_new_24
import xevrae.composeapp.generated.resources.description
import xevrae.composeapp.generated.resources.error
import xevrae.composeapp.generated.resources.featured_inArtist
import xevrae.composeapp.generated.resources.holder
import xevrae.composeapp.generated.resources.more
import xevrae.composeapp.generated.resources.no_description
import xevrae.composeapp.generated.resources.popular
import xevrae.composeapp.generated.resources.related_artists
import xevrae.composeapp.generated.resources.singles
import xevrae.composeapp.generated.resources.unknown
import xevrae.composeapp.generated.resources.videos

@Composable
@ExperimentalMaterial3Api
fun ArtistScreen(
    channelId: String,
    viewModel: ArtistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
) {
    val artistScreenState by viewModel.artistScreenState.collectAsStateWithLifecycle()
    val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
    val canvasUrl by viewModel.canvasUrl.collectAsStateWithLifecycle()

    val playingTrack by sharedViewModel.nowPlayingState.map { it?.track?.videoId }.collectAsState(null)

    // Choosing song to show Bottom sheet
    var choosingTrack by remember {
        mutableStateOf<Track?>(null)
    }
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(channelId) {
        if (channelId != artistScreenState.data.channelId) {
            viewModel.browseArtist(channelId)
        }
    }

    val density = LocalDensity.current
    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP

    // Palette extraction from the artist artwork (Apple Music style)
    val paletteState = rememberPaletteState()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var paletteGeneratedFor by remember { mutableStateOf<String?>(null) }
    val currentImageUrl = (artistScreenState as? ArtistScreenState.Success)?.data?.imageUrl

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null && currentImageUrl != null && paletteGeneratedFor != currentImageUrl) {
            paletteState.generate(bm)
            paletteGeneratedFor = currentImageUrl
        }
    }

    // Page background derived from artwork
    val mutedPaletteBg = paletteState.palette.toImmersiveBackground()
    // Section tint for description card
    val sectionTint = paletteState.palette.getColorFromPalette()
    // Accent color for action buttons
    val artistAccent = Color.White

    val hazeState = rememberHazeState(blurEnabled = true)
    val lazyState = rememberLazyListState()
    val firstItemVisible by remember {
        derivedStateOf { lazyState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    Crossfade(artistScreenState) { state ->
        when (state) {
            is ArtistScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CenterLoadingBox(
                        Modifier
                            .align(Alignment.Center),
                    )
                }
            }

            is ArtistScreenState.Success -> {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(mutedPaletteBg)
                                .hazeSource(hazeState),
                        state = lazyState,
                    ) {
                        item(contentType = "header") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy((-36).dp),
                            ) {
                                val artworkBackdrop = rememberBackdrop()
                                val backBtnLayer = rememberGraphicsLayer()
                                val headerHaze = rememberHazeState(blurEnabled = true)
                                val headerImageUrl =
                                    if (isPortrait) {
                                        state.data.imageUrl?.toSquareThumbnailUrl()
                                    } else {
                                        state.data.imageUrl
                                    }

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isPortrait) {
                                                    Modifier.aspectRatio(1f)
                                                } else {
                                                    Modifier.height((screenInfo.hDP / 2).dp)
                                                },
                                            ),
                                ) {
                                    // Inner Box — backdrop SOURCE (artwork + canvas + overlays, NO glass)
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clipToBounds()
                                                .layerBackdrop(artworkBackdrop),
                                    ) {
                                        // Media layer (artwork + canvas) — Haze SOURCE for bottom blur
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .hazeSource(headerHaze),
                                        ) {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalPlatformContext.current)
                                                        .data(headerImageUrl)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(headerImageUrl)
                                                        .memoryCacheKey(headerImageUrl)
                                                        .crossfade(false)
                                                        .build(),
                                                placeholder = painterResource(Res.drawable.holder),
                                                error = painterResource(Res.drawable.holder),
                                                contentDescription = null,
                                                contentScale =
                                                    if (isPortrait) ContentScale.FillWidth else ContentScale.Crop,
                                                onSuccess = {
                                                    bitmap = it.result.image.toImageBitmap()
                                                },
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .alpha(if (canvasUrl != null) 0f else 1f),
                                            )
                                            // Canvas (Spotify) plays AS background when present
                                            canvasUrl?.let { canvas ->
                                                MediaPlayerView(
                                                    url = canvas.first,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        }

                                        // Progressive blur (Android)
                                        if (getPlatform() == Platform.Android) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .align(Alignment.BottomCenter)
                                                        .hazeEffect(headerHaze) {
                                                            blurRadius = 32.dp
                                                            progressive =
                                                                HazeProgressive.verticalGradient(
                                                                    startIntensity = 0f,
                                                                    endIntensity = 1f,
                                                                )
                                                        },
                                            )
                                        }

                                        // Smooth color scrim box
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(
                                                        if (isPortrait) {
                                                            (screenInfo.wDP * 0.7f).dp
                                                        } else {
                                                            (screenInfo.hDP * 0.35f).dp
                                                        },
                                                    )
                                                    .align(Alignment.BottomCenter)
                                                    .background(artworkScrimBrush(mutedPaletteBg)),
                                        )

                                        // Artist name + subtitle (subscribers · playCount)
                                        Column(
                                            modifier =
                                                Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .offset(y = (-36).dp)
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp)
                                                    .padding(bottom = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Text(
                                                text = state.data.title ?: stringResource(Res.string.unknown),
                                                style = typo().titleLarge,
                                                color = Color.White,
                                                maxLines = 2,
                                                textAlign = TextAlign.Center,
                                            )
                                            val meta =
                                                listOfNotNull(
                                                    state.data.subscribers?.takeIf { it.isNotBlank() },
                                                    state.data.playCount?.takeIf { it.isNotBlank() },
                                                ).joinToString(" · ")
                                            if (meta.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = meta,
                                                    style = typo().bodyMedium,
                                                    color = Color(0xC4FFFFFF),
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }

                                    // Back button — LiquidGlass back button (sibling of backdrop)
                                    Box(
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(12.dp)
                                                .windowInsetsPadding(WindowInsets.statusBars)
                                                .size(48.dp)
                                                .drawBackdropCustomShape(
                                                    backdrop = artworkBackdrop,
                                                    layer = backBtnLayer,
                                                    luminanceAnimation = 0.5f,
                                                    shape = CircleShape,
                                                ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RippleIconButton(
                                            resId = Res.drawable.baseline_arrow_back_ios_new_24,
                                        ) {
                                            navController.navigateUp()
                                        }
                                    }
                                }

                                // Centered Apple Music-style action row: [Radio][Shuffle Play][Follow]
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 32.dp)
                                            .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Radio button
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, artistAccent, CircleShape)
                                                .pressClickable {
                                                    val param = state.data.radioParam
                                                    if (param != null) {
                                                        viewModel.onRadioClick(param)
                                                    } else {
                                                        viewModel.makeToast(runBlocking { getString(Res.string.error) })
                                                    }
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Sensors,
                                            contentDescription = "Radio",
                                            tint = artistAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }

                                    // Shuffle button (Primary Play)
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(artistAccent)
                                                .pressClickable {
                                                    val param = state.data.shuffleParam
                                                    if (param != null) {
                                                        viewModel.onShuffleClick(param)
                                                    } else {
                                                        viewModel.makeToast(runBlocking { getString(Res.string.error) })
                                                    }
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = mutedPaletteBg,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }

                                    // Follow button
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(if (isFollowed == true) artistAccent else Color.Transparent)
                                                .border(1.5.dp, artistAccent, CircleShape)
                                                .pressClickable {
                                                    viewModel.updateFollowed(
                                                        if (isFollowed == true) 0 else 1,
                                                        state.data.channelId ?: return@pressClickable,
                                                    )
                                                },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowed == true) Icons.Default.Check else Icons.Outlined.PersonAdd,
                                            contentDescription = if (isFollowed == true) "Followed" else "Follow",
                                            tint = if (isFollowed == true) mutedPaletteBg else artistAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Content shelves
                        item(contentType = "sections") {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val scaleRatio =
                                    if (screenInfo.wDP > 0 && maxWidth > 0.dp) {
                                        (maxWidth.value / screenInfo.wDP).coerceIn(0.4f, 1.2f)
                                    } else {
                                        1f
                                    }
                                val dynamicThumbSize = (180.dp * scaleRatio).coerceAtLeast(120.dp)

                                Column {
                                    // Popular Songs
                                AnimatedVisibility(state.data.popularSongs.isNotEmpty()) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.popular),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f),
                                            )
                                            TextButton(
                                                onClick = {
                                                    val id = state.data.listSongParam
                                                    if (id != null) {
                                                        navController.navigate(PlaylistDestination(id))
                                                    } else {
                                                        viewModel.makeToast(runBlocking { getString(Res.string.error) })
                                                    }
                                                },
                                                colors =
                                                    ButtonDefaults
                                                        .textButtonColors()
                                                        .copy(
                                                            contentColor = Color.White,
                                                        ),
                                            ) {
                                                Text(stringResource(Res.string.more), style = typo().bodySmall)
                                            }
                                        }
                                        state.data.popularSongs.forEach { song ->
                                            SongFullWidthItems(
                                                track = song,
                                                isPlaying = song.videoId == playingTrack,
                                                modifier = Modifier.fillMaxWidth(),
                                                onMoreClickListener = {
                                                    choosingTrack = song
                                                    showBottomSheet = true
                                                },
                                                onClickListener = {
                                                    val firstQueue: Track = song
                                                    viewModel.setQueueData(
                                                        QueueData.Data(
                                                            listTracks = arrayListOf(firstQueue),
                                                            firstPlayedTrack = firstQueue,
                                                            playlistId = "RDAMVM${song.videoId}",
                                                            playlistName = "\"${state.data.title ?: ""}\" ${getStringBlocking(Res.string.popular)}",
                                                            playlistType = PlaylistType.RADIO,
                                                            continuation = null,
                                                        ),
                                                    )
                                                    viewModel.loadMediaItem(
                                                        firstQueue,
                                                        type = Config.SONG_CLICK,
                                                    )
                                                },
                                                onAddToQueue = {
                                                    sharedViewModel.addListToQueue(
                                                        arrayListOf(song),
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }

                                // Singles
                                AnimatedVisibility(
                                    state.data.singles != null &&
                                        state.data.singles.results.isNotEmpty(),
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.singles),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f),
                                            )
                                            TextButton(
                                                onClick = {
                                                    if (state.data.channelId != null) {
                                                        val id = "MPAD${state.data.channelId}"
                                                        navController.navigate(
                                                            MoreAlbumsDestination(
                                                                id = id,
                                                                type = MoreAlbumsDestination.SINGLE_TYPE,
                                                            ),
                                                        )
                                                    } else {
                                                        viewModel.makeToast(getStringBlocking(Res.string.error))
                                                    }
                                                },
                                                colors =
                                                    ButtonDefaults
                                                        .textButtonColors()
                                                        .copy(
                                                            contentColor = Color.White,
                                                        ),
                                            ) {
                                                Text(stringResource(Res.string.more), style = typo().bodySmall)
                                            }
                                        }
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                            items(state.data.singles?.results ?: emptyList()) { single ->
                                                HomeItemContentPlaylist(
                                                    onClick = {
                                                        navController.navigate(
                                                            AlbumDestination(
                                                                single.browseId,
                                                            ),
                                                        )
                                                    },
                                                    data = single,
                                                    thumbSize = dynamicThumbSize,
                                                )
                                            }
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }

                                // Albums
                                AnimatedVisibility(
                                    state.data.albums != null &&
                                        state.data.albums.results.isNotEmpty(),
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.albums),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f),
                                            )
                                            TextButton(
                                                onClick = {
                                                    if (state.data.channelId != null) {
                                                        val id = "MPAD${state.data.channelId}"
                                                        navController.navigate(
                                                            MoreAlbumsDestination(
                                                                id = id,
                                                                type = MoreAlbumsDestination.ALBUM_TYPE,
                                                            ),
                                                        )
                                                    } else {
                                                        viewModel.makeToast(getStringBlocking(Res.string.error))
                                                    }
                                                },
                                                colors =
                                                    ButtonDefaults
                                                        .textButtonColors()
                                                        .copy(
                                                            contentColor = Color.White,
                                                        ),
                                            ) {
                                                Text(stringResource(Res.string.more), style = typo().bodySmall)
                                            }
                                        }
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                            items(state.data.albums?.results ?: emptyList()) { album ->
                                                HomeItemContentPlaylist(
                                                    onClick = {
                                                        navController.navigate(
                                                            AlbumDestination(
                                                                browseId = album.browseId,
                                                            ),
                                                        )
                                                    },
                                                    data = album,
                                                    thumbSize = dynamicThumbSize,
                                                )
                                            }
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }

                                // Videos
                                AnimatedVisibility(
                                    state.data.video != null &&
                                        state.data.video.video.isNotEmpty(),
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.videos),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f),
                                            )
                                            TextButton(
                                                onClick = {
                                                    val videoListParam = state.data.video?.videoListParam
                                                    if (videoListParam != null) {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                videoListParam,
                                                            ),
                                                        )
                                                    } else {
                                                        viewModel.makeToast(getStringBlocking(Res.string.error))
                                                    }
                                                },
                                                colors =
                                                    ButtonDefaults
                                                        .textButtonColors()
                                                        .copy(
                                                            contentColor = Color.White,
                                                        ),
                                            ) {
                                                Text(stringResource(Res.string.more), style = typo().bodySmall)
                                            }
                                        }
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                            items(state.data.video?.video ?: emptyList()) { video ->
                                                HomeItemVideo(
                                                    onClick = {
                                                        val firstQueue: Track = video
                                                        viewModel.setQueueData(
                                                            QueueData.Data(
                                                                listTracks = arrayListOf(firstQueue),
                                                                firstPlayedTrack = firstQueue,
                                                                playlistId = "RDAMVM${video.videoId}",
                                                                playlistName = (state.data.title ?: "") + getStringBlocking(Res.string.videos),
                                                                playlistType = PlaylistType.RADIO,
                                                                continuation = null,
                                                            ),
                                                        )
                                                        viewModel.loadMediaItem(
                                                            firstQueue,
                                                            type = Config.VIDEO_CLICK,
                                                        )
                                                    },
                                                    onLongClick = {
                                                        choosingTrack = video
                                                        showBottomSheet = true
                                                    },
                                                    data =
                                                        Content(
                                                            album = null,
                                                            artists = video.artists,
                                                            description = null,
                                                            isExplicit = video.isExplicit,
                                                            playlistId = null,
                                                            browseId = null,
                                                            thumbnails = video.thumbnails ?: emptyList(),
                                                            title = video.title,
                                                            videoId = video.videoId,
                                                            views = video.videoType,
                                                        ),
                                                    thumbSize = dynamicThumbSize,
                                                )
                                            }
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }

                                // Featured on
                                AnimatedVisibility(state.data.featuredOn.isNotEmpty()) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.featured_inArtist),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .padding(vertical = 10.dp),
                                            )
                                        }
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                            items(state.data.featuredOn) { feature ->
                                                HomeItemContentPlaylist(
                                                    onClick = {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                feature.id,
                                                            ),
                                                        )
                                                    },
                                                    data = feature,
                                                    thumbSize = dynamicThumbSize,
                                                )
                                            }
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }

                                // Related Artists
                                AnimatedVisibility(
                                    state.data.related != null &&
                                        state.data.related.results.isNotEmpty(),
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.related_artists),
                                                style = typo().labelMedium,
                                                color = Color.White,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .padding(vertical = 10.dp),
                                            )
                                        }
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                            items(state.data.related?.results ?: emptyList()) { related ->
                                                HomeItemArtist(
                                                    onClick = {
                                                        navController.navigate(
                                                            ArtistDestination(
                                                                channelId = related.browseId,
                                                            ),
                                                        )
                                                    },
                                                    data =
                                                        Content(
                                                            album = null,
                                                            artists =
                                                                listOf(
                                                                    Artist(
                                                                        id = related.browseId,
                                                                        name = related.title,
                                                                    ),
                                                                ),
                                                            description = related.subscribers,
                                                            isExplicit = null,
                                                            playlistId = null,
                                                            browseId = related.browseId,
                                                            thumbnails = related.thumbnails,
                                                            title = related.title,
                                                            videoId = null,
                                                            views = null,
                                                            durationSeconds = null,
                                                            radio = null,
                                                        ),
                                                    thumbSize = dynamicThumbSize,
                                                )
                                            }
                                            item {
                                                Spacer(Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                ) {
                                    Text(
                                        text = stringResource(Res.string.description),
                                        style = typo().labelMedium,
                                        color = Color.White,
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .padding(vertical = 12.dp),
                                    )
                                }
                                val urlHandler = LocalUriHandler.current
                                ElevatedCard(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors =
                                        CardDefaults.elevatedCardColors().copy(
                                            containerColor = sectionTint.rgbFactor(0.5f),
                                        ),
                                ) {
                                    DescriptionView(
                                        modifier = Modifier.padding(16.dp),
                                        text = state.data.description ?: stringResource(Res.string.no_description),
                                        limitLine = 5,
                                        onTimeClicked = {},
                                        onURLClicked = { url ->
                                            urlHandler.openUri(url)
                                        },
                                    )
                                }
                                EndOfPage()
                            }
                        }
                    }
                    }

                    // Floating Haze top bar when scrolled away
                    AnimatedVisibility(
                        visible = shouldHideTopBar,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = state.data.title ?: "",
                                    style = typo().titleMedium,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            },
                            navigationIcon = {
                                Box(Modifier.padding(horizontal = 5.dp)) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            painter = painterResource(Res.drawable.baseline_arrow_back_ios_new_24),
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                            colors =
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                ),
                            modifier =
                                Modifier.hazeEffect(hazeState) {
                                    blurEnabled = true
                                    blurRadius = 24.dp
                                    backgroundColor = mutedPaletteBg
                                    tints = listOf(HazeTint(mutedPaletteBg.copy(alpha = 0.55f)))
                                },
                        )
                    }
                }

                if (showBottomSheet && choosingTrack != null) {
                    NowPlayingBottomSheet(
                        onDismiss = {
                            showBottomSheet = false
                            choosingTrack = null
                        },
                        navController = navController,
                        song = choosingTrack?.toSongEntity(),
                    )
                }
            }

            is ArtistScreenState.Error -> {
                viewModel.makeToast(state.message ?: stringResource(Res.string.error))
                navController.navigateUp()
            }
        }
    }
}
