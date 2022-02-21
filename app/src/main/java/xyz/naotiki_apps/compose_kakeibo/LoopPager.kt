package xyz.naotiki_apps.compose_kakeibo

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

//あああ
enum class Direction(val value: Int) {
    Previous(-1),
    Next(1)
}

class LoopPagerState {
    var hasOrder by mutableStateOf(false)
    var orderDirection: Direction? by mutableStateOf(null)
    fun animateTo(direction: Direction) {

        hasOrder = true
        orderDirection = direction
    }
}

@Composable
fun rememberLoopPagerState() = remember { LoopPagerState() }

@Composable
fun LoopPager(
    state: LoopPagerState = rememberLoopPagerState(),
    onChanged: (Direction) -> Unit,
    previous: @Composable () -> Unit,
    current: @Composable () -> Unit,
    next: @Composable () -> Unit,
    threshold: Float = 150F,
) {

    Log.i("TAG", "LoopPager:${state.hasOrder}")
    Log.i("TAG", "LoopPager:${state.orderDirection}")
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sizePx = with(LocalDensity.current) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { mutableStateOf(0f) }
    var onDragging: Boolean by remember {
        mutableStateOf(false)
    }
    val animatable = remember(offsetX.value) { Animatable(offsetX.value) }
    val animatableForState = remember(state.hasOrder) { Animatable(0F) }
    if ((offsetX.value >= sizePx) && onDragging && !animatable.isRunning) {
        onChanged(Direction.Previous)

        offsetX.value = 0F
    } else if ((offsetX.value <= -sizePx) && onDragging && !animatable.isRunning) {
        onChanged(Direction.Next)

        offsetX.value = 0F
    }

    val float = if (abs(offsetX.value) > threshold && offsetX.value != 0F && (!onDragging || animatable.isRunning)) {
        if (!animatable.isRunning) {
            coroutineScope.launch {
                if (!animatable.isRunning && offsetX.value != 0F) {
                    val direction =
                        if (offsetX.value > 0) Direction.Previous else if (offsetX.value < 0) Direction.Next else throw InternalError()
                    animatable.animateTo(
                        direction.value * sizePx * -1,
                        animationSpec = tween(150, easing = LinearEasing)
                    )
                    onChanged(direction)
                    offsetX.value = 0F

                }
            }
        }

        remember {
            animatable.asState()
        }
    } else if (state.hasOrder && (!onDragging || animatableForState.isRunning)) {
        Log.i("TAG", "LoopPager: PROGRAM OUTSIDE")
        if (!animatableForState.isRunning) {
            coroutineScope.launch {
                if (!animatableForState.isRunning) {
                    val direction = state.orderDirection!!
                    animatableForState.animateTo(
                        direction.value * sizePx * -1,
                        animationSpec = tween(100, easing = LinearEasing)
                    )
                    onChanged(direction)
                    offsetX.value = 0F
                    state.hasOrder = false
                    state.orderDirection = null
                }
            }
        }

        remember {
            animatableForState.asState()
        }
    } else if (abs(offsetX.value) <= threshold && offsetX.value != 0F && (!onDragging || animatable.isRunning)) {
        if (!animatable.isRunning) {

            coroutineScope.launch {

                animatable.animateTo(
                    0F,
                    animationSpec = tween(250, easing = LinearEasing)
                )
                offsetX.value = 0F
            }
        }
        remember {
            animatable.asState()
        }
    } else {
        state.hasOrder = false
        state.orderDirection = null

        offsetX
    }



    Log.i("floatAnim", "LoopPager: $float")


    Row(Modifier.requiredWidth(screenWidth * 3).pointerInput(Unit) {
        detectHorizontalDragGestures(onDragStart = {
            onDragging = true
        }, onDragEnd = {
            onDragging = false
        }) { _, dragAmount ->
            val originalX = offsetX.value
            val newValue = (originalX + dragAmount).coerceIn(-sizePx..sizePx)
            offsetX.value = newValue
        }
    }.offset { IntOffset(float.value.roundToInt(), 0) }) {
        previous()
        current()
        next()
    }


}

