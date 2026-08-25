package com.xevrae.expect.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.xevrae.domain.data.model.metadata.Lyrics
import com.xevrae.domain.data.model.streams.TimeLine
import com.xevrae.media3.ui.MediaPlayerView
import com.xevrae.media3.ui.MediaPlayerViewWithSubtitle
import com.xevrae.extension.findActivity
import com.xevrae.extension.getScreenSizeInfo
import com.xevrae.ui.theme.typo

@Composable
fun MediaPlayerView(
    url: String,
    modifier: Modifier = Modifier,
) {
    MediaPlayerView(
        modifier = modifier,
        context = LocalContext.current,
        density = LocalDensity.current,
        url = url,
        screenSize = getScreenSizeInfo(),
    )
}

@Composable
fun MediaPlayerViewWithSubtitle(
    modifier: Modifier = Modifier,
    playerName: String,
    shouldPip: Boolean = false,
    shouldShowSubtitle: Boolean = true,
    shouldScaleDownSubtitle: Boolean = false,
    isInPipMode: Boolean = false,
    timelineState: TimeLine,
    lyricsData: Lyrics? = null,
    translatedLyricsData: Lyrics? = null,
    mainTextStyle: TextStyle = typo().bodyLarge,
    translatedTextStyle: TextStyle = typo().bodyMedium,
) {
    MediaPlayerViewWithSubtitle(
        playerName = playerName,
        modifier = modifier,
        shouldShowSubtitle = shouldShowSubtitle,
        shouldPip = shouldPip,
        shouldScaleDownSubtitle = shouldScaleDownSubtitle,
        timelineState = timelineState,
        lyricsData = lyricsData,
        translatedLyricsData = translatedLyricsData,
        context = LocalContext.current,
        activity = (LocalContext.current as? ComponentActivity) ?: LocalContext.current.findActivity(),
        isInPipMode = isInPipMode,
        mainTextStyle = mainTextStyle,
        translatedTextStyle = translatedTextStyle,
    )
}