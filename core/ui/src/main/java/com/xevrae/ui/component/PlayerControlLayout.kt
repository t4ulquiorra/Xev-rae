package com.xevrae.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.xevrae.domain.mediaservice.handler.ControlState
import com.xevrae.domain.mediaservice.handler.RepeatState
import com.xevrae.ui.theme.seed
import com.xevrae.ui.theme.transparent
import com.xevrae.viewModel.UIEvent
import xevrae.composeapp.generated.resources.painterResource
import xevrae.composeapp.generated.resources.Res
import xevrae.composeapp.generated.resources.repeat_all
import xevrae.composeapp.generated.resources.repeat_off
import xevrae.composeapp.generated.resources.repeat_one
import xevrae.composeapp.generated.resources.shuffle_off
import xevrae.composeapp.generated.resources.shuffle_on
import xevrae.composeapp.generated.resources.baseline_play_arrow_24
import xevrae.composeapp.generated.resources.baseline_pause_24

@Composable
fun PlayerControlLayout(
    controllerState: ControlState,
    isSmallSize: Boolean = false,
    onUIEvent: (UIEvent) -> Unit,
) {
    val height = if (isSmallSize) 48.dp else 96.dp
    val smallIcon = if (isSmallSize) 16.dp to 31.dp else 26.dp to 46.dp
    val mediumIcon = if (isSmallSize) 31.dp to 42.dp else 46.dp to 57.dp
    val bigIcon = if (isSmallSize) 42.dp to 53.dp else 79.dp to 106.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 6.dp),
    ) {
        // Shuffle
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "shuffle_alpha")
            Box(
                modifier =
                    Modifier
                        .background(transparent)
                        .size(smallIcon.second)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .dimClickable(interactionSource) { onUIEvent(UIEvent.Shuffle) }
                        .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.isShuffle, label = "Shuffle Button") { isShuffle ->
                    if (!isShuffle) {
                        Icon(
                            painter = painterResource(Res.drawable.shuffle_off),
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(smallIcon.first),
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.shuffle_on),
                            tint = seed,
                            contentDescription = "",
                            modifier = Modifier.size(smallIcon.first),
                        )
                    }
                }
            }
        }
        // Previous
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "prev_alpha")
            Box(
                modifier =
                    Modifier
                        .background(transparent)
                        .size(mediumIcon.second)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .dimClickable(interactionSource) {
                            if (controllerState.isPreviousAvailable) onUIEvent(UIEvent.Previous)
                        }
                        .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    tint = if (controllerState.isPreviousAvailable) Color.White else Color.Gray,
                    contentDescription = "",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        // Play/Pause
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "playpause_alpha")
            Box(
                modifier =
                    Modifier
                        .background(transparent)
                        .size(bigIcon.second)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .dimClickable(interactionSource) { onUIEvent(UIEvent.PlayPause) }
                        .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.isPlaying) { isPlaying ->
                    if (!isPlaying) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PauseCircle,
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    }
                }
            }
        }
        // Next
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "next_alpha")
            Box(
                modifier =
                    Modifier
                        .background(transparent)
                        .size(mediumIcon.second)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .dimClickable(interactionSource) {
                            if (controllerState.isNextAvailable) onUIEvent(UIEvent.Next)
                        }
                        .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    tint = if (controllerState.isNextAvailable) Color.White else Color.Gray,
                    contentDescription = "",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        // Repeat
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val alpha by animateFloatAsState(if (isPressed) 0.4f else 1f, label = "repeat_alpha")
            Box(
                modifier =
                    Modifier
                        .size(smallIcon.second)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .dimClickable(interactionSource) { onUIEvent(UIEvent.Repeat) }
                        .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = controllerState.repeatState) { rs ->
                    when (rs) {
                        is RepeatState.None -> {
                            Icon(
                                painter = painterResource(Res.drawable.repeat_off),
                                tint = Color.White,
                                contentDescription = "",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                        RepeatState.All -> {
                            Icon(
                                painter = painterResource(Res.drawable.repeat_all),
                                tint = seed,
                                contentDescription = "",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                        RepeatState.One -> {
                            Icon(
                                painter = painterResource(Res.drawable.repeat_one),
                                tint = seed,
                                contentDescription = "",
                                modifier = Modifier.size(smallIcon.first),
                            )
                        }
                    }
                }
            }
        }
    }
}
