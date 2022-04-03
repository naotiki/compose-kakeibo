package xyz.naotiki_apps.compose_kakeibo

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
enum class Direction(val value: Int) {
    Previous(-1),
    Next(1)
}
/**
 * The class for to operation loopPager from program
 * @property hasOrder the boolean value is animating now or not
 * */
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

/*
 * RecyclerView使ったら簡単にできた説・・・
 * ただやりたいことができなかったのでこれを一から作った
 * */
//英語でコメント書くと英作文力つきそうだよね
/**
 * "LoopPager" will show the screen with horizontal loop.
 * Draw current,previous and next screen.
 * If you need to run the task consuming more resource in this pager,
 * you should run the task inside [current] composable.
 * @param
 * @param onChanged Be called before position reset
 * and take the arg what direction loopPager will switch.
 * @param previous Previous screen.
 * @param current Current screen.
 * @param next Next Screen.
 * @param threshold If drag amount is more than threshold, screen will switch.
 * If not, screen will back to original position with animations.
 * (Default value is 150F)
 * */
@Composable
fun LoopPager(
    state: LoopPagerState = rememberLoopPagerState(),
    onChanged: (Direction) -> Unit,
    previous: @Composable () -> Unit,
    current: @Composable () -> Unit,
    next: @Composable () -> Unit,
    threshold: Float = 150F,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sizePx = with(LocalDensity.current) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()
    val dragX = remember { mutableStateOf(0f) }
    var onDragging: Boolean by remember {
        mutableStateOf(false)
    }
    //For 指
    val animatable = remember(dragX.value) { Animatable(dragX.value) }
    //For コード操作
    val animatableForState = remember(state.hasOrder) { Animatable(0F) }
    //端に到達したら即画面更新
    if ((dragX.value >= sizePx) && onDragging && !animatable.isRunning) {
        onChanged(Direction.Previous)
        dragX.value = 0F
    } else if ((dragX.value <= -sizePx) && onDragging && !animatable.isRunning) {
        onChanged(Direction.Next)
        dragX.value = 0F
    }


                //よーするにドラッグ途中で離した時のやつー
    val float = if ((abs(dragX.value) > threshold) && (dragX.value != 0F) && (!onDragging || animatable.isRunning)) {
        if (!animatable.isRunning) {
            coroutineScope.launch {
                if (!animatable.isRunning && dragX.value != 0F) {
                    val direction =
                        if (dragX.value > 0) Direction.Previous else if (dragX.value < 0) Direction.Next else throw InternalError()
                    animatable.animateTo(
                        direction.value * sizePx * -1,
                        animationSpec = tween(150, easing = LinearEasing)
                    )
                    onChanged(direction)
                    dragX.value = 0F
                }
            }
        }
        remember { animatable.asState() }
        //コード操作
    } else if (state.hasOrder && (!onDragging || animatableForState.isRunning)) {
        if (!animatableForState.isRunning) {
            coroutineScope.launch {
                if (!animatableForState.isRunning) {
                    val direction = state.orderDirection!!
                    animatableForState.animateTo(
                        direction.value * sizePx * -1,
                        animationSpec = tween(100, easing = LinearEasing)
                    )
                    onChanged(direction)
                    dragX.value = 0F
                    state.hasOrder = false
                    state.orderDirection = null
                }
            }
        }
        remember { animatableForState.asState() }
        //閾値に満たない
    } else if ((abs(dragX.value) <= threshold) && (dragX.value != 0F) && (!onDragging || animatable.isRunning)) {
        if (!animatable.isRunning) {
            coroutineScope.launch {
                animatable.animateTo(0F, animationSpec = tween(250, easing = LinearEasing))
                dragX.value = 0F
            }
        }
        remember { animatable.asState() }
    } else {
        state.hasOrder = false
        state.orderDirection = null
        dragX
    }

    Row(Modifier.requiredWidth(screenWidth * 3).pointerInput(Unit) {
        detectHorizontalDragGestures(onDragStart = {
            onDragging = true
        }, onDragEnd = {
            onDragging = false
        }) { _, dragAmount ->
            val originalX = dragX.value
            val newValue = (originalX + dragAmount).coerceIn(-sizePx..sizePx)
            dragX.value = newValue
        }
    }.offset { IntOffset(float.value.roundToInt(), 0) }) {
        previous()
        current()
        next()
    }
}

