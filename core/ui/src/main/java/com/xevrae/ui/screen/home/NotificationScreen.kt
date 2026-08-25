package com.xevrae.ui.screen.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.xevrae.extension.getScreenSizeInfo
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.xevrae.domain.data.entities.NotificationEntity
import com.xevrae.extension.formatTimeAgo
import com.xevrae.ui.component.CenterLoadingBox
import com.xevrae.ui.component.EndOfPage
import com.xevrae.ui.component.RippleIconButton
import com.xevrae.ui.navigation.destination.list.AlbumDestination
import com.xevrae.ui.navigation.destination.list.ArtistDestination
import com.xevrae.ui.theme.typo
import com.xevrae.viewModel.NotificationViewModel
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.app_icon
import xevrae.composeapp.generated.resources.album
import xevrae.composeapp.generated.resources.baseline_arrow_back_ios_new_24
import xevrae.composeapp.generated.resources.holder
import xevrae.composeapp.generated.resources.new_release
import xevrae.composeapp.generated.resources.no_notification
import xevrae.composeapp.generated.resources.notification
import xevrae.composeapp.generated.resources.singles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = koinViewModel(),
) {
    val listNotification by viewModel.listNotification.collectAsStateWithLifecycle()
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.notification),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                RippleIconButton(resId = Res.drawable.baseline_arrow_back_ios_new_24) {
                    navController.navigateUp()
                }
            },
        )
        Crossfade(targetState = listNotification) {
            if (it == null) {
                Box(
                    Modifier.fillMaxSize(),
                ) {
                    CenterLoadingBox(modifier = Modifier.align(Alignment.Center))
                }
            } else if (it.isNotEmpty()) {
                LazyColumn(modifier = Modifier.padding(15.dp)) {
                    items(it) { notification ->
                        NotificationItem(
                            notification = notification,
                            navController,
                        )
                    }
                    item {
                        EndOfPage()
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = stringResource(Res.string.no_notification),
                        style = typo().titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    navController: NavController,
) {
    val density = LocalDensity.current
    val screenInfo = getScreenSizeInfo()
    var containerWidthDp by remember { mutableStateOf(0.dp) }
    val scaleRatio =
        if (screenInfo.wDP > 0 && containerWidthDp > 0.dp) {
            (containerWidthDp.value / screenInfo.wDP).coerceIn(0.4f, 1.2f)
        } else {
            1f
        }
    val dynamicThumbSize = (150.dp * scaleRatio).coerceAtLeast(120.dp)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Column(
            modifier =
                Modifier.onGloballyPositioned { coordinates ->
                    with(density) {
                        containerWidthDp = coordinates.size.width.toDp()
                    }
                },
        ) {
            Row(
                Modifier.clickable {
                    navController.navigate(
                        ArtistDestination(
                            channelId = notification.channelId,
                        ),
                    )
                },
            ) {
                val thumb = notification.thumbnail
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(thumb)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(thumb)
                            .crossfade(true)
                            .build(),
                    placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                    error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically)
                            .size(50.dp)
                            .clip(
                                CircleShape,
                            ),
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Column {
                    Text(text = stringResource(Res.string.new_release), style = typo().titleSmall)
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text(text = notification.name, style = typo().headlineMedium)
                }
            }
            LazyRow(
                Modifier.padding(top = 15.dp),
            ) {
                items(notification.single) { single ->
                    ItemAlbumNotification(
                        isAlbum = false,
                        browseId = single["browseId"] ?: "",
                        title = single["title"] ?: "",
                        thumbnail = single["thumbnails"],
                        navController = navController,
                        thumbSize = dynamicThumbSize,
                    )
                }
                items(notification.album) { album ->
                    ItemAlbumNotification(
                        isAlbum = true,
                        browseId = album["browseId"] ?: "",
                        title = album["title"] ?: "",
                        thumbnail = album["thumbnails"],
                        navController = navController,
                        thumbSize = dynamicThumbSize,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = notification.time.formatTimeAgo(),
            style = typo().titleSmall,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 15.dp),
        )
    }
}

@Composable
fun ItemAlbumNotification(
    isAlbum: Boolean,
    browseId: String,
    title: String,
    thumbnail: String?,
    navController: NavController,
    thumbSize: Dp = 150.dp,
) {
    Box(
        modifier =
            Modifier
                .clickable {
                    navController.navigate(
                        AlbumDestination(
                            browseId = browseId,
                        ),
                    )
                },
    ) {
        Column(
            Modifier.padding(5.dp),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(thumbnail)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(thumbnail)
                        .crossfade(true)
                        .build(),
                placeholder = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                error = ColorPainter(androidx.compose.ui.graphics.Color(0xFF2A2A2A)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(thumbSize)
                        .clip(
                            RoundedCornerShape(10),
                        ),
            )
            Text(
                text = title,
                style = typo().titleSmall,
                color = Color.White,
                maxLines = 1,
                modifier =
                    Modifier
                        .width(thumbSize)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 10.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                        ).focusable(),
            )
            Text(
                text = if (isAlbum) stringResource(Res.string.album) else stringResource(Res.string.singles),
                style = typo().bodySmall,
                maxLines = 1,
                modifier =
                    Modifier
                        .width(thumbSize)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 10.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                        ).focusable(),
            )
        }
    }
}