@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.kudo.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kudo.app.core.model.KudoTask
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val HabitGridSpacing = 10.dp
private val HabitGridItemHeight = 64.dp

@Composable
internal fun HabitsGrid(
    habits: List<KudoTask>,
    palette: KudoPalette,
    finalMultiplier: Float,
    isJiggleMode: Boolean,
    modifier: Modifier = Modifier,
    onGestureLockChange: (Boolean) -> Unit,
    onHabitsChange: (List<KudoTask>) -> Unit,
    onDragFinished: () -> Unit,
    onCompleteHabit: (Long) -> Unit,
    onEnterJiggle: () -> Unit,
    onDeleteHabit: (Long) -> Unit
) {
    if (habits.isEmpty()) return

    val density = LocalDensity.current
    val haptics = rememberKudoHaptics()
    val latestHabits by rememberUpdatedState(habits)
    val latestOnGestureLockChange by rememberUpdatedState(onGestureLockChange)
    val latestOnHabitsChange by rememberUpdatedState(onHabitsChange)
    val latestOnDragFinished by rememberUpdatedState(onDragFinished)

    var draggingHabitId by remember { mutableStateOf<Long?>(null) }
    var draggingPosition by remember { mutableStateOf(Offset.Zero) }
    var dragChangedOrder by remember { mutableStateOf(false) }
    var lastHabitSwapHapticAtMs by remember { mutableStateOf(0L) }

    LaunchedEffect(isJiggleMode) {
        if (!isJiggleMode) {
            draggingHabitId = null
            dragChangedOrder = false
            lastHabitSwapHapticAtMs = 0L
        }
    }
    DisposableEffect(Unit) {
        onDispose { latestOnGestureLockChange(false) }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val spacingPx = with(density) { HabitGridSpacing.roundToPx() }
        val itemHeightPx = with(density) { HabitGridItemHeight.roundToPx() }
        val itemWidthPx = ((constraints.maxWidth - spacingPx).coerceAtLeast(0) / 2f)
        val rowCount = (habits.size + 1) / 2
        val gridHeightPx = rowCount * itemHeightPx + (rowCount - 1).coerceAtLeast(0) * spacingPx
        val maxDragX = (itemWidthPx + spacingPx).coerceAtLeast(0f)
        val maxDragY = (gridHeightPx - itemHeightPx).coerceAtLeast(0).toFloat()
        val gridHeight = with(density) {
            (
                rowCount * itemHeightPx +
                    (rowCount - 1).coerceAtLeast(0) * spacingPx
                ).toDp()
        }
        val itemWidthDp = with(density) { itemWidthPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
                .then(
                    if (isJiggleMode) {
                        Modifier.pointerInput(isJiggleMode) {
                            awaitEachGesture {
                                awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial
                                )
                                latestOnGestureLockChange(true)
                                try {
                                    var hasPressedPointers = true
                                    while (hasPressedPointers) {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                                        hasPressedPointers = event.changes.any { it.pressed }
                                    }
                                } finally {
                                    latestOnGestureLockChange(false)
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            habits.forEachIndexed { index, habit ->
                key(habit.id) {
                    val targetSlot = remember(index, itemWidthPx, itemHeightPx, spacingPx) {
                        habitGridSlotOffset(
                            index = index,
                            itemWidthPx = itemWidthPx,
                            itemHeightPx = itemHeightPx,
                            spacingPx = spacingPx
                        )
                    }
                    val isDragging = draggingHabitId == habit.id
                    val animatedOffset by animateIntOffsetAsState(
                        targetValue = if (isDragging) {
                            IntOffset(
                                draggingPosition.x.roundToInt(),
                                draggingPosition.y.roundToInt()
                            )
                        } else {
                            IntOffset(
                                targetSlot.x.roundToInt(),
                                targetSlot.y.roundToInt()
                            )
                        },
                        animationSpec = if (isDragging) {
                            tween(durationMillis = 0)
                        } else {
                            spring(
                                dampingRatio = 0.84f,
                                stiffness = 620f
                            )
                        },
                        label = "habitGridOffset"
                    )

                    HabitChip(
                        task = habit,
                        palette = palette,
                        finalMultiplier = finalMultiplier,
                        isJiggleMode = isJiggleMode,
                        isDragging = isDragging,
                        modifier = Modifier
                            .offset { animatedOffset }
                            .width(itemWidthDp)
                            .height(HabitGridItemHeight)
                            .zIndex(if (isDragging) 3f else 0f)
                            .then(
                                if (isJiggleMode) {
                                    Modifier.pointerInput(
                                        habit.id,
                                        isJiggleMode,
                                        itemWidthPx,
                                        itemHeightPx,
                                        spacingPx
                                    ) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggingHabitId = habit.id
                                                draggingPosition = targetSlot
                                                dragChangedOrder = false
                                                lastHabitSwapHapticAtMs = 0L
                                                haptics.vibrate(HapticTickMs)
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (draggingHabitId != habit.id) return@detectDragGestures
                                                change.consume()
                                                draggingPosition = Offset(
                                                    x = (draggingPosition.x + dragAmount.x)
                                                        .coerceIn(0f, maxDragX),
                                                    y = (draggingPosition.y + dragAmount.y)
                                                        .coerceIn(0f, maxDragY)
                                                )

                                                val currentHabits = latestHabits
                                                val fromIndex = currentHabits.indexOfFirst { it.id == habit.id }
                                                if (fromIndex == -1) return@detectDragGestures

                                                val targetIndex = habitGridIndexForPosition(
                                                    centerX = draggingPosition.x + itemWidthPx / 2f,
                                                    centerY = draggingPosition.y + itemHeightPx / 2f,
                                                    itemCount = currentHabits.size,
                                                    itemWidthPx = itemWidthPx,
                                                    itemHeightPx = itemHeightPx,
                                                    spacingPx = spacingPx
                                                )

                                                if (targetIndex != fromIndex) {
                                                    latestOnHabitsChange(
                                                        moveListItem(
                                                            list = currentHabits,
                                                            fromIndex = fromIndex,
                                                            toIndex = targetIndex
                                                        )
                                                    )
                                                    dragChangedOrder = true
                                                    lastHabitSwapHapticAtMs =
                                                        maybeTriggerReorderSwapHaptic(
                                                            haptics = haptics,
                                                            lastHapticAtMs = lastHabitSwapHapticAtMs
                                                        )
                                                }
                                            },
                                            onDragEnd = {
                                                draggingHabitId = null
                                                if (dragChangedOrder) {
                                                    haptics.vibrate(HapticConfirmMs)
                                                    latestOnDragFinished()
                                                }
                                                dragChangedOrder = false
                                                lastHabitSwapHapticAtMs = 0L
                                            },
                                            onDragCancel = {
                                                draggingHabitId = null
                                                if (dragChangedOrder) {
                                                    haptics.vibrate(HapticConfirmMs)
                                                    latestOnDragFinished()
                                                }
                                                dragChangedOrder = false
                                                lastHabitSwapHapticAtMs = 0L
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        onComplete = { onCompleteHabit(habit.id) },
                        onEnterJiggle = onEnterJiggle,
                        onDelete = { onDeleteHabit(habit.id) }
                    )
                }
            }
        }
    }
}

private fun habitGridSlotOffset(
    index: Int,
    itemWidthPx: Float,
    itemHeightPx: Int,
    spacingPx: Int
): Offset {
    val column = index % 2
    val row = index / 2
    return Offset(
        x = column * (itemWidthPx + spacingPx),
        y = row * (itemHeightPx + spacingPx).toFloat()
    )
}

private fun habitGridIndexForPosition(
    centerX: Float,
    centerY: Float,
    itemCount: Int,
    itemWidthPx: Float,
    itemHeightPx: Int,
    spacingPx: Int
): Int {
    if (itemCount <= 1) return 0

    val columnWidth = itemWidthPx + spacingPx
    val rowHeight = itemHeightPx + spacingPx
    val column = (centerX / columnWidth).toInt().coerceIn(0, 1)
    val row = (centerY / rowHeight).toInt().coerceAtLeast(0)
    return (row * 2 + column).coerceIn(0, itemCount - 1)
}

private val HabitChargePattern = listOf(
    0L to 2L,
    80L to 3L,
    160L to 3L,
    240L to 4L,
    320L to 4L,
    400L to 4L,
    480L to 5L,
    560L to 5L,
    640L to 5L,
    720L to 6L,
    800L to 6L,
    880L to 6L,
    960L to 7L,
    1040L to 7L,
    1120L to 7L
)

@Composable
private fun rememberHabitJiggleRotation(enabled: Boolean): Float {
    if (!enabled) return 0f

    val jiggleTransition = rememberInfiniteTransition(label = "habitJiggle")
    val jiggleRotation by jiggleTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "habitJiggleRotation"
    )
    return jiggleRotation
}

@Composable
private fun HabitChip(
    task: KudoTask,
    palette: KudoPalette,
    finalMultiplier: Float,
    isJiggleMode: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit,
    onEnterJiggle: () -> Unit,
    onDelete: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)
    val jiggleRotation = rememberHabitJiggleRotation(isJiggleMode)
    val completedToday = remember(task.last) { isToday(task.last) }
    val value = remember(task.coins, finalMultiplier) {
        (task.coins * finalMultiplier).toInt()
    }
    var isCharging by remember(task.id) { mutableStateOf(false) }
    var chargeJob by remember(task.id) { mutableStateOf<Job?>(null) }
    val chargeProgress = remember(task.id) { Animatable(0f) }
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.035f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 760f
        ),
        label = "habitDragScale"
    )
    val dragAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = 760f
        ),
        label = "habitDragAlpha"
    )
    val dragShadow by animateDpAsState(
        targetValue = if (isDragging) 14.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = 760f
        ),
        label = "habitDragShadow"
    )

    fun cancelCharge(withReleaseHaptic: Boolean = true) {
        chargeJob?.cancel()
        chargeJob = null
        if (isCharging && withReleaseHaptic) {
            haptics.vibrate(4)
        }
        isCharging = false
        scope.launch {
            chargeProgress.stop()
            chargeProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 110, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(isJiggleMode) {
        if (isJiggleMode) {
            cancelCharge(withReleaseHaptic = false)
        }
    }

    DisposableEffect(task.id) {
        onDispose {
            chargeJob?.cancel()
        }
    }

    fun startCharge() {
        if (isJiggleMode) {
            return
        }
        if (isCharging) {
            cancelCharge()
            return
        }

        isCharging = true
        chargeJob = scope.launch {
            chargeProgress.stop()
            chargeProgress.snapTo(0f)
            val progressJob = launch {
                chargeProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
                )
            }
            var previous = 0L
            HabitChargePattern.forEach { (time, duration) ->
                delay(time - previous)
                previous = time
                if (isActive) {
                    haptics.vibrate(duration)
                }
            }
            delay(1500L - previous)
            if (isActive) {
                progressJob.cancel()
                chargeProgress.snapTo(1f)
                onComplete()
                haptics.vibrate(25)
                isCharging = false
                chargeJob = null
                delay(90)
                chargeProgress.snapTo(0f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(dragShadow, shape, clip = false)
            .clip(shape)
            .border(1.dp, palette.line, shape)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                rotationZ = if (isJiggleMode && !isDragging) jiggleRotation else 0f
                scaleX = dragScale
                scaleY = dragScale
                alpha = dragAlpha
            }
            .combinedClickable(
                enabled = !isJiggleMode,
                onClick = { startCharge() },
                onLongClick = {
                    if (!isJiggleMode) {
                        cancelCharge(withReleaseHaptic = false)
                        haptics.vibrate(25)
                        onEnterJiggle()
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(palette.card)
                .drawBehind {
                    drawRect(
                        color = palette.greenBg,
                        size = Size(
                            width = size.width * chargeProgress.value,
                            height = size.height
                        )
                    )
                }
        )
        AnimatedVisibility(
            visible = isJiggleMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .zIndex(2f)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFCF6679))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    color = palette.textMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "+${'$'}" + value,
                    color = palette.green,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (completedToday) "x${task.count}" else if (isCharging) "..." else "Go",
                color = if (completedToday) palette.background else palette.textSub,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (completedToday) palette.green else palette.line)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
