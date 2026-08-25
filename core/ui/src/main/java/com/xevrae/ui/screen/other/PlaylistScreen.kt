package com.xevrae.ui.screen.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.Dp
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
import com.xevrae.domain.data.entities.DownloadState
import com.xevrae.domain.data.model.browse.album.Track
import com.xevrae.domain.utils.toSongEntity
import com.xevrae.expect.pressClickable
import com.xevrae.expect.ui.drawBackdropCustomShape
import com.xevrae.expect.ui.layerBackdrop
import com.xevrae.expect.ui.rememberBackdrop
import com.xevrae.expect.ui.toImageBitmap
import com.xevrae.extension.artworkScrimBrush
import com.xevrae.extension.getColorFromPalette
import com.xevrae.extension.getScreenSizeInfo
import com.xevrae.extension.getStringBlocking
import com.xevrae.extension.toImmersiveBackground
import com.xevrae.getPlatform
import com.xevrae.logger.Logger
import com.xevrae.ui.component.CenterLoadingBox
import com.xevrae.ui.component.DescriptionView
import com.xevrae.ui.component.EndOfPage
import com.xevrae.ui.component.HeartCheckBox
import com.xevrae.ui.component.LoadingDialog
import com.xevrae.ui.component.NowPlayingBottomSheet
import com.xevrae.ui.component.PlaylistBottomSheet
import com.xevrae.ui.component.RippleIconButton
import com.xevrae.ui.component.SongFullWidthItems
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.theme.seed
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.ListState
import com.xevrae.viewModel.PlaylistUIEvent
import com.xevrae.viewModel.PlaylistUIState
import com.xevrae.viewModel.PlaylistViewModel
import com.xevrae.viewModel.SharedViewModel
import com.xevrae.viewModel.UIEvent
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.album_length
import xevrae.composeapp.generated.resources.baseline_arrow_back_ios_new_24
import xevrae.composeapp.generated.resources.baseline_downloaded
import xevrae.composeapp.generated.resources.baseline_more_vert_24
import xevrae.composeapp.generated.resources.download_button
import xevrae.composeapp.generated.resources.downloaded
import xevrae.composeapp.generated.resources.downloading
import xevrae.composeapp.generated.resources.error
import xevrae.composeapp.generated.resources.holder
import xevrae.composeapp.generated.resources.no_description
import xevrae.composeapp.generated.resources.playlist
import xevrae.composeapp.generated.resources.radio
import xevrae.composeapp.generated.resources.search
import xevrae.composeapp.generated.resources.unlimited

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    playlistId: String,
    isYourYouTubePlaylist: Boolean,
    navController: NavController,
) {
    val id = playlistId.removePrefix("VL")
    val tag = "PlaylistScreen"

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/downloading_animation.json").decodeToString(),
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val liked by viewModel.liked.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val tracksListState by viewModel.tracksListState.collectAsStateWithLifecycle()

    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var searchBarHeightPx by remember { mutableStateOf(0) }

    val lazyState = rememberLazyListState()
    val firstItemVisible by remember {
        derivedStateOf {
            lazyState.firstVisibleItemIndex == 0
        }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredTrack by remember {
        derivedStateOf {
            if (query.isEmpty() || !showSearchBar) {
                tracks
            } else {
                tracks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.artists?.joinToString(", ")?.contains(query, ignoreCase = true) == true
                }
            }
        }
    }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            viewModel.getFullTracks {}
            lazyState.animateScrollToItem(0)
        }
    }

    val shouldStartPaginate =
        remember {
            derivedStateOf {
                tracksListState != ListState.PAGINATION_EXHAUST &&
                    (
                        lazyState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: -9
                    ) >= (lazyState.layoutInfo.totalItemsCount - 6)
            }
        }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value && tracksListState == ListState.IDLE) {
            viewModel.getContinuationTrack(
                id,
                continuation,
            )
        }
    }

    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val playingPlaylistId by remember {
        derivedStateOf {
            queueData?.data?.playlistId
        }
    }

    val playingTrack by sharedViewModel.nowPlayingState
        .mapLatest {
            it?.songEntity
        }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)

    var currentItem by remember {
        mutableStateOf<Track?>(null)
    }

    var itemBottomSheetShow by remember {
        mutableStateOf(false)
    }
    var playlistBottomSheetShow by remember {
        mutableStateOf(false)
    }

    val onPlaylistItemClick: (videoId: String) -> Unit = { videoId ->
        viewModel.onUIEvent(
            PlaylistUIEvent.ItemClick(
                videoId = videoId,
            ),
        )
    }
    val onItemMoreClick: (videoId: String) -> Unit = { videoId ->
        currentItem = tracks.firstOrNull { it.videoId == videoId }
        if (currentItem != null) {
            itemBottomSheetShow = true
        }
    }
    val onPlaylistMoreClick: () -> Unit = {
        playlistBottomSheetShow = true
    }

    LaunchedEffect(key1 = id) {
        if (id != uiState.data?.id) {
            viewModel.getData(id)
        }
    }
    LaunchedEffect(key1 = firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }
    val paletteState = rememberPaletteState()
    val hazeState =
        rememberHazeState(
            blurEnabled = true,
        )
    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    
    val currentThumbnail = (uiState as? PlaylistUIState.Success)?.data?.thumbnail
    var paletteGeneratedFor by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null && currentThumbnail != null && paletteGeneratedFor != currentThumbnail) {
            paletteState.generate(bm)
            paletteGeneratedFor = currentThumbnail
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            .distinctUntilChanged()
            .collectLatest {
                viewModel.setBrush(listOf(it.getColorFromPalette(), Color.Black))
            }
    }

    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP
    val mutedPaletteBg = paletteState.palette.toImmersiveBackground()

    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }

    Crossfade(
        targetState = uiState,
    ) { state ->
        when (state) {
            is PlaylistUIState.Success -> {
                val data = state.data
                if (data == null) return@Crossfade
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(mutedPaletteBg)
                            .hazeSource(hazeState),
                    state = lazyState,
                ) {
                    if (!showSearchBar) {
                        item(contentType = "header") {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(Color.Transparent)
                                        .animateItem(),
                            ) {
                                Column(
                                    Modifier
                                        .background(Color.Transparent),
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        if (isPortrait) {
                                            val artworkBackdrop = rememberBackdrop()
                                            val backBtnLayer = rememberGraphicsLayer()
                                            val rightGroupLayer = rememberGraphicsLayer()
                                            val headerHaze = rememberHazeState(blurEnabled = true)
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height((screenInfo.hDP / 2).dp),
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxSize()
                                                            .clipToBounds()
                                                            .layerBackdrop(artworkBackdrop),
                                                ) {
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
                                                                    .data(data.thumbnail)
                                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                                                    .diskCacheKey(data.thumbnail)
                                                                    .memoryCacheKey(data.thumbnail)
                                                                    .crossfade(false)
                                                                    .build(),
                                                            placeholder = painterResource(Res.drawable.holder),
                                                            error = painterResource(Res.drawable.holder),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            onSuccess = { res ->
                                                                bitmap = res.result.image.toImageBitmap()
                                                            },
                                                            modifier = Modifier.fillMaxSize(),
                                                        )
                                                    }
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
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .height((screenInfo.hDP * 0.35f).dp)
                                                                .align(Alignment.BottomCenter)
                                                                .background(artworkScrimBrush(mutedPaletteBg)),
                                                    )
                                                    Column(
                                                        modifier =
                                                            Modifier
                                                                .align(Alignment.BottomCenter)
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 20.dp)
                                                                .padding(bottom = 16.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                    ) {
                                                        Text(
                                                            text = data.title,
                                                            style = typo().titleLarge,
                                                            color = Color.White,
                                                            maxLines = 2,
                                                            textAlign = TextAlign.Center,
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        CompositionLocalProvider(
                                                            LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                                                        ) {
                                                            TextButton(
                                                                modifier =
                                                                    Modifier
                                                                        .wrapContentHeight()
                                                                        .defaultMinSize(minHeight = 1.dp, minWidth = 1.dp),
                                                                contentPadding = PaddingValues(vertical = 1.dp),
                                                                onClick = {
                                                                    if (data.author.id.isNotEmpty()) {
                                                                        navController.navigate(
                                                                            ArtistDestination(
                                                                                data.author.id,
                                                                            ),
                                                                        )
                                                                    }
                                                                },
                                                            ) {
                                                                Text(
                                                                    text = data.author.name,
                                                                    style = typo().titleSmall,
                                                                    color = Color.White,
                                                                    textAlign = TextAlign.Center,
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "${
                                                                if (data.isRadio) {
                                                                    stringResource(Res.string.radio)
                                                                } else {
                                                                    stringResource(Res.string.playlist)
                                                                }
                                                            } • ${data.year}",
                                                            style = typo().bodyMedium,
                                                            color = Color(0xC4FFFFFF),
                                                            textAlign = TextAlign.Center,
                                                        )
                                                    }
                                                }
                                                Row(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.TopCenter)
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                                            .windowInsetsPadding(WindowInsets.statusBars),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .size(48.dp)
                                                                .drawBackdropCustomShape(
                                                                    artworkBackdrop,
                                                                    backBtnLayer,
                                                                    0.5f,
                                                                    CircleShape,
                                                                ),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        RippleIconButton(
                                                            resId = Res.drawable.baseline_arrow_back_ios_new_24,
                                                        ) {
                                                            navController.navigateUp()
                                                        }
                                                    }
                                                    Spacer(Modifier.weight(1f))
                                                    Row(
                                                        modifier =
                                                            Modifier
                                                                .height(48.dp)
                                                                .drawBackdropCustomShape(
                                                                    artworkBackdrop,
                                                                    rightGroupLayer,
                                                                    0.5f,
                                                                    RoundedCornerShape(24.dp),
                                                                ),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier = Modifier.size(48.dp),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                HeartCheckBox(
                                                                    size = 28,
                                                                    checked = liked,
                                                                    onStateChange = {
                                                                        viewModel.onUIEvent(PlaylistUIEvent.Favorite)
                                                                    },
                                                                )
                                                            }
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                showSearchBar = !showSearchBar
                                                            },
                                                        ) {
                                                            Icon(Icons.Rounded.Search, null, tint = Color.White)
                                                        }
                                                        IconButton(
                                                            onClick = onPlaylistMoreClick,
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(Res.drawable.baseline_more_vert_24),
                                                                contentDescription = "More",
                                                                tint = Color.White,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            val headerBackdrop = rememberBackdrop()
                                            val backBtnLayer = rememberGraphicsLayer()
                                            val rightGroupLayer = rememberGraphicsLayer()
                                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                                val scaleRatio =
                                                    if (screenInfo.wDP > 0 && maxWidth > 0.dp) {
                                                        (maxWidth.value / screenInfo.wDP).coerceIn(0.4f, 1.2f)
                                                    } else {
                                                        1f
                                                    }
                                                val dynamicArtworkSize = (280.dp * scaleRatio).coerceAtLeast(120.dp)

                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    Column(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .layerBackdrop(headerBackdrop)
                                                                .windowInsetsPadding(WindowInsets.statusBars)
                                                                .padding(horizontal = 32.dp, vertical = 16.dp),
                                                    ) {
                                                        Spacer(modifier = Modifier.height(48.dp))
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                                                            verticalAlignment = Alignment.Top,
                                                        ) {
                                                            AsyncImage(
                                                                model =
                                                                    ImageRequest
                                                                        .Builder(LocalPlatformContext.current)
                                                                        .data(data.thumbnail)
                                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                                        .diskCacheKey(data.thumbnail)
                                                                        .memoryCacheKey(data.thumbnail)
                                                                        .crossfade(false)
                                                                        .build(),
                                                                placeholder = painterResource(Res.drawable.holder),
                                                                error = painterResource(Res.drawable.holder),
                                                                contentDescription = null,
                                                                contentScale = ContentScale.Crop,
                                                                onSuccess = { res ->
                                                                    bitmap = res.result.image.toImageBitmap()
                                                                },
                                                                modifier =
                                                                    Modifier
                                                                        .size(dynamicArtworkSize)
                                                                        .clip(RoundedCornerShape(8.dp)),
                                                            )
                                                        Column(
                                                            modifier = Modifier.weight(1f),
                                                        ) {
                                                            Text(
                                                                text = data.title,
                                                                style = typo().headlineSmall,
                                                                color = Color.White,
                                                                maxLines = 2,
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = data.author.name,
                                                                style = typo().titleMedium,
                                                                color = seed,
                                                                modifier =
                                                                    Modifier.pressClickable {
                                                                        if (data.author.id.isNotEmpty()) {
                                                                            navController.navigate(
                                                                                ArtistDestination(
                                                                                    data.author.id,
                                                                                ),
                                                                            )
                                                                        }
                                                                    },
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(
                                                                text = "${
                                                                    if (data.isRadio) {
                                                                        stringResource(Res.string.radio)
                                                                    } else {
                                                                        stringResource(Res.string.playlist)
                                                                    }
                                                                } • ${data.year}",
                                                                style = typo().labelMedium,
                                                                color = Color(0xC4FFFFFF),
                                                            )
                                                            Spacer(modifier = Modifier.height(20.dp))
                                                            val isThisPlaying = isPlaying && playingPlaylistId == data.id
                                                            Row(
                                                                modifier =
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(vertical = 8.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                if (!data.isRadio) {
                                                                    Box(
                                                                        modifier =
                                                                            Modifier
                                                                                .size(48.dp)
                                                                                .clip(CircleShape)
                                                                                .background(Color.White.copy(alpha = 0.12f))
                                                                                .pressClickable {
                                                                                    viewModel.onUIEvent(PlaylistUIEvent.Shuffle)
                                                                                },
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Rounded.Shuffle,
                                                                            contentDescription = "Shuffle",
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(22.dp),
                                                                        )
                                                                    }
                                                                }
                                                                Box(
                                                                    modifier =
                                                                        Modifier
                                                                            .height(48.dp)
                                                                            .widthIn(min = 110.dp)
                                                                            .clip(CircleShape)
                                                                            .background(Color.White)
                                                                            .pressClickable {
                                                                                if (isThisPlaying) {
                                                                                    sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                                                } else {
                                                                                    viewModel.onUIEvent(PlaylistUIEvent.PlayAll)
                                                                                }
                                                                            }.padding(horizontal = 20.dp),
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(
                                                                            imageVector =
                                                                                if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                                            contentDescription = null,
                                                                            tint = Color.Black,
                                                                            modifier = Modifier.size(22.dp),
                                                                        )
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(
                                                                            text = if (isThisPlaying) "Pause" else "Play",
                                                                            color = Color.Black,
                                                                            style = typo().labelLarge,
                                                                        )
                                                                    }
                                                                }
                                                                if (!data.isRadio) {
                                                                    Box(
                                                                        modifier =
                                                                            Modifier
                                                                                .size(48.dp)
                                                                                .clip(CircleShape)
                                                                                .background(Color.White.copy(alpha = 0.12f)),
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Crossfade(targetState = downloadState) { state ->
                                                                            when (state) {
                                                                                DownloadState.STATE_DOWNLOADED -> {
                                                                                    Box(
                                                                                        modifier =
                                                                                            Modifier
                                                                                                .fillMaxSize()
                                                                                                .pressClickable {
                                                                                                    viewModel.makeToast(
                                                                                                        getStringBlocking(Res.string.downloaded),
                                                                                                    )
                                                                                                },
                                                                                        contentAlignment = Alignment.Center,
                                                                                    ) {
                                                                                        Icon(
                                                                                            painter = painterResource(Res.drawable.baseline_downloaded),
                                                                                            tint = Color(0xFF00A0CB),
                                                                                            contentDescription = "",
                                                                                            modifier = Modifier.size(22.dp),
                                                                                        )
                                                                                    }
                                                                                }

                                                                                DownloadState.STATE_DOWNLOADING -> {
                                                                                    Box(
                                                                                        modifier =
                                                                                            Modifier
                                                                                                .fillMaxSize()
                                                                                                .pressClickable {
                                                                                                    viewModel.makeToast(
                                                                                                        getStringBlocking(Res.string.downloading),
                                                                                                    )
                                                                                                },
                                                                                        contentAlignment = Alignment.Center,
                                                                                    ) {
                                                                                        Image(
                                                                                            painter =
                                                                                                rememberLottiePainter(
                                                                                                    composition = composition,
                                                                                                    iterations = Compottie.IterateForever,
                                                                                                ),
                                                                                            contentDescription = "Lottie animation",
                                                                                            modifier = Modifier.size(28.dp),
                                                                                        )
                                                                                    }
                                                                                }

                                                                                else -> {
                                                                                    Box(
                                                                                        modifier =
                                                                                            Modifier
                                                                                                .fillMaxSize()
                                                                                                .pressClickable {
                                                                                                    Logger.w(
                                                                                                        "PlaylistScreen",
                                                                                                        "downloadState: $downloadState",
                                                                                                    )
                                                                                                    viewModel.onUIEvent(PlaylistUIEvent.Download)
                                                                                                },
                                                                                        contentAlignment = Alignment.Center,
                                                                                    ) {
                                                                                        Icon(
                                                                                            painter = painterResource(Res.drawable.download_button),
                                                                                            tint = Color.White,
                                                                                            contentDescription = "Download",
                                                                                            modifier = Modifier.size(22.dp),
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(12.dp)
                                                            .windowInsetsPadding(WindowInsets.statusBars)
                                                            .size(48.dp)
                                                            .drawBackdropCustomShape(
                                                                headerBackdrop,
                                                                backBtnLayer,
                                                                0.5f,
                                                                CircleShape,
                                                            ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    RippleIconButton(
                                                        resId = Res.drawable.baseline_arrow_back_ios_new_24,
                                                    ) {
                                                        navController.navigateUp()
                                                    }
                                                }
                                                Row(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.TopEnd)
                                                            .windowInsetsPadding(WindowInsets.statusBars)
                                                            .padding(end = 32.dp, top = 16.dp)
                                                            .height(48.dp)
                                                            .drawBackdropCustomShape(
                                                                headerBackdrop,
                                                                rightGroupLayer,
                                                                0.5f,
                                                                RoundedCornerShape(24.dp),
                                                            ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    if (!data.isRadio) {
                                                        Box(
                                                            modifier = Modifier.size(48.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            HeartCheckBox(
                                                                size = 28,
                                                                checked = liked,
                                                                onStateChange = {
                                                                    viewModel.onUIEvent(PlaylistUIEvent.Favorite)
                                                                },
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            showSearchBar = !showSearchBar
                                                        },
                                                    ) {
                                                        Icon(Icons.Rounded.Search, null, tint = Color.White)
                                                    }
                                                    IconButton(
                                                        onClick = onPlaylistMoreClick,
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(Res.drawable.baseline_more_vert_24),
                                                            contentDescription = "More",
                                                            tint = Color.White,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(),
                                        ) {
                                            Column(Modifier.padding(horizontal = 32.dp)) {
                                                if (isPortrait) {
                                                    val isThisPlaying = isPlaying && playingPlaylistId == data.id
                                                    Row(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.12f))
                                                                        .pressClickable {
                                                                            viewModel.onUIEvent(PlaylistUIEvent.Shuffle)
                                                                        },
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Shuffle,
                                                                    contentDescription = "Shuffle",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(22.dp),
                                                                )
                                                            }
                                                        }
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .height(48.dp)
                                                                    .widthIn(min = 110.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color.White)
                                                                    .pressClickable {
                                                                        if (isThisPlaying) {
                                                                            sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                                        } else {
                                                                            viewModel.onUIEvent(PlaylistUIEvent.PlayAll)
                                                                        }
                                                                    }.padding(horizontal = 20.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector =
                                                                        if (isThisPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                                    contentDescription = null,
                                                                    tint = Color.Black,
                                                                    modifier = Modifier.size(22.dp),
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = if (isThisPlaying) "Pause" else "Play",
                                                                    color = Color.Black,
                                                                    style = typo().labelLarge,
                                                                )
                                                            }
                                                        }
                                                        if (!data.isRadio) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.12f)),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Crossfade(targetState = downloadState) { state ->
                                                                    when (state) {
                                                                        DownloadState.STATE_DOWNLOADED -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .pressClickable {
                                                                                            viewModel.makeToast(
                                                                                                getStringBlocking(Res.string.downloaded),
                                                                                            )
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(Res.drawable.baseline_downloaded),
                                                                                    tint = Color(0xFF00A0CB),
                                                                                    contentDescription = "",
                                                                                    modifier = Modifier.size(22.dp),
                                                                                )
                                                                            }
                                                                        }

                                                                        DownloadState.STATE_DOWNLOADING -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .pressClickable {
                                                                                            viewModel.makeToast(
                                                                                                getStringBlocking(Res.string.downloading),
                                                                                            )
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Image(
                                                                                    painter =
                                                                                        rememberLottiePainter(
                                                                                            composition = composition,
                                                                                            iterations = Compottie.IterateForever,
                                                                                        ),
                                                                                    contentDescription = "Lottie animation",
                                                                                    modifier = Modifier.size(28.dp),
                                                                                )
                                                                            }
                                                                        }

                                                                        else -> {
                                                                            Box(
                                                                                modifier =
                                                                                    Modifier
                                                                                        .fillMaxSize()
                                                                                        .pressClickable {
                                                                                            Logger.w(
                                                                                                "PlaylistScreen",
                                                                                                "downloadState: $downloadState",
                                                                                            )
                                                                                            viewModel.onUIEvent(PlaylistUIEvent.Download)
                                                                                        },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Icon(
                                                                                    painter = painterResource(Res.drawable.download_button),
                                                                                    tint = Color.White,
                                                                                    contentDescription = "Download",
                                                                                    modifier = Modifier.size(22.dp),
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                val uriHandler = LocalUriHandler.current
                                                DescriptionView(
                                                    modifier =
                                                        Modifier
                                                            .padding(
                                                                top = 8.dp,
                                                            ),
                                                    text =
                                                        state.data.description.let {
                                                            if (!it.isNullOrEmpty()) {
                                                                it
                                                            } else {
                                                                stringResource(Res.string.no_description)
                                                            }
                                                        },
                                                    limitLine = 3,
                                                    onTimeClicked = {},
                                                    onURLClicked = { url ->
                                                        uriHandler.openUri(url)
                                                    },
                                                )
                                                Text(
                                                    text =
                                                        if (data.isRadio) {
                                                            stringResource(Res.string.unlimited)
                                                        } else {
                                                            stringResource(
                                                                Res.string.album_length,
                                                                (data.trackCount).toString(),
                                                                "",
                                                            )
                                                        },
                                                    color = Color.White,
                                                    style = typo().bodyMedium,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            val density = LocalDensity.current
                            Spacer(
                                Modifier.height(
                                    with(density) { searchBarHeightPx.toDp() },
                                ),
                            )
                        }
                    }
                    items(count = filteredTrack.size, key = { index ->
                        val item = filteredTrack.getOrNull(index)
                        (item?.videoId ?: "") + "item_$index"
                    }) { index ->
                        val item = filteredTrack.getOrNull(index)
                        if (item != null) {
                            Column(modifier = Modifier.animateItem()) {
                                SongFullWidthItems(
                                    isPlaying = playingTrack?.videoId == item.videoId && isPlaying,
                                    track = item,
                                    onMoreClickListener = { onItemMoreClick(it) },
                                    onClickListener = {
                                        Logger.w("PlaylistScreen", "index: $index")
                                        onPlaylistItemClick(it)
                                    },
                                    onAddToQueue = {
                                        sharedViewModel.addListToQueue(
                                            arrayListOf(item),
                                        )
                                    },
                                    modifier = Modifier,
                                )
                                if (index < filteredTrack.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.White.copy(alpha = 0.12f),
                                    )
                                }
                            }
                        }
                    }
                    when (tracksListState) {
                        ListState.IDLE -> {
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.LOADING, ListState.PAGINATING -> {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CenterLoadingBox(
                                        modifier = Modifier.size(80.dp),
                                    )
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.ERROR -> {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(64.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.error),
                                        style = typo().bodyMedium,
                                    )
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.PAGINATION_EXHAUST -> {
                            item {
                                EndOfPage()
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { searchBarHeightPx = it.size.height }
                            .hazeEffect(hazeState) {
                                blurEnabled = true
                                blurRadius = 24.dp
                                backgroundColor = mutedPaletteBg
                                tints = listOf(HazeTint(mutedPaletteBg.copy(alpha = 0.55f)))
                            },
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RippleIconButton(
                                resId = Res.drawable.baseline_arrow_back_ios_new_24,
                            ) {
                                navController.navigateUp()
                            }
                            SearchBar(
                                modifier =
                                    Modifier
                                        .height(50.dp)
                                        .padding(horizontal = 12.dp)
                                        .weight(1f),
                                colors =
                                    SearchBarDefaults.colors().copy(
                                        containerColor = Color.Transparent,
                                    ),
                                inputField = {
                                    CompositionLocalProvider(LocalTextStyle provides typo().bodySmall) {
                                        SearchBarDefaults.InputField(
                                            query = query,
                                            onQueryChange = { query = it },
                                            onSearch = { showSearchBar = false },
                                            expanded = showSearchBar,
                                            onExpandedChange = { showSearchBar = it },
                                            placeholder = {
                                                Text(
                                                    stringResource(Res.string.search),
                                                    style = typo().bodyMedium,
                                                )
                                            },
                                        )
                                    }
                                },
                                expanded = false,
                                onExpandedChange = {},
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            ) {
                            }
                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                },
                            ) {
                                Icon(Icons.Rounded.Close, null, tint = Color.White)
                            }
                        }
                    }
                }

                if (itemBottomSheetShow && currentItem != null) {
                    val track = currentItem?.toSongEntity() ?: return@Crossfade
                    NowPlayingBottomSheet(
                        onDismiss = {
                            itemBottomSheetShow = false
                            currentItem = null
                        },
                        navController = navController,
                        song = track,
                    )
                }
                if (playlistBottomSheetShow) {
                    val addToQueue = {
                        viewModel.getFullTracks { track ->
                            sharedViewModel.addListToQueue(
                                track.toCollection(arrayListOf()),
                            )
                        }
                    }
                    PlaylistBottomSheet(
                        onDismiss = { playlistBottomSheetShow = false },
                        playlistId = data.id,
                        playlistName = data.title,
                        isYourYouTubePlaylist = isYourYouTubePlaylist && !data.isRadio,
                        onSaveToLocal = {
                            viewModel.getFullTracks { track ->
                                viewModel.saveToLocal(track)
                            }
                        },
                        onEditTitle = { newTitle ->
                            viewModel.updatePlaylistTitle(newTitle, data.id)
                        },
                        onAddToQueue = if (data.isRadio) null else addToQueue,
                    )
                }
                AnimatedVisibility(
                    visible = shouldHideTopBar && !showSearchBar,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    TopAppBar(
                        windowInsets =
                            TopAppBarDefaults.windowInsets.exclude(
                                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start),
                            ),
                        title = {
                            Text(
                                text = data.title,
                                style = typo().titleMedium,
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
                        },
                        navigationIcon = {
                            Box(Modifier.padding(horizontal = 5.dp)) {
                                RippleIconButton(
                                    Res.drawable.baseline_arrow_back_ios_new_24,
                                    Modifier
                                        .size(32.dp),
                                    true,
                                ) {
                                    navController.navigateUp()
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    showSearchBar = !showSearchBar
                                },
                            ) {
                                Icon(Icons.Rounded.Search, null, tint = Color.White)
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

            is PlaylistUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CenterLoadingBox(
                        modifier = Modifier.size(80.dp),
                    )
                }
            }

            is PlaylistUIState.Error -> {
                viewModel.makeToast("Error: ${state.message}")
                navController.navigateUp()
            }
        }
    }
}