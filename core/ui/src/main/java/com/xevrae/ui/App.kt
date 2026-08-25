package com.xevrae.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import coil3.toUri
import com.xevrae.domain.data.player.GenericMediaItem
import com.xevrae.domain.manager.DataStoreManager
import com.xevrae.domain.manager.DataStoreManager.Values.TRUE
import com.xevrae.logger.Logger
import com.xevrae.expect.Orientation
import com.xevrae.expect.currentOrientation
import com.xevrae.expect.openUrl
import com.xevrae.expect.ui.layerBackdrop
import com.xevrae.expect.ui.rememberBackdrop
import com.xevrae.extension.copy
import com.xevrae.ui.component.AppBottomNavigationBar
import com.xevrae.ui.component.AppNavigationRail
import com.xevrae.ui.component.LiquidGlassAppBottomNavigationBar
import com.xevrae.ui.component.LiquidGlassLandscapeNavBar
import com.xevrae.ui.navigation.destination.home.NotificationDestination
import com.xevrae.ui.navigation.destination.list.AlbumDestination
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.navigation.destination.list.PlaylistDestination
import com.xevrae.ui.navigation.destination.player.FullscreenDestination
import com.xevrae.ui.navigation.graph.AppNavigationGraph
import com.xevrae.ui.screen.LeftPanelContent
import com.xevrae.ui.screen.MiniPlayer
import com.xevrae.ui.screen.player.NowPlayingScreen
import com.xevrae.ui.screen.player.NowPlayingScreenContent
import com.xevrae.ui.theme.AppTheme
import com.xevrae.ui.theme.fontFamily
import com.xevrae.ui.theme.typo
import com.xevrae.utils.VersionManager
import com.xevrae.viewModel.SharedViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import xevrae.composeapp.generated.resources.getString
import xevrae.composeapp.generated.resources.stringResource
import org.koin.compose.koinInject
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.cancel
import xevrae.composeapp.generated.resources.do_not_show_again
import xevrae.composeapp.generated.resources.download
import xevrae.composeapp.generated.resources.good_night
import xevrae.composeapp.generated.resources.notification
import xevrae.composeapp.generated.resources.sleep_timer_off
import xevrae.composeapp.generated.resources.this_app_needs_to_access_your_notification
import xevrae.composeapp.generated.resources.this_link_is_not_supported
import xevrae.composeapp.generated.resources.unknown
import xevrae.composeapp.generated.resources.update_available
import xevrae.composeapp.generated.resources.update_message
import xevrae.composeapp.generated.resources.version_format
import xevrae.composeapp.generated.resources.yes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun App(viewModel: SharedViewModel = hiltViewModel()) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val navController = rememberNavController()

    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val nowPlayingData by viewModel.nowPlayingState.collectAsStateWithLifecycle()
    val updateData by viewModel.updateResponse.collectAsStateWithLifecycle()
    val intent by viewModel.intent.collectAsStateWithLifecycle()
    val showNotificationPermissionDialog by viewModel.showNotificationPermissionDialog.collectAsStateWithLifecycle()

    val isTranslucentBottomBar by viewModel.getTranslucentBottomBar().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val isLiquidGlassEnabled by viewModel.getEnableLiquidGlass().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    var isShowMiniPlayer by rememberSaveable {
        mutableStateOf(true)
    }

    var isShowNowPlaylistScreen by rememberSaveable {
        mutableStateOf(false)
    }

    var isShowLeftPanel by rememberSaveable {
        mutableStateOf(false)
    }
    var leftPanelAccountUrl by remember {
        mutableStateOf("")
    }
    var leftPanelAccountName by remember {
        mutableStateOf("")
    }

    var isInFullscreen by rememberSaveable {
        mutableStateOf(false)
    }

    var isNavBarVisible by rememberSaveable {
        mutableStateOf(true)
    }

    var shouldShowUpdateDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val hazeState =
        rememberHazeState(
            blurEnabled = true,
        )

    LaunchedEffect(nowPlayingData) {
        isShowMiniPlayer = !(nowPlayingData?.mediaItem == null || nowPlayingData?.mediaItem == GenericMediaItem.EMPTY)
    }

    LaunchedEffect(intent) {
        val intent = intent ?: return@LaunchedEffect
        val data = intent.data
        Logger.d("MainActivity", "onCreate: $data")
        if (data != null) {
            if (data == "xevrae://notification".toUri()) {
                viewModel.setIntent(null)
                navController.navigate(
                    NotificationDestination,
                )
            } else if (data.host == "xevrae.org" || data.scheme == "xevrae") {
                val segments = data.pathSegments
                val appPath =
                    if (data.scheme == "xevrae") {
                        data.host
                    } else {
                        segments.getOrNull(1)
                    }
                Logger.d("MainActivity", "xevrae.org deep link, appPath: $appPath")
                viewModel.setIntent(null)
                when (appPath) {
                    "watch" -> {
                        data.getQueryParameter("v")?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }

                    "playlist" -> {
                        data.getQueryParameter("list")?.let { playlistId ->
                            if (playlistId.startsWith("OLAK5uy_")) {
                                navController.navigate(AlbumDestination(browseId = playlistId))
                            } else if (playlistId.startsWith("VL")) {
                                navController.navigate(PlaylistDestination(playlistId = playlistId))
                            } else {
                                navController.navigate(PlaylistDestination(playlistId = "VL$playlistId"))
                            }
                        }
                    }

                    "channel", "c" -> {
                        val artistId =
                            if (data.scheme == "xevrae") {
                                segments.firstOrNull()
                            } else {
                                segments.getOrNull(2)
                            }
                        artistId?.let {
                            if (it.startsWith("UC")) {
                                navController.navigate(ArtistDestination(channelId = it))
                            } else {
                                viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                            }
                        }
                    }

                    "album" -> {
                        data.getQueryParameter("id")?.let { albumId ->
                            navController.navigate(AlbumDestination(browseId = albumId))
                        }
                    }

                    else -> {
                        viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                    }
                }
            } else {
                Logger.d("MainActivity", "onCreate: $data")
                when (val path = data.pathSegments.firstOrNull()) {
                    "playlist" -> {
                        data
                            .getQueryParameter("list")
                            ?.let { playlistId ->
                                viewModel.setIntent(null)
                                if (playlistId.startsWith("OLAK5uy_")) {
                                    navController.navigate(
                                        AlbumDestination(
                                            browseId = playlistId,
                                        ),
                                    )
                                } else if (playlistId.startsWith("VL")) {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = playlistId,
                                        ),
                                    )
                                } else {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = "VL$playlistId",
                                        ),
                                    )
                                }
                            }
                    }

                    "channel", "c" -> {
                        data.lastPathSegment?.let { artistId ->
                            if (artistId.startsWith("UC")) {
                                viewModel.setIntent(null)
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = artistId,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(
                                    getString(
                                        Res.string.this_link_is_not_supported,
                                    ),
                                )
                            }
                        }
                    }

                    else -> {
                        when {
                            path == "watch" -> data.getQueryParameter("v")
                            data.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(updateData) {
        val response = updateData ?: return@LaunchedEffect
        if (viewModel.showedUpdateDialog &&
            response.tagName != getString(Res.string.version_format, VersionManager.getVersionName())
        ) {
            shouldShowUpdateDialog = true
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        Logger.d("MainActivity", "Current destination: ${navBackStackEntry?.destination?.route}")
        if (navBackStackEntry?.destination?.route?.contains("FullscreenDestination") == true) {
            isShowNowPlaylistScreen = false
        }
        isInFullscreen = navBackStackEntry?.destination?.hierarchy?.any {
            it.hasRoute(FullscreenDestination::class)
        } == true
    }
    var isScrolledToTop by rememberSaveable {
        mutableStateOf(false)
    }
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isTabletLandscape = isTablet && currentOrientation() == Orientation.LANDSCAPE && (androidx.compose.ui.platform.LocalConfiguration.current.let { it.screenWidthDp.toFloat() / it.screenHeightDp.toFloat() } >= 1.1f)
    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val leftPanelProgress = remember { androidx.compose.animation.core.Animatable(if (isShowLeftPanel) 1f else 0f) }
    LaunchedEffect(isShowLeftPanel) {
        leftPanelProgress.animateTo(
            targetValue = if (isShowLeftPanel) 1f else 0f,
            animationSpec = tween(300),
        )
    }
    val navBarStartPad = if (isTabletLandscape) screenWidthDp * 0.25f * leftPanelProgress.value else 0.dp
    val nowPlayingPanelWidth = screenWidthDp * 0.32f
    val miniPlayerPanelWidth = screenWidthDp * 0.32f

    val backdrop = rememberBackdrop()

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 8.dp, end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            ) {
        Scaffold(
            bottomBar = {
                if (!isTablet || (isTablet && !isTabletLandscape)) {
                    AnimatedVisibility(
                        isNavBarVisible,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut(),
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.width(navBarStartPad))
                        Column(Modifier.weight(1f)) {
                            AnimatedVisibility(
                                isShowMiniPlayer && isLiquidGlassEnabled == DataStoreManager.FALSE,
                                enter = fadeIn() + slideInHorizontally(),
                                exit = fadeOut(),
                            ) {
                                MiniPlayer(
                                    Modifier
                                        .height(60.dp)
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 12.dp,
                                        ),
                                    backdrop = backdrop,
                                    onClick = {
                                        isShowNowPlaylistScreen = true
                                    },
                                    onClose = {
                                        viewModel.stopPlayer()
                                        viewModel.isServiceRunning = false
                                    },
                                )
                            }
                            if (isLiquidGlassEnabled == TRUE) {
                                LiquidGlassAppBottomNavigationBar(
                                    navController = navController,
                                    backdrop = backdrop,
                                    viewModel = viewModel,
                                    onOpenNowPlaying = { isShowNowPlaylistScreen = true },
                                    isScrolledToTop = isScrolledToTop,
                                ) { klass ->
                                    viewModel.reloadDestination(klass)
                                }
                            } else {
                                AppBottomNavigationBar(
                                    navController = navController,
                                    isTranslucentBackground = isTranslucentBottomBar == TRUE,
                                ) { klass ->
                                    viewModel.reloadDestination(klass)
                                }
                            }
                        }
                        }
                    }
                }
            },
            content = { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (isLiquidGlassEnabled == TRUE && !isTablet) {
                                Modifier.layerBackdrop(backdrop)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        Row(Modifier.fillMaxSize().background(Color(0xFF121212))) {
                            if (isTabletLandscape && !isInFullscreen && (isShowLeftPanel || leftPanelProgress.value > 0f)) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .width(screenWidthDp * 0.25f * leftPanelProgress.value)
                                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                                        .pointerInput(screenWidthDp) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    coroutineScope.launch {
                                                        if (leftPanelProgress.value < 0.5f) {
                                                            leftPanelProgress.animateTo(0f, animationSpec = tween(200))
                                                            isShowLeftPanel = false
                                                        } else {
                                                            leftPanelProgress.animateTo(1f, animationSpec = tween(200))
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    coroutineScope.launch {
                                                        if (leftPanelProgress.value < 0.5f) {
                                                            leftPanelProgress.animateTo(0f, animationSpec = tween(200))
                                                            isShowLeftPanel = false
                                                        } else {
                                                            leftPanelProgress.animateTo(1f, animationSpec = tween(200))
                                                        }
                                                    }
                                                },
                                            ) { change, dragAmount ->
                                                change.consume()
                                                val panelWidthPx = with(density) { (screenWidthDp * 0.25f).toPx() }
                                                coroutineScope.launch {
                                                    val newProgress = (leftPanelProgress.value + dragAmount / panelWidthPx).coerceIn(0f, 1f)
                                                    leftPanelProgress.snapTo(newProgress)
                                                }
                                            }
                                        },
                                ) {
                                    Box(Modifier.fillMaxHeight().requiredWidth(screenWidthDp * 0.25f)) {
                                        LeftPanelContent(
                                            accountUrl = leftPanelAccountUrl,
                                            accountName = leftPanelAccountName,
                                            navController = navController,
                                            onDismiss = { isShowLeftPanel = false },
                                        )
                                    }
                                }
                            }
                            if (isTabletLandscape && !isInFullscreen && leftPanelProgress.value > 0f) {
                                Spacer(Modifier.width(8.dp * leftPanelProgress.value).background(Color.Black))
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .then(
                                        if (isTabletLandscape) {
                                            Modifier.clip(RoundedCornerShape(
                                                topStart = if (leftPanelProgress.value > 0f) 16.dp else 0.dp,
                                                bottomStart = if (leftPanelProgress.value > 0f) 16.dp else 0.dp,
                                                topEnd = if (isShowNowPlaylistScreen) 16.dp else 0.dp,
                                                bottomEnd = if (isShowNowPlaylistScreen) 16.dp else 0.dp,
                                            ))
                                        } else {
                                            Modifier
                                        }
                                    ),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (isLiquidGlassEnabled == TRUE && isTablet && !isInFullscreen) {
                                                Modifier.layerBackdrop(backdrop)
                                            } else {
                                                Modifier
                                            },
                                        ).hazeSource(hazeState),
                                ) {
                                    AppNavigationGraph(
                                        innerPadding = innerPadding,
                                        navController = navController,
                                        hideNavBar = {
                                            isNavBarVisible = false
                                        },
                                        showNavBar = {
                                            isNavBarVisible = true
                                        },
                                        showNowPlayingSheet = {
                                            isShowNowPlaylistScreen = true
                                        },
                                        showLeftPanel = { url, name ->
                                            leftPanelAccountUrl = url
                                            leftPanelAccountName = name
                                            isShowLeftPanel = true
                                        },
                                        onScrolling = {
                                            isScrolledToTop = it
                                        },
                                    )
                                }
                            }
                            if (isTablet && isTabletLandscape && !isInFullscreen) {
                                AnimatedVisibility(
                                    isShowNowPlaylistScreen,
                                    enter = expandHorizontally() + fadeIn(),
                                    exit = fadeOut() + shrinkHorizontally(),
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxHeight()
                                            .width(nowPlayingPanelWidth),
                                    ) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            Modifier
                                                .padding(
                                                    innerPadding.copy(
                                                        start = 0.dp,
                                                        top = 0.dp,
                                                        bottom = 0.dp,
                                                    ),
                                                ).clip(
                                                    RoundedCornerShape(16.dp),
                                                ),
                                        ) {
                                            NowPlayingScreenContent(
                                                navController = navController,
                                                sharedViewModel = viewModel,
                                                isExpanded = true,
                                                dismissIcon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                            ) {
                                                isShowNowPlaylistScreen = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Nav bar and mini player: overlaid on full screen, never affected by content layout
                        if (isTablet && isTabletLandscape && !isInFullscreen) {
                            // FIX: Animate both left and right padding smoothly when NowPlayingScreen opens
                            val navBarLeftPad by animateDpAsState(
                                targetValue = if (isShowNowPlaylistScreen) screenWidthDp * 0.075f else screenWidthDp * 0.0137f,
                                animationSpec = tween(300),
                                label = "navBarLeftPad"
                            )
                            val navBarRightReserve by animateDpAsState(
                                targetValue = when {
                                    isShowNowPlaylistScreen -> nowPlayingPanelWidth + (screenWidthDp * 0.075f)
                                    isShowMiniPlayer -> miniPlayerPanelWidth + (screenWidthDp * 0.0206f)
                                    else -> screenWidthDp * 0.0137f
                                },
                                animationSpec = tween(300),
                                label = "navBarRightReserve",
                            )
                            
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .padding(start = navBarStartPad + navBarLeftPad, end = navBarRightReserve, bottom = 8.dp)
                                    .height(67.dp)
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                            ) {
                                if (isLiquidGlassEnabled == TRUE) {
                                    LiquidGlassLandscapeNavBar(
                                        navController = navController,
                                        backdrop = backdrop,
                                        viewModel = viewModel,
                                        reloadDestinationIfNeeded = { klass ->
                                            viewModel.reloadDestination(klass)
                                        }
                                    )
                                } else {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
                                        AppBottomNavigationBar(
                                            navController = navController,
                                            isTranslucentBackground = false,
                                            containerColor = androidx.compose.ui.graphics.Color(0xFF1F1F1F),
                                        ) { klass ->
                                            viewModel.reloadDestination(klass)
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .padding(end = screenWidthDp * 0.0137f, bottom = 8.dp)
                                    .height(67.dp)
                                    .width(miniPlayerPanelWidth)
                                    .align(Alignment.BottomEnd),
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isShowMiniPlayer && !isShowNowPlaylistScreen,
                                    enter = fadeIn() + slideInHorizontally { it },
                                    exit = fadeOut() + slideOutHorizontally { it },
                                ) {
                                    MiniPlayer(
                                        Modifier.fillMaxSize(),
                                        backdrop = backdrop,
                                        cornerRadius = 16.dp,
                                        onClick = {
                                            isShowNowPlaylistScreen = true
                                        },
                                        onClose = {
                                            viewModel.stopPlayer()
                                            viewModel.isServiceRunning = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                if (isShowNowPlaylistScreen && !isTabletLandscape) {
                    NowPlayingScreen(
                        navController = navController,
                    ) {
                        isShowNowPlaylistScreen = false
                    }
                }

                if (!isTabletLandscape && (isShowLeftPanel || leftPanelProgress.value > 0f)) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeStyle(
                                        blurRadius = 20.dp * leftPanelProgress.value,
                                        tint = null,
                                    ),
                                )
                                .background(Color.Black.copy(alpha = 0.5f * leftPanelProgress.value))
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        coroutineScope.launch {
                                            leftPanelProgress.animateTo(0f, animationSpec = tween(300))
                                            isShowLeftPanel = false
                                        }
                                    }
                                },
                        )
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(screenWidthDp * 0.85f * leftPanelProgress.value)
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                                .pointerInput(screenWidthDp) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            coroutineScope.launch {
                                                if (leftPanelProgress.value < 0.5f) {
                                                    leftPanelProgress.animateTo(0f, animationSpec = tween(200))
                                                    isShowLeftPanel = false
                                                } else {
                                                    leftPanelProgress.animateTo(1f, animationSpec = tween(200))
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                if (leftPanelProgress.value < 0.5f) {
                                                    leftPanelProgress.animateTo(0f, animationSpec = tween(200))
                                                    isShowLeftPanel = false
                                                } else {
                                                    leftPanelProgress.animateTo(1f, animationSpec = tween(200))
                                                }
                                            }
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val panelWidthPx = with(density) { (screenWidthDp * 0.85f).toPx() }
                                        coroutineScope.launch {
                                            val newProgress = (leftPanelProgress.value + dragAmount / panelWidthPx).coerceIn(0f, 1f)
                                            leftPanelProgress.snapTo(newProgress)
                                        }
                                    }
                                },
                        ) {
                            Box(Modifier.fillMaxHeight().requiredWidth(screenWidthDp * 0.85f)) {
                                LeftPanelContent(
                                    accountUrl = leftPanelAccountUrl,
                                    accountName = leftPanelAccountName,
                                    navController = navController,
                                    onDismiss = { isShowLeftPanel = false },
                                )
                            }
                        }
                    }
                }

                if (sleepTimerState.isDone) {
                    Logger.w("MainActivity", "Sleep Timer Done: $sleepTimerState")
                    AlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            viewModel.stopSleepTimer()
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.stopSleepTimer()
                            }) {
                                Text(
                                    stringResource(Res.string.yes),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        text = {
                            Text(
                                stringResource(Res.string.sleep_timer_off),
                                style = typo().labelSmall,
                            )
                        },
                        title = {
                            Text(
                                stringResource(Res.string.good_night),
                                style = typo().bodySmall,
                            )
                        },
                    )
                }

                if (shouldShowUpdateDialog) {
                    val response = updateData ?: return@Scaffold
                    AlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            shouldShowUpdateDialog = false
                            viewModel.showedUpdateDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                    openUrl("https://xevrae.org/download")
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.download),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.cancel),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(Res.string.update_available),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            val formatted =
                                response.releaseTime?.let { input ->
                                    try {
                                        val instant = kotlin.time.Instant.parse(input)
                                        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                        dateTime.format(
                                            LocalDateTime.Format {
                                                day()
                                                char(' ')
                                                monthName(MonthNames.ENGLISH_ABBREVIATED)
                                                char(' ')
                                                year()
                                                char(' ')
                                                hour()
                                                char(':')
                                                minute()
                                                char(':')
                                                second()
                                            },
                                        )
                                    } catch (e: Exception) {
                                        stringResource(Res.string.unknown)
                                    }
                                } ?: stringResource(Res.string.unknown)

                            val updateMessage =
                                runBlocking {
                                    getString(
                                        Res.string.update_message,
                                        response.tagName,
                                        formatted,
                                    )
                                }
                            Column(
                                Modifier
                                    .heightIn(
                                        max = 400.dp,
                                    ).verticalScroll(
                                        rememberScrollState(),
                                    ),
                            ) {
                                Text(
                                    text = updateMessage,
                                    style = typo().labelMedium,
                                    modifier =
                                        Modifier.padding(
                                            vertical = 8.dp,
                                        ),
                                )
                                Markdown(
                                    response.body,
                                    typography =
                                        markdownTypography(
                                            h1 = typo().labelLarge,
                                            h2 = typo().labelMedium,
                                            h3 = typo().labelSmall,
                                            text = typo().bodySmall,
                                            bullet = typo().bodySmall,
                                            paragraph = typo().bodySmall,
                                            textLink =
                                                TextLinkStyles(
                                                    SpanStyle(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        fontFamily = fontFamily(),
                                                        textDecoration = TextDecoration.Underline,
                                                    ),
                                                ),
                                        ),
                                )
                            }
                        },
                    )
                }

                if (showNotificationPermissionDialog) {
                    var doNotShowAgain by remember { mutableStateOf(false) }
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.yes),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(Res.string.notification),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    stringResource(Res.string.this_app_needs_to_access_your_notification),
                                    style = typo().bodySmall,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .clickable { doNotShowAgain = !doNotShowAgain }
                                            .fillMaxWidth(),
                                ) {
                                    Checkbox(
                                        checked = doNotShowAgain,
                                        onCheckedChange = { doNotShowAgain = it },
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        stringResource(Res.string.do_not_show_again),
                                        style = typo().bodySmall,
                                    )
                                }
                            }
                        },
                    )
                }
            },
        )
        }
            }
    }
}
