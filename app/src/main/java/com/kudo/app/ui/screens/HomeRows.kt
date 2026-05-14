package com.kudo.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.core.model.KudoStoreItem
import com.kudo.app.core.model.KudoStoreKind
import com.kudo.app.core.model.KudoSubtask
import com.kudo.app.core.model.KudoTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun TaskRow(
    task: KudoTask,
    palette: KudoPalette,
    finalMultiplier: Float,
    isActive: Boolean = false,
    isDragging: Boolean = false,
    expanded: Boolean = false,
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit = {},
    onComplete: () -> Unit,
    onCompleteSubtask: (Long) -> Unit,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val reward = (task.remainingCoins * finalMultiplier).toInt()
    val dueBadge = remember(task.dueAtEpochMillis, palette) {
        task.dueAtEpochMillis?.let { dueBadgeFor(it, palette) }
    }
    val subtaskProgress = remember(task.subtasks) {
        if (task.hasSubtasks) {
            "${task.completedSubtaskCount}/${task.subtasks.size}"
        } else {
            null
        }
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "subtaskChevronRotation"
    )
    val activeBorderColor = if (isActive) palette.green.copy(alpha = 0.5f) else palette.line
    SwipeActionCard(
        modifier = modifier,
        reorderModifier = reorderModifier,
        palette = palette,
        lifted = isDragging,
        positiveAction = SwipeVisualAction(
            color = palette.green,
            icon = Icons.Rounded.Check,
            alignStart = true
        ),
        negativeAction = SwipeVisualAction(
            color = palette.green,
            icon = Icons.Rounded.Check,
            alignStart = false
        ),
        onPositiveAllowed = { true },
        onNegativeAllowed = { true },
        onPositiveCommit = onComplete,
        onNegativeCommit = onComplete,
        onPositiveCommitHaptic = haptics::vibrateDoubleTick,
        onNegativeReleaseDurationMs = 280,
        onNegativeDismissScale = 0.95f,
        onNegativeRejected = {},
        onTap = onEdit,
        swipeEnabled = swipeEnabled,
        onGestureLockChange = onGestureLockChange
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, activeBorderColor, RoundedCornerShape(14.dp))
                .then(if (isActive) Modifier.background(palette.greenBg.copy(alpha = 0.18f), RoundedCornerShape(14.dp)) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = palette.textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    dueBadge?.let { badge ->
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = badge.text,
                            color = badge.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subtaskProgress?.let { progress ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(palette.background)
                                .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onToggleExpanded
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = palette.textSub,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = progress,
                                color = palette.textSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (expanded) {
                                    "Collapse subtasks"
                                } else {
                                    "Expand subtasks"
                                },
                                tint = palette.textSub,
                                modifier = Modifier
                                    .size(14.dp)
                                    .graphicsLayer { rotationZ = chevronRotation }
                            )
                        }
                    }
                    Text(
                        text = "+${'$'}" + reward,
                        color = palette.green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.greenBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded && task.hasSubtasks,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = palette.line)
                    task.subtasks.forEach { subtask ->
                        SubtaskRow(
                            subtask = subtask,
                            palette = palette,
                            finalMultiplier = finalMultiplier,
                            onComplete = {
                                if (!subtask.isCompleted) {
                                    haptics.vibrate(HapticTickMs)
                                    onCompleteSubtask(subtask.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: KudoSubtask,
    palette: KudoPalette,
    finalMultiplier: Float,
    onComplete: () -> Unit
) {
    val reward = (subtask.coins * finalMultiplier).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .clickable(
                enabled = !subtask.isCompleted,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onComplete
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = if (subtask.isCompleted) palette.green else palette.line,
                    shape = CircleShape
                )
                .background(if (subtask.isCompleted) palette.greenBg else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = palette.green,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = subtask.title,
            color = if (subtask.isCompleted) palette.textSub else palette.textMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "+${'$'}" + reward,
            color = if (subtask.isCompleted) palette.textSub else palette.green,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun StoreRow(
    item: KudoStoreItem,
    coins: Int,
    palette: KudoPalette,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit = {},
    onBuyGesture: () -> Boolean,
    onEdit: () -> Unit
) {
    val canBuy = coins >= item.cost
    SwipeActionCard(
        modifier = modifier,
        reorderModifier = reorderModifier,
        palette = palette,
        lifted = isDragging,
        positiveAction = SwipeVisualAction(
            color = palette.orange,
            icon = Icons.Rounded.ShoppingCart,
            alignStart = true
        ),
        negativeAction = SwipeVisualAction(
            color = palette.orange,
            icon = Icons.Rounded.ShoppingCart,
            alignStart = false
        ),
        onPositiveAllowed = { canBuy },
        onNegativeAllowed = { canBuy },
        onPositiveCommit = { onBuyGesture() },
        onNegativeCommit = { onBuyGesture() },
        onTap = onEdit,
        swipeEnabled = swipeEnabled,
        onGestureLockChange = onGestureLockChange,
        failureFlashColor = palette.red,
        alpha = if (coins >= item.cost) 1f else 0.55f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.line, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = palette.textMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (item.kind == KudoStoreKind.Repeatable) "Infinite" else "One-time",
                    color = palette.textSub,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "-${'$'}" + item.cost,
                color = palette.orange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.orangeBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

private data class SwipeVisualAction(
    val color: Color,
    val icon: ImageVector,
    val alignStart: Boolean
)

private data class SwipeResolvedAction(
    val visual: SwipeVisualAction,
    val allowed: () -> Boolean,
    val commit: () -> Unit,
    val onRejected: (() -> Unit)? = null,
    val onCommitHaptic: (() -> Unit)? = null,
    val releaseDurationMs: Int = 220,
    val dismissScale: Float = 1f
)

@Composable
private fun SwipeActionCard(
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    palette: KudoPalette,
    lifted: Boolean = false,
    positiveAction: SwipeVisualAction,
    negativeAction: SwipeVisualAction,
    onPositiveAllowed: () -> Boolean,
    onNegativeAllowed: () -> Boolean,
    onPositiveCommit: () -> Unit,
    onNegativeCommit: () -> Unit,
    onPositiveCommitHaptic: (() -> Unit)? = null,
    onNegativeCommitHaptic: (() -> Unit)? = null,
    onPositiveReleaseDurationMs: Int = 220,
    onNegativeReleaseDurationMs: Int = 220,
    onPositiveDismissScale: Float = 1f,
    onNegativeDismissScale: Float = 1f,
    onTap: () -> Unit,
    swipeEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit = {},
    onPositiveRejected: (() -> Unit)? = null,
    onNegativeRejected: (() -> Unit)? = null,
    failureFlashColor: Color? = null,
    alpha: Float = 1f,
    content: @Composable () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val scope = rememberCoroutineScope()
    var rawOffsetX by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var releaseDurationMs by remember { mutableIntStateOf(180) }
    var errorShakeTargetX by remember { mutableFloatStateOf(0f) }
    var errorFlashVisible by remember { mutableStateOf(false) }
    var dismissAlphaTarget by remember { mutableFloatStateOf(1f) }
    var dismissScaleTarget by remember { mutableFloatStateOf(1f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = rawOffsetX,
        animationSpec = tween(durationMillis = if (isDragging) 0 else releaseDurationMs),
        label = "swipeOffset"
    )
    val errorShakeX by animateFloatAsState(
        targetValue = errorShakeTargetX,
        animationSpec = tween(durationMillis = 45),
        label = "swipeErrorShake"
    )
    val liftScale by animateFloatAsState(
        targetValue = if (lifted) 1.028f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 760f
        ),
        label = "swipeLiftScale"
    )
    val liftAlpha by animateFloatAsState(
        targetValue = if (lifted) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = 0.92f,
            stiffness = 760f
        ),
        label = "swipeLiftAlpha"
    )
    val liftElevation by animateDpAsState(
        targetValue = if (lifted) 16.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = 760f
        ),
        label = "swipeLiftElevation"
    )
    val errorFlashProgress by animateFloatAsState(
        targetValue = if (errorFlashVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "swipeErrorFlash"
    )
    val dismissAlpha by animateFloatAsState(
        targetValue = dismissAlphaTarget,
        animationSpec = tween(durationMillis = if (isDragging) 0 else releaseDurationMs),
        label = "swipeDismissAlpha"
    )
    val dismissScale by animateFloatAsState(
        targetValue = dismissScaleTarget,
        animationSpec = tween(durationMillis = if (isDragging) 0 else releaseDurationMs),
        label = "swipeDismissScale"
    )
    val animatedCardColor = if (failureFlashColor != null) {
        lerp(
            start = palette.card.copy(alpha = alpha),
            stop = failureFlashColor.copy(alpha = 0.68f),
            fraction = errorFlashProgress
        )
    } else {
        palette.card.copy(alpha = alpha)
    }
    val displayOffsetX = animatedOffsetX + errorShakeX
    val activeAction = when {
        animatedOffsetX > 30f -> positiveAction
        animatedOffsetX < -30f -> negativeAction
        else -> null
    }

    LaunchedEffect(swipeEnabled) {
        if (!swipeEnabled) {
            onGestureLockChange(false)
        }
    }
    DisposableEffect(Unit) {
        onDispose { onGestureLockChange(false) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .clip(RoundedCornerShape(14.dp))
            .background(activeAction?.color ?: palette.card)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 20.dp),
            contentAlignment = if (activeAction?.alignStart != false) {
                Alignment.CenterStart
            } else {
                Alignment.CenterEnd
            }
        ) {
            activeAction?.let { action ->
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(displayOffsetX.roundToInt(), 0) }
                .graphicsLayer {
                    scaleX = liftScale * dismissScale
                    scaleY = liftScale * dismissScale
                    this.alpha = liftAlpha * dismissAlpha
                }
                .then(reorderModifier)
                .then(
                    if (swipeEnabled) {
                        Modifier.draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                onGestureLockChange(true)
                                isDragging = true
                                dismissAlphaTarget = 1f
                                rawOffsetX = (rawOffsetX + delta).coerceIn(-widthPx, widthPx)
                            },
                            onDragStopped = {
                                scope.launch {
                                    onGestureLockChange(false)
                                    isDragging = false
                                    val threshold = widthPx * 0.28f
                                    val action = when {
                                        rawOffsetX > threshold -> SwipeResolvedAction(
                                            visual = positiveAction,
                                            allowed = onPositiveAllowed,
                                            commit = onPositiveCommit,
                                            onRejected = onPositiveRejected,
                                            onCommitHaptic = onPositiveCommitHaptic,
                                            releaseDurationMs = onPositiveReleaseDurationMs,
                                            dismissScale = onPositiveDismissScale
                                        )
                                        rawOffsetX < -threshold -> SwipeResolvedAction(
                                            visual = negativeAction,
                                            allowed = onNegativeAllowed,
                                            commit = onNegativeCommit,
                                            onRejected = onNegativeRejected,
                                            onCommitHaptic = onNegativeCommitHaptic,
                                            releaseDurationMs = onNegativeReleaseDurationMs,
                                            dismissScale = onNegativeDismissScale
                                        )
                                        else -> null
                                    }

                                    if (action == null) {
                                        releaseDurationMs = 240
                                        dismissAlphaTarget = 1f
                                        dismissScaleTarget = 1f
                                        rawOffsetX = 0f
                                        return@launch
                                    }

                                    if (action.allowed()) {
                                        releaseDurationMs = action.releaseDurationMs
                                        dismissAlphaTarget = 0f
                                        dismissScaleTarget = action.dismissScale
                                        action.onCommitHaptic?.invoke() ?: haptics.vibrate(25)
                                        rawOffsetX = if (action.visual.alignStart) {
                                            widthPx * 1.18f
                                        } else {
                                            -widthPx * 1.18f
                                        }
                                        delay(action.releaseDurationMs.toLong())
                                        action.commit()
                                        dismissAlphaTarget = 1f
                                        dismissScaleTarget = 1f
                                    } else {
                                        action.onRejected?.invoke()
                                        releaseDurationMs = 240
                                        haptics.vibrate(35)
                                        dismissScaleTarget = 1f
                                        rawOffsetX = 0f
                                        if (failureFlashColor != null) {
                                            errorFlashVisible = true
                                            listOf(-6f, 6f, -4f, 4f, 0f).forEach { target ->
                                                errorShakeTargetX = target
                                                delay(45)
                                            }
                                            errorFlashVisible = false
                                            errorShakeTargetX = 0f
                                        }
                                    }
                                    rawOffsetX = 0f
                                }
                            }
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    enabled = !lifted && !isDragging,
                    onClick = onTap
                ),
            colors = CardDefaults.cardColors(containerColor = animatedCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = liftElevation),
            shape = RoundedCornerShape(14.dp)
        ) {
            content()
        }
    }
}
