package com.supershade.ui.shade

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supershade.haptics.LocalSuperHaptics
import com.supershade.haptics.SuperHaptics
import com.supershade.settings.ShadeTheme
import com.supershade.ui.theme.ChamferedCornerShape
import com.supershade.ui.theme.LocalShadeShapeScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Finger-tracked horizontal swipe with release + fling physics:
 *  - 1:1 tracking while dragging, subtle tilt, rubber-band when a direction is disabled
 *  - fling velocity or distance past the threshold commits the dismissal, carrying the
 *    release velocity into the exit spring
 *  - otherwise springs back with a controlled ease-out spring seeded with release velocity
 *  - haptic tick when crossing the commit threshold in either direction
 */
@Stable
class FluidSwipeState internal constructor(
    internal val scope: CoroutineScope,
) {
    internal val offset = Animatable(0f)
    internal var widthPx by mutableFloatStateOf(1f)

    /** Current horizontal translation in px (positive = to the right). */
    val offsetPx: Float get() = offset.value

    /** Distance after which a release commits the dismissal. */
    val thresholdPx: Float get() = widthPx * 0.28f

    /** 0..1 progress towards the commit threshold-ish range, used for background effects. */
    val progress: Float get() = (abs(offset.value) / (widthPx * 0.5f)).coerceIn(0f, 1f)

    val isAtRest: Boolean get() = abs(offset.value) < 4f
}

@Composable
fun rememberFluidSwipeState(key: Any?): FluidSwipeState {
    val scope = rememberCoroutineScope()
    return remember(key) { FluidSwipeState(scope) }
}

@Composable
fun FluidSwipeToDismiss(
    state: FluidSwipeState,
    onDismissed: (towardsEnd: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enableStartToEnd: Boolean = true,
    enableEndToStart: Boolean = true,
    backgroundContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val haptics: SuperHaptics = LocalSuperHaptics.current ?: remember(context) { SuperHaptics(context) }
    val density = LocalDensity.current
    val flingVelocityPx = with(density) { 900.dp.toPx() }
    val currentOnDismissed by rememberUpdatedState(onDismissed)
    val allowStart by rememberUpdatedState(enableStartToEnd)
    val allowEnd by rememberUpdatedState(enableEndToStart)

    LaunchedEffect(state) {
        snapshotFlow { abs(state.offset.value) > state.thresholdPx }
            .distinctUntilChanged()
            .collect { past -> if (past && !state.isAtRest) haptics.notificationDismissTick() }
    }

    fun allowed(dir: Float) = if (dir > 0f) allowStart else allowEnd

    val draggableState = rememberDraggableState { delta ->
        val cur = state.offset.value
        val dir = if (cur != 0f) sign(cur) else sign(delta)
        // Rubber-band when the direction is disabled.
        val effective = if (allowed(if (delta != 0f) sign(cur + delta) else dir)) delta else delta * 0.18f
        state.scope.launch { state.offset.snapTo(cur + effective) }
    }

    Box(modifier = modifier) {
        Box(modifier = Modifier.matchParentSize()) { backgroundContent() }
        Box(
            modifier = Modifier
                .onSizeChanged { state.widthPx = it.width.toFloat().coerceAtLeast(1f) }
                .graphicsLayer {
                    val p = state.progress
                    translationX = state.offset.value
                    rotationZ = (state.offset.value / state.widthPx) * 3.5f
                    val cardScale = (1.0f - p * 0.04f).coerceIn(0.95f, 1.0f)
                    scaleX = cardScale
                    scaleY = cardScale
                    alpha = if (p > 0.75f) (1f - (p - 0.75f) * 2.5f).coerceIn(0.4f, 1.0f) else 1.0f
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enableStartToEnd || enableEndToStart,
                    state = draggableState,
                    onDragStopped = { velocity ->
                        val o = state.offset.value
                        val dir = sign(o)
                        val flung = abs(velocity) > flingVelocityPx && sign(velocity) == dir && abs(o) > 8f
                        val commit = dir != 0f && allowed(dir) && (abs(o) > state.thresholdPx || flung)
                        if (commit) {
                            haptics.notificationDismissCommit()
                            state.offset.animateTo(
                                dir * state.widthPx * 1.2f,
                                spring(Spring.DampingRatioNoBouncy, 420f),
                                initialVelocity = velocity,
                            )
                            currentOnDismissed(dir > 0f)
                            // If the item survives (e.g. dismissal rejected), glide back.
                            state.offset.animateTo(0f, spring(0.85f, 360f))
                        } else {
                            state.offset.animateTo(
                                0f,
                                spring(0.78f, 460f),
                                initialVelocity = velocity,
                            )
                        }
                    },
                ),
        ) { content() }
    }
}

/**
 * Shared Draw/GraphicsLayer-deferred dismiss background for single and grouped notification cards.
 * Uses [derivedStateOf] for discrete threshold/direction states and defers continuous progress
 * reads to `drawBehind` and `graphicsLayer`, avoiding per-frame recomposition during swipes.
 */
@Composable
fun SwipeDismissBackground(
    swipeState: FluidSwipeState,
    shadeTheme: ShadeTheme,
    isGroup: Boolean = false,
    cardShape: Shape = LocalShadeShapeScheme.current.card,
) {
    val isSwiping by remember(swipeState) { derivedStateOf { !swipeState.isAtRest } }
    val isSwipingRight by remember(swipeState) { derivedStateOf { swipeState.offsetPx >= 0f } }
    val isPastDismissThreshold by remember(swipeState) {
        derivedStateOf { abs(swipeState.offsetPx) > swipeState.thresholdPx }
    }
    val showLabel by remember(swipeState) { derivedStateOf { swipeState.progress > 0.25f } }
    if (!isSwiping) return

    val baseTrackColor = when (shadeTheme) {
        is ShadeTheme.Cyberpunk -> Color(0xFFFF0055)
        is ShadeTheme.Nothing -> Color(0xFFD71920)
        else -> Color(0xFFE53935)
    }
    val maxTrackAlpha = if (shadeTheme is ShadeTheme.Cyberpunk) 0.35f else 0.40f
    val badgeColor = if (isPastDismissThreshold) baseTrackColor else baseTrackColor.copy(alpha = 0.85f)
    val badgeShape = when (shadeTheme) {
        is ShadeTheme.Cyberpunk -> ChamferedCornerShape(6.dp)
        is ShadeTheme.Nothing -> RoundedCornerShape(8.dp)
        is ShadeTheme.Pixel -> RoundedCornerShape(50)
        else -> RoundedCornerShape(20.dp)
    }
    val badgeBorder = when (shadeTheme) {
        is ShadeTheme.Cyberpunk -> BorderStroke(1.dp, Color(0xFFFF007F))
        is ShadeTheme.Nothing -> BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
        else -> null
    }
    val label = when {
        isGroup && shadeTheme is ShadeTheme.Cyberpunk -> "[PURGE_GROUP]"
        isGroup && shadeTheme is ShadeTheme.Nothing -> "CLEAR GROUP"
        isGroup -> "Clear group"
        shadeTheme is ShadeTheme.Cyberpunk -> "[PURGE]"
        shadeTheme is ShadeTheme.Nothing -> "DISMISS"
        else -> "Dismiss"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(cardShape)
            .drawBehind {
                val alpha = (swipeState.progress * 0.45f).coerceIn(0f, maxTrackAlpha)
                drawRect(baseTrackColor.copy(alpha = alpha))
            },
        contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Surface(
            shape = badgeShape,
            color = badgeColor,
            border = badgeBorder,
            shadowElevation = if (isPastDismissThreshold) 6.dp else 1.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    val p = swipeState.progress
                    val scale = if (abs(swipeState.offsetPx) > swipeState.thresholdPx) {
                        1.08f
                    } else {
                        (0.80f + p * 0.40f).coerceIn(0.80f, 1.0f)
                    }
                    scaleX = scale
                    scaleY = scale
                    val slideFraction = p.coerceIn(0f, 0.35f) / 0.35f
                    val slidePx = 16.dp.toPx()
                    translationX = if (swipeState.offsetPx >= 0f) {
                        (slideFraction - 1f) * slidePx
                    } else {
                        (1f - slideFraction) * slidePx
                    }
                },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val iconComposable: @Composable () -> Unit = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                rotationZ = if (abs(swipeState.offsetPx) > swipeState.thresholdPx) {
                                    0f
                                } else if (swipeState.offsetPx >= 0f) {
                                    -12f * (1f - swipeState.progress)
                                } else {
                                    12f * (1f - swipeState.progress)
                                }
                            },
                    )
                }
                val textComposable: @Composable () -> Unit = {
                    if (showLabel) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = if (shadeTheme is ShadeTheme.Cyberpunk) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 12.5.sp,
                            ),
                            color = Color.White,
                        )
                    }
                }
                if (isSwipingRight || isGroup) {
                    iconComposable()
                    textComposable()
                } else {
                    textComposable()
                    iconComposable()
                }
            }
        }
    }
}
