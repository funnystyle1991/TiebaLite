package com.huanchengfly.tieba.post.ui.page.main

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hihonor.smartgripkit.SmartGripEventListener
import com.hihonor.smartgripkit.SmartGripEventManager
import kotlin.math.roundToInt

/** 悬浮件横向倾向: -1f 贴左手一侧, 0f 居中, 1f 贴右手一侧 */
internal val LocalGripBias = staticCompositionLocalOf<() -> Float> { { 0f } }

/** 这台机器上随心握是否真的可用; 不可用时悬浮件保持原样, 不收窄也不挪 */
internal val LocalGripAvailable = staticCompositionLocalOf { false }

private const val GRIP_TAG = "GripFollow"

/** 底栏要挪得出来, 左右至少各留这么多余量 */
internal val GripMinSlide = 32.dp

/** 悬浮键能挪的极限就是它自带的边距, 再多就出屏了 */
internal val GripFabShift = 16.dp

@Stable
internal class GripState(
    private val availableState: MutableState<Boolean>,
    val bias: () -> Float,
) {
    val available: Boolean get() = availableState.value
}

/**
 * 荣耀随心握: 单手握持时把悬浮件整条靠向那只手, 双手/平放回到正中.
 *
 * SDK 内部反射的是荣耀框架的隐藏类, 非荣耀机型连静态初始化都过不去, 所以每个入口都按
 * Throwable 兜住 —— 兜住的结果就是悬浮件一直居中, 在别人手机上跟这个功能不存在一样.
 *
 * 系统里的"随心握"开关是唯一的总闸 (荣耀 SDK 自己也是这么判的), 所以应用内不再放开关.
 */
@Composable
internal fun rememberGripState(enabled: Boolean = true): GripState {
    val context = LocalContext.current
    val availableState = remember { mutableStateOf(false) }
    val target = remember { mutableFloatStateOf(0f) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(context, enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }
        val support = try {
            SmartGripEventManager.getSmartGripSupportState(context)
        } catch (t: Throwable) {
            Log.i(GRIP_TAG, "unavailable: ${t.javaClass.simpleName}")
            null
        }
        if (support != SmartGripEventManager.SMART_GRIP_SUPPORT) {
            Log.i(GRIP_TAG, "off, supportState=$support")
            return@DisposableEffect onDispose { }
        }
        val listener = object : SmartGripEventListener() {
            override fun onSmartGripEventChanged(state: Int) {
                // 回调来自 binder 线程, 写 Compose 状态要回主线程
                mainHandler.post {
                    target.floatValue = when (state) {
                        SmartGripEventManager.GRIP_STATE_LEFT_HAND -> -1f
                        SmartGripEventManager.GRIP_STATE_RIGHT_HAND -> 1f
                        else -> 0f
                    }
                }
            }
        }
        val registered = try {
            SmartGripEventManager.registerSmartGripMotionListener(context, listener)
        } catch (t: Throwable) {
            Log.e(GRIP_TAG, "register failed", t)
            false
        }
        Log.i(GRIP_TAG, "registered=$registered")
        availableState.value = registered
        onDispose {
            availableState.value = false
            target.floatValue = 0f
            mainHandler.removeCallbacksAndMessages(null)
            if (registered) {
                try {
                    SmartGripEventManager.unregisterSmartGripMotionListener(context, listener)
                } catch (t: Throwable) {
                    Log.e(GRIP_TAG, "unregister failed", t)
                }
            }
        }
    }

    val animated = animateFloatAsState(
        targetValue = target.floatValue,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
        label = "gripBias",
    )
    return remember(availableState, animated) { GripState(availableState) { animated.value } }
}

/**
 * 底栏的随心握: 整条横向挪 "居中时一侧剩下的空白" × 倾向, 于是最多正好贴到边, 挪不出屏幕.
 *
 * 同时把宽度压到可用宽度减去左右各 [GripMinSlide] —— 4 个 tab 的胶囊在很多机型上本来就已经
 * 占满整行, 不收窄就没有余量可挪, 这个功能等于没开.
 *
 * @param availableWidthPx 底栏可摆放的宽度 (外层容器扣掉左右留白之后)
 */
@Composable
internal fun Modifier.gripFollow(availableWidthPx: Int): Modifier {
    if (!LocalGripAvailable.current) return this
    val bias = LocalGripBias.current
    val minSlidePx = with(LocalDensity.current) { GripMinSlide.toPx() }
    val maxWidthPx = (availableWidthPx - minSlidePx * 2f).coerceAtLeast(availableWidthPx / 2f)
    return this then Modifier
        .widthIn(max = with(LocalDensity.current) { maxWidthPx.toDp() })
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                val slack = ((availableWidthPx - placeable.width) / 2f).coerceAtLeast(0f)
                placeable.placeRelative(x = (slack * bias()).roundToInt(), y = 0)
            }
        }
}

/**
 * 悬浮键的随心握: 和底栏同一个方向、同一条弹簧往握持那侧挪一档.
 *
 * 键不跟着走, 单手时就比底栏更难够到.
 */
@Composable
internal fun Modifier.gripShift(): Modifier {
    if (!LocalGripAvailable.current) return this
    val bias = LocalGripBias.current
    val maxPx = with(LocalDensity.current) { GripFabShift.toPx() }
    return this then Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = (bias() * maxPx).roundToInt(), y = 0)
        }
    }
}
