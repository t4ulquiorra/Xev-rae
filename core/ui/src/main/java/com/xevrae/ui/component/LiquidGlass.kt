package com.xevrae.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.xevrae.expect.ui.PlatformBackdrop

@Composable
fun Modifier.liquidGlass(
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape,
    interactive: Boolean,
): Modifier {
    val interaction = rememberGlassInteraction()
    return this.drawInteractiveGlass(
        backdrop = backdrop,
        layer = layer,
        luminanceAnimation = luminanceAnimation,
        shape = shape,
        interaction = if (interactive) interaction else null,
        pressedScale = 1.04f,
    )
}
