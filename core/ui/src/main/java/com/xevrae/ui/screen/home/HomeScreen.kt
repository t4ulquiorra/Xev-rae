package com.xevrae.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.xevrae.expect.pressClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.xevrae.common.CHART_SUPPORTED_COUNTRY
import com.xevrae.common.Config
import com.xevrae.domain.data.model.browse.album.Track
import com.xevrae.domain.data.model.home.HomeItem
import com.xevrae.domain.data.model.home.chart.Chart
import com.xevrae.domain.extension.now
import com.xevrae.domain.mediaservice.handler.PlaylistType
import com.xevrae.domain.mediaservice.handler.QueueData
import com.xevrae.domain.utils.toTrack
import com.xevrae.logger.Logger
import com.xevrae.extension.angledGradientBackground
import com.xevrae.extension.getScreenSizeInfo
import com.xevrae.extension.isScrollingUp
import com.xevrae.extension.rgbFactor
import com.xevrae.ui.component.CenterLoadingBox
import com.xevrae.ui.component.Chip
import com.xevrae.ui.component.DropdownButton
import com.xevrae.ui.component.EndOfPage
import com.xevrae.ui.component.HomeItem
import com.xevrae.ui.component.HomeItemContentPlaylist
import com.xevrae.ui.component.HomeShimmer
import com.xevrae.ui.component.ItemArtistChart
import com.xevrae.ui.component.QuickPicksItem
import com.xevrae.ui.component.ReviewDialog
import com.xevrae.ui.component.RippleIconButton
import com.xevrae.ui.component.ShareSavedLyricsDialog
import com.xevrae.ui.navigation.destination.home.HomeDestination
import com.xevrae.ui.navigation.destination.home.MoodDestination
import com.xevrae.ui.navigation.destination.home.NotificationDestination
import com.xevrae.ui.navigation.destination.home.RecentlySongsDestination
import com.xevrae.ui.navigation.destination.home.SettingsDestination
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.navigation.destination.list.PlaylistDestination
import com.xevrae.ui.navigation.destination.login.LoginDestination
import com.xevrae.ui.theme.md_theme_dark_background
import com.xevrae.ui.theme.typo
import com.xevrae.ui.theme.white
import com.xevrae.viewModel.HomeViewModel
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_COMMUTE
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_ENERGIZE
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_FEEL_GOOD
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_FOCUS
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_PARTY
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_RELAX
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_ROMANCE
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_SAD
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_SLEEP
import com.xevrae.viewModel.HomeViewModel.Companion.HOME_PARAMS_WORKOUT
import com.xevrae.viewModel.ListState
import com.xevrae.viewModel.SharedViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.http.Url
import kotlinx.coroutines.launch
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import com.xevrae.ui.di.hiltInject
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.app_icon
import xevrae.composeapp.generated.resources.all
import xevrae.composeapp.generated.resources.app_name
import xevrae.composeapp.generated.resources.baseline_history_24
import xevrae.composeapp.generated.resources.baseline_settings_24
import xevrae.composeapp.generated.resources.cancel
import xevrae.composeapp.generated.resources.chart
import xevrae.composeapp.generated.resources.commute
import xevrae.composeapp.generated.resources.do_not_show_again
import xevrae.composeapp.generated.resources.energize
import xevrae.composeapp.generated.resources.feel_good
import xevrae.composeapp.generated.resources.focus
import xevrae.composeapp.generated.resources.go_to_log_in_page
import xevrae.composeapp.generated.resources.good_afternoon
import xevrae.composeapp.generated.resources.good_evening
import xevrae.composeapp.generated.resources.good_morning
import xevrae.composeapp.generated.resources.good_night
import xevrae.composeapp.generated.resources.holder
import xevrae.composeapp.generated.resources.let_s_start_with_a_radio
import xevrae.composeapp.generated.resources.log_in_warning
import xevrae.composeapp.generated.resources.outline_notifications_24
import xevrae.composeapp.generated.resources.party
import xevrae.composeapp.generated.resources.quick_picks
import xevrae.composeapp.generated.resources.relax
import xevrae.composeapp.generated.resources.romance
import xevrae.composeapp.generated.resources.sad
import xevrae.composeapp.generated.resources.sleep
import xevrae.composeapp.generated.resources.top_artists
import xevrae.composeapp.generated.resources.warning
import xevrae.composeapp.generated.resources.welcome_back
import xevrae.composeapp.generated.resources.what_is_best_choice_today
import xevrae.composeapp.generated.resources.workout

private val listOfHomeChip =
    listOf(
        Res.string.all,
        Res.string.relax,
        Res.string.sleep,
        Res.string.energize,
        Res.string.sad,
        Res.string.romance,
        Res.string.feel_good,
        Res.string.workout,
        Res.string.party,
        Res.string.commute,
        Res.string.focus,
    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onScrolling: (onTop: Boolean) -> Unit = {},
    onOpenLeftPanel: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel =
        hiltViewModel(),
    sharedViewModel: SharedViewModel =
        hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val isScrollingUp by scrollState.isScrollingUp()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val homeData by viewModel.homeItemList.collectAsStateWithLifecycle()
    val newRelease by viewModel.newRelease.collectAsStateWithLifecycle()
    val chart by viewModel.chart.collectAsStateWithLifecycle()
    val chartLoading by viewModel.loadingChart.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var accountShow by rememberSaveable {
        mutableStateOf(false)
    }
    val regionChart by viewModel.regionCodeChart.collectAsStateWithLifecycle()
    val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val chipRowState = rememberScrollState()
    val params by viewModel.params.collectAsStateWithLifecycle()
    val homeListState by viewModel.homeListState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()

    val shouldShowLogInAlert by viewModel.showLogInAlert.collectAsStateWithLifecycle()

    val openAppTime by sharedViewModel.openAppTime.collectAsStateWithLifecycle()
    val shareLyricsPermissions by sharedViewModel.shareSavedLyrics.collectAsStateWithLifecycle()

    var topHeaderColor by remember {
        mutableStateOf(md_theme_dark_background)
    }
    val animatedColor by animateColorAsState(topHeaderColor, tween(500))
    val mainHomeThumbnail by viewModel.mainHomeThumbnail.collectAsStateWithLifecycle()
    val networkLoader = rememberNetworkLoader(HttpClient(OkHttp))
    val dominantColorState =
        rememberDominantColorState(
            defaultColor = md_theme_dark_background,
            defaultOnColor = md_theme_dark_background,
            loader = networkLoader,
        )

    LaunchedEffect(mainHomeThumbnail) {
        mainHomeThumbnail?.let {
            dominantColorState.updateFrom(Url(it))
        }
    }

    LaunchedEffect(dominantColorState) {
        snapshotFlow { dominantColorState.color }.collect {
            topHeaderColor = it.rgbFactor(0.3f)
        }
    }

    var showReviewDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showRequestShareLyricsPermissions by rememberSaveable {
        mutableStateOf(false)
    }

    var maxTopAppBarHeightPx by rememberSaveable {
        mutableIntStateOf(0)
    }

    val hazeState =
        rememberHazeState(
            blurEnabled = true,
        )

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect {
                if (it <= 1) {
                    onScrolling.invoke(true)
                } else {
                    onScrolling.invoke(isScrollingUp)
                }
            }
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        viewModel.getHomeItemList(params, forceRefresh = true)
        Logger.w("HomeScreen", "onRefresh")
    }
    LaunchedEffect(key1 = reloadDestination) {
        if (reloadDestination == HomeDestination::class) {
            if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
                Logger.w("HomeScreen", "scrollState not at top (index=${scrollState.firstVisibleItemIndex}, offset=${scrollState.firstVisibleItemScrollOffset}), scrolling to top")
                scrollState.animateScrollToItem(0)
                sharedViewModel.reloadDestinationDone()
            } else {
                Logger.w("HomeScreen", "scrollState already at top, triggering onRefresh")
                onRefresh.invoke()
            }
        }
    }
    LaunchedEffect(key1 = loading) {
        if (!loading) {
            isRefreshing = false
            sharedViewModel.reloadDestinationDone()
            coroutineScope.launch {
                pullToRefreshState.animateToHidden()
            }
        }
    }
    LaunchedEffect(key1 = homeData) {
        accountShow = homeData.find { it.subtitle == accountInfo?.first } == null
    }
    LaunchedEffect(openAppTime, shareLyricsPermissions) {
        Logger.w("HomeScreen", "openAppTime: $openAppTime, shareLyricsPermissions: $shareLyricsPermissions")
        if (openAppTime >= 10 && openAppTime % 10 == 0 && openAppTime <= 50) {
            showReviewDialog = true
        } else if ((openAppTime == 1 || openAppTime % 15 == 0) && openAppTime <= 60 && !shareLyricsPermissions) {
            showRequestShareLyricsPermissions = true
        } else {
            showReviewDialog = false
            showRequestShareLyricsPermissions = false
        }
    }

    val shouldStartPaginate =
        remember {
            derivedStateOf {
                homeListState != ListState.PAGINATION_EXHAUST &&
                    (
                        scrollState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: -9
                    ) >= (scrollState.layoutInfo.totalItemsCount - 1)
            }
        }

    LaunchedEffect(key1 = shouldStartPaginate.value) {
        Logger.d("HomeScreen", "shouldStartPaginate: ${shouldStartPaginate.value}")
        Logger.d("HomeScreen", "homeListState: $homeListState")
        Logger.d("HomeScreen", "Continuation: $continuation")
        if (shouldStartPaginate.value && homeListState == ListState.IDLE) {
            viewModel.getContinueHomeItem(
                continuation,
            )
        }
    }

//    if (shouldShowGetDataSyncIdBottomSheet) {
//        GetDataSyncIdBottomSheet(
//            cookie = youTubeCookie,
//            onDismissRequest = {
//                shouldShowGetDataSyncIdBottomSheet = false
//            },
//        )
//    }

    if (showReviewDialog) {
        ReviewDialog(
            onDismissRequest = {
                sharedViewModel.onDoneReview(
                    isDismissOnly = true,
                )
                showReviewDialog = false
            },
            onDoneReview = {
                sharedViewModel.onDoneReview(
                    isDismissOnly = false,
                )
                showReviewDialog = false
            },
        )
    }

    if (showRequestShareLyricsPermissions) {
        ShareSavedLyricsDialog(
            onDismissRequest = {
                showRequestShareLyricsPermissions = false
                sharedViewModel.onDoneReview(
                    isDismissOnly = true,
                )
            },
            onConfirm = { contributor ->
                sharedViewModel.onDoneRequestingShareLyrics(
                    contributor,
                )
            },
        )
    }

    if (shouldShowLogInAlert) {
        var doNotShowAgain by rememberSaveable {
            mutableStateOf(false)
        }
        AlertDialog(
            title = {
                Text(stringResource(Res.string.warning))
            },
            text = {
                Column {
                    Text(text = stringResource(Res.string.log_in_warning))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .pressClickable {
                                    doNotShowAgain = !doNotShowAgain
                                }.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = doNotShowAgain,
                            onCheckedChange = {
                                doNotShowAgain = it
                            },
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(stringResource(Res.string.do_not_show_again))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.doneShowLogInAlert(doNotShowAgain)
                    navController.navigate(LoginDestination)
                }) {
                    Text(stringResource(Res.string.go_to_log_in_page))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.doneShowLogInAlert(doNotShowAgain)
                }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            onDismissRequest = {
                viewModel.doneShowLogInAlert()
            },
        )
    }

    Box {
        Box(
            modifier =
                Modifier
                    .hazeSource(hazeState),
        ) {
            Crossfade(targetState = loading, label = "Home Shimmer") { loading ->
                if (!loading) {
                    LazyColumn(
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        itemsIndexed(
                            homeData.filter { it.contents.isNotEmpty() && !it.title.equals("Shows for you", ignoreCase = true) },
                            key = { index, item ->
                                "${item.title}_${item.subtitle ?: ""}_$index"
                            },
                        ) { index, item ->
                            Box {
                                if (index == 0) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                                .angledGradientBackground(listOf(animatedColor, md_theme_dark_background), 25f),
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        brush =
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color.Transparent,
                                                                    Color(0x75000000),
                                                                    Color(0xFF121212),
                                                                ),
                                                            ),
                                                    ),
                                        )
                                    }
                                }
                                Column(
                                    modifier =
                                        Modifier
                                            .padding(start = 0.dp),
                                 ) {
                                    if (index == 0) {
                                        Spacer(
                                            Modifier.height(
                                                with(LocalDensity.current) { maxTopAppBarHeightPx.toDp() },
                                            ),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (index == 0 && accountInfo != null && accountShow) {
                                        AccountLayout(
                                            accountName = accountInfo?.first ?: "",
                                            url = accountInfo?.second ?: "",
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (item.title == stringResource(Res.string.quick_picks)) {
                                        AnimatedVisibility(
                                            visible =
                                                homeData.find {
                                                    it.title ==
                                                        stringResource(
                                                            Res.string.quick_picks,
                                                        )
                                                } != null,
                                        ) {
                                            QuickPicks(
                                                homeItem =
                                                    (
                                                        homeData.find {
                                                            it.title ==
                                                                stringResource(
                                                                    Res.string.quick_picks,
                                                                )
                                                        } ?: return@AnimatedVisibility
                                                    ).let { content ->
                                                        content.copy(
                                                            contents =
                                                                content.contents.mapNotNull { ct ->
                                                                    ct?.copy(
                                                                        artists =
                                                                            ct.artists?.let { art ->
                                                                                if (art.size > 1) {
                                                                                    art.dropLast(1)
                                                                                } else {
                                                                                    art
                                                                                }
                                                                            },
                                                                    )
                                                                },
                                                        )
                                                    },
                                                viewModel = viewModel,
                                            )
                                        }
                                    } else {
                                        HomeItem(
                                            navController = navController,
                                            data = item,
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(
                                homeListState == ListState.PAGINATING,
                                enter = expandVertically() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                CenterLoadingBox(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                )
                            }
                        }
                        if (homeListState == ListState.PAGINATION_EXHAUST) {
                            items(newRelease, key = { it.hashCode() }) {
                                AnimatedVisibility(
                                    visible = newRelease.isNotEmpty(),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(start = 0.dp),
                                    ) {
                                        HomeItem(
                                            navController = navController,
                                            data = it,
                                        )
                                    }
                                }
                            }
                            item {
                                Column(
                                    Modifier
                                        .padding(vertical = 10.dp)
                                        .padding(start = 0.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    ChartTitle()
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Crossfade(targetState = regionChart) {
                                        Logger.w("HomeScreen", "regionChart: $it")
                                        if (it != null) {
                                            Box(modifier = Modifier.padding(start = 16.dp)) {
                                                DropdownButton(
                                                    items = CHART_SUPPORTED_COUNTRY.itemsData.toList(),
                                                    defaultSelected =
                                                        CHART_SUPPORTED_COUNTRY.itemsData.getOrNull(
                                                            CHART_SUPPORTED_COUNTRY.items.indexOf(it),
                                                        )
                                                            ?: CHART_SUPPORTED_COUNTRY.itemsData[1],
                                                ) {
                                                    viewModel.exploreChart(
                                                        CHART_SUPPORTED_COUNTRY.items[
                                                            CHART_SUPPORTED_COUNTRY.itemsData.indexOf(
                                                                it,
                                                            ),
                                                        ],
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Crossfade(
                                        targetState = chartLoading,
                                        label = "Chart",
                                    ) { loading ->
                                        if (!loading) {
                                            chart?.let {
                                                ChartData(
                                                    chart = it,
                                                    navController = navController,
                                                )
                                            }
                                        } else {
                                            CenterLoadingBox(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(400.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            EndOfPage()
                        }
                    }
                } else {
                    Column {
                        Spacer(
                            Modifier.height(
                                with(LocalDensity.current) {
                                    maxTopAppBarHeightPx.toDp()
                                },
                            ),
                        )
                        HomeShimmer()
                    }
                }
            }
        }
        AnimatedContent(
            targetState = scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0,
            transitionSpec = {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            },
        ) { target ->
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .then(
                            if (target) {
                                Modifier.background(Color.Transparent)
                            } else {
                                Modifier
                                    .hazeEffect(hazeState, style = HazeMaterials.ultraThin()) {
                                        blurEnabled = true
                                    }
                            },
                        ).onGloballyPositioned { coordinates ->
                            if (coordinates.size.height > maxTopAppBarHeightPx) {
                                maxTopAppBarHeightPx = coordinates.size.height
                            }
                        },
            ) {
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    HomeTopAppBar(navController, accountInfo?.second ?: "", accountInfo?.first ?: "", onOpenLeftPanel)
                }
                AnimatedVisibility(
                    visible = !isScrollingUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.statusBars,
                                ),
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .horizontalScroll(chipRowState)
                            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                            .background(Color.Transparent),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOfHomeChip.forEach { id ->
                        val isSelected =
                            when (params) {
                                HOME_PARAMS_RELAX -> id == Res.string.relax
                                HOME_PARAMS_SLEEP -> id == Res.string.sleep
                                HOME_PARAMS_ENERGIZE -> id == Res.string.energize
                                HOME_PARAMS_SAD -> id == Res.string.sad
                                HOME_PARAMS_ROMANCE -> id == Res.string.romance
                                HOME_PARAMS_FEEL_GOOD -> id == Res.string.feel_good
                                HOME_PARAMS_WORKOUT -> id == Res.string.workout
                                HOME_PARAMS_PARTY -> id == Res.string.party
                                HOME_PARAMS_COMMUTE -> id == Res.string.commute
                                HOME_PARAMS_FOCUS -> id == Res.string.focus
                                else -> id == Res.string.all
                            }
                        Chip(
                            isAnimated = loading,
                            isSelected = isSelected,
                            text = stringResource(id),
                        ) {
                            when (id) {
                                Res.string.all -> { viewModel.setParams(null); viewModel.getHomeItemList(null, forceRefresh = true) }
                                Res.string.relax -> viewModel.setParams(HOME_PARAMS_RELAX)
                                Res.string.sleep -> viewModel.setParams(HOME_PARAMS_SLEEP)
                                Res.string.energize -> viewModel.setParams(HOME_PARAMS_ENERGIZE)
                                Res.string.sad -> viewModel.setParams(HOME_PARAMS_SAD)
                                Res.string.romance -> viewModel.setParams(HOME_PARAMS_ROMANCE)
                                Res.string.feel_good -> viewModel.setParams(HOME_PARAMS_FEEL_GOOD)
                                Res.string.workout -> viewModel.setParams(HOME_PARAMS_WORKOUT)
                                Res.string.party -> viewModel.setParams(HOME_PARAMS_PARTY)
                                Res.string.commute -> viewModel.setParams(HOME_PARAMS_COMMUTE)
                                Res.string.focus -> viewModel.setParams(HOME_PARAMS_FOCUS)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(navController: NavController, accountUrl: String = "", accountName: String = "", onOpenLeftPanel: (String, String) -> Unit = { _, _ -> }) {
    val hour =
        remember {
            val date = now().time
            date.hour
        }
    TopAppBar(
        windowInsets =
            TopAppBarDefaults.windowInsets.exclude(
                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start),
            ),
        title = {
            Column {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = typo().titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text =
                        when (hour) {
                            in 6..12 -> {
                                stringResource(Res.string.good_morning)
                            }

                            in 13..17 -> {
                                stringResource(Res.string.good_afternoon)
                            }

                            in 18..23 -> {
                                stringResource(Res.string.good_evening)
                            }

                            else -> {
                                stringResource(Res.string.good_night)
                            }
                        },
                    style = typo().bodySmall,
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
                    .clickable { onOpenLeftPanel(accountUrl, accountName) },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(accountUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
    )
}

@Composable
fun AccountLayout(
    accountName: String,
    url: String,
) {
    Column(
        modifier = Modifier.padding(start = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.welcome_back),
            style = typo().bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 3.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(
                            CircleShape,
                        ),
            )
            Text(
                text = accountName,
                style = typo().headlineMedium,
                color = Color.White,
                modifier =
                    Modifier
                        .padding(start = 8.dp),
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun QuickPicks(
    homeItem: HomeItem,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val lazyListState = rememberLazyGridState()
    val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState, snapPosition = SnapPosition.Start))
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val widthDp = maxWidth
        Column(
            Modifier.padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.let_s_start_with_a_radio),
                style = typo().bodySmall,
                modifier = Modifier.padding(start = 16.dp),
            )
            Text(
                text = stringResource(Res.string.quick_picks),
                style = typo().headlineMedium,
                color = Color.White,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 5.dp, bottom = 5.dp),
            )
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                modifier = Modifier.height(256.dp),
                state = lazyListState,
                flingBehavior = snapperFlingBehavior,
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp),
            ) {
                items(homeItem.contents, key = { it.hashCode() }) {
                    if (it != null) {
                        QuickPicksItem(
                            onClick = {
                                val firstQueue: Track = it.toTrack()
                                viewModel.setQueueData(
                                    QueueData.Data(
                                        listTracks = arrayListOf(firstQueue),
                                        firstPlayedTrack = firstQueue,
                                        playlistId = "RDAMVM${it.videoId}",
                                        playlistName = "\"${it.title}\" Radio",
                                        playlistType = PlaylistType.RADIO,
                                        continuation = null,
                                    ),
                                )
                                viewModel.loadMediaItem(
                                    firstQueue,
                                    type = Config.SONG_CLICK,
                                )
                            },
                            data = it,
                            widthDp = widthDp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartTitle() {
    Column(modifier = Modifier.padding(start = 16.dp)) {
        Text(
            text = stringResource(Res.string.what_is_best_choice_today),
            style = typo().bodyMedium,
        )
        Text(
            text = stringResource(Res.string.chart),
            style = typo().headlineMedium,
            color = white,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
    }
}

@Composable
fun ChartData(
    chart: Chart,
    navController: NavController,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val widthDp = maxWidth
        val screenInfo = getScreenSizeInfo()
        val scaleRatio =
            if (screenInfo.wDP > 0 && widthDp > 0.dp) {
                (widthDp.value / screenInfo.wDP).coerceIn(0.4f, 1.2f)
            } else {
                1f
            }
        val dynamicThumbSize = (160.dp * scaleRatio).coerceAtLeast(120.dp)

        val lazyListState2 = rememberLazyGridState()
        val snapperFlingBehavior2 = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState2))

        Column {
            chart.listChartItem.forEach { item ->
                Text(
                    text = item.title,
                    style = typo().headlineMedium,
                    color = white,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
                )
                val lazyListState = rememberLazyListState()
                val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState))
                LazyRow(
                    flingBehavior = snapperFlingBehavior,
                    contentPadding = PaddingValues(start = 6.dp, end = 6.dp),
                ) {
                    items(item.playlists.size, key = { index ->
                        val data = item.playlists[index]
                        data.id + data.title + index
                    }) {
                        HomeItemContentPlaylist(
                            onClick = {
                                navController.navigate(
                                    PlaylistDestination(
                                        playlistId = item.playlists[it].id,
                                        isYourYouTubePlaylist = false,
                                    ),
                                )
                            },
                            data = item.playlists[it],
                            thumbSize = dynamicThumbSize,
                        )
                    }
                }
            }
            Text(
                text = stringResource(Res.string.top_artists),
                style = typo().headlineMedium,
                color = white,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
            )
            LazyHorizontalGrid(
                rows = GridCells.Fixed(3),
                modifier = Modifier.height(240.dp),
                state = lazyListState2,
                flingBehavior = snapperFlingBehavior2,
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp),
            ) {
                items(chart.artists.itemArtists.size, key = { index ->
                    val item = chart.artists.itemArtists[index]
                    item.title + item.browseId + index
                }) {
                    val data = chart.artists.itemArtists[it]
                    ItemArtistChart(
                        onClick = {
                            navController.navigate(
                                ArtistDestination(
                                    channelId = data.browseId,
                                ),
                            )
                        },
                        data = data,
                        widthDp = widthDp,
                    )
                }
            }
        }
    }
}