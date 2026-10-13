package com.leadrescue.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Fades the element in and slides it up slightly the first time it appears.
 * [index] staggers neighbouring items so sections reveal one after another.
 */
fun Modifier.reveal(index: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    val offsetPx = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(8) * 55L)
        progress.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}
