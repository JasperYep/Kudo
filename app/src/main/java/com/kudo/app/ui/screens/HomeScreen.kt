@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.kudo.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.ReorderableItem
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoveDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kudo.app.core.platform.KudoHaptics
import com.kudo.app.core.model.KudoLogEntry
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoStoreItem
import com.kudo.app.core.model.KudoTask
import com.kudo.app.core.repository.KudoStateRepository
import com.kudo.app.ui.theme.DarkBackground
import com.kudo.app.ui.theme.DarkBlue
import com.kudo.app.ui.theme.DarkCard
import com.kudo.app.ui.theme.DarkGold
import com.kudo.app.ui.theme.DarkGoldBg
import com.kudo.app.ui.theme.DarkGreen
import com.kudo.app.ui.theme.DarkGreenBg
import com.kudo.app.ui.theme.DarkLine
import com.kudo.app.ui.theme.DarkOrange
import com.kudo.app.ui.theme.DarkOrangeBg
import com.kudo.app.ui.theme.DarkRed
import com.kudo.app.ui.theme.DarkTextMain
import com.kudo.app.ui.theme.DarkTextSub
import com.kudo.app.ui.theme.LightBackground
import com.kudo.app.ui.theme.LightBlue
import com.kudo.app.ui.theme.LightCard
import com.kudo.app.ui.theme.LightGold
import com.kudo.app.ui.theme.LightGoldBg
import com.kudo.app.ui.theme.LightGreen
import com.kudo.app.ui.theme.LightGreenBg
import com.kudo.app.ui.theme.LightLine
import com.kudo.app.ui.theme.LightOrange
import com.kudo.app.ui.theme.LightOrangeBg
import com.kudo.app.ui.theme.LightRed
import com.kudo.app.ui.theme.LightTextMain
import com.kudo.app.ui.theme.LightTextSub
import com.kudo.app.ui.viewmodel.EditingTarget
import com.kudo.app.ui.viewmodel.KudoUiState
import com.kudo.app.ui.viewmodel.KudoViewModel
import com.kudo.app.ui.viewmodel.TaskCreationTarget
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: KudoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val palette = rememberPalette(
        when (uiState.theme) {
            KudoStateRepository.THEME_LIGHT -> false
            KudoStateRepository.THEME_DARK -> true
            else -> isSystemDark
        }
    )
    val context = LocalContext.current

    var taskDraftTitle by rememberSaveable { mutableStateOf("") }
    var taskDraftValue by rememberSaveable { mutableStateOf("") }
    var storeDraftTitle by rememberSaveable { mutableStateOf("") }
    var storeDraftValue by rememberSaveable { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var isFileTransferInProgress by remember { mutableStateOf(false) }
    var activeComposerRevealProgress by remember { mutableFloatStateOf(0f) }
    val tasksListState = rememberLazyListState()
    val storeListState = rememberLazyListState()
    val logListState = rememberLazyListState()
    LaunchedEffect(uiState.currentView) {
        activeComposerRevealProgress = 0f
    }
    val pullHintAlpha = ((activeComposerRevealProgress * 0.45f) + 0.2f)
        .coerceIn(0.2f, 0.55f)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isFileTransferInProgress = true
            viewModel.exportToUri(uri) { success ->
                isFileTransferInProgress = false
                Toast.makeText(
                    context,
                    if (success) "Backup saved" else "Export failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    Scaffold(
        containerColor = palette.background,
        bottomBar = {
            BottomTabs(
                uiState = uiState,
                palette = palette,
                onViewSelected = viewModel::switchView
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background)
                .padding(innerPadding)
        ) {
            Header(
                state = uiState.data,
                palette = palette,
                showPullHint = uiState.currentView != KudoViewModel.VIEW_LOG && activeComposerRevealProgress <= 0.02f,
                pullHintAlpha = pullHintAlpha,
                onOpenSettings = viewModel::openSettings
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.currentView) {
                    KudoViewModel.VIEW_TASKS -> PullComposerPage(
                        uiState = uiState,
                        palette = palette,
                        title = taskDraftTitle,
                        value = taskDraftValue,
                        listState = tasksListState,
                        onTitleChange = { taskDraftTitle = it },
                        onValueChange = { taskDraftValue = it.filter(Char::isDigit) },
                        onModeToggle = viewModel::cycleTaskCreationTarget,
                        onAdd = {
                            if (viewModel.addDashboardItem(taskDraftTitle, taskDraftValue)) {
                                taskDraftTitle = ""
                                taskDraftValue = ""
                                true
                            } else {
                                false
                            }
                        },
                        onRevealProgressChanged = { activeComposerRevealProgress = it }
                    ) {
                        modifier ->
                        TasksPage(
                            uiState = uiState,
                            palette = palette,
                            modifier = modifier,
                            onToggleHabits = viewModel::toggleHabitsCollapsed,
                            onSetListMode = viewModel::setListMode,
                            onExitHabitJiggle = viewModel::exitHabitJiggleMode,
                            onEnterHabitJiggle = viewModel::enterHabitJiggleMode,
                            onDeleteHabit = viewModel::deleteTaskItem,
                            onReorderTask = viewModel::reorderCurrentTaskList,
                            onReorderHabits = viewModel::reorderHabits,
                            onCompleteTask = viewModel::completeTask,
                            onMoveTaskGesture = viewModel::moveTaskFromGesture,
                            onEditTask = viewModel::openEditTask,
                            onCompleteHabit = viewModel::completeHabit,
                            listState = tasksListState
                        )
                    }

                    KudoViewModel.VIEW_STORE -> PullComposerPage(
                        uiState = uiState,
                        palette = palette,
                        title = storeDraftTitle,
                        value = storeDraftValue,
                        listState = storeListState,
                        onTitleChange = { storeDraftTitle = it },
                        onValueChange = { storeDraftValue = it.filter(Char::isDigit) },
                        onModeToggle = viewModel::toggleStoreMode,
                        onAdd = {
                            if (viewModel.addDashboardItem(storeDraftTitle, storeDraftValue)) {
                                storeDraftTitle = ""
                                storeDraftValue = ""
                                true
                            } else {
                                false
                            }
                        },
                        onRevealProgressChanged = { activeComposerRevealProgress = it }
                    ) {
                        modifier ->
                        StorePage(
                            uiState = uiState,
                            palette = palette,
                            modifier = modifier,
                            onReorderStore = viewModel::reorderStore,
                            onBuyGesture = viewModel::purchaseItemFromGesture,
                            onEdit = viewModel::openEditStore,
                            listState = storeListState
                        )
                    }

                    KudoViewModel.VIEW_LOG -> LogPage(
                        uiState = uiState,
                        palette = palette,
                        modifier = Modifier.fillMaxSize(),
                        onUndo = viewModel::undoLog,
                        listState = logListState
                    )
                }
            }
        }
    }

    if (uiState.isSettingsVisible) {
        SettingsSheet(
            uiState = uiState,
            palette = palette,
            isFileActionInProgress = isFileTransferInProgress,
            onDismiss = viewModel::closeSettings,
            onToggleHelp = viewModel::toggleHelp,
            onSetTheme = viewModel::setTheme,
            onExport = {
                val filename = "kudo_backup_${System.currentTimeMillis()}.json"
                exportLauncher.launch(filename)
            },
            onImport = {
                openDocumentLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            }
        )
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Restore Data") },
            text = { Text("This will overwrite current local data. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingImportUri ?: return@TextButton
                        pendingImportUri = null
                        isFileTransferInProgress = true
                        viewModel.importFromUri(uri) { success ->
                            isFileTransferInProgress = false
                            if (success) {
                                Toast.makeText(context, "Restore complete", Toast.LENGTH_SHORT).show()
                                viewModel.closeSettings()
                            } else {
                                Toast.makeText(context, "Invalid backup file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val editTarget = uiState.pendingMoveTaskId?.let {
        EditingTarget(KudoViewModel.KIND_TASK, it)
    } ?: uiState.editingTarget

    if (editTarget != null) {
        EditSheet(
            uiState = uiState,
            palette = palette,
            target = editTarget,
            onDismiss = viewModel::closeEdit,
            onSave = viewModel::saveEditing,
            onDelete = viewModel::deleteEditing
        )
    }
}

@Immutable
private data class KudoPalette(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val line: Color,
    val textMain: Color,
    val textSub: Color,
    val green: Color,
    val greenBg: Color,
    val orange: Color,
    val orangeBg: Color,
    val gold: Color,
    val goldBg: Color,
    val blue: Color,
    val red: Color
)

private val HeaderHeight = 120.dp
private const val HapticTickMs = 8L
private const val HapticConfirmMs = 12L
private const val HapticErrorMs = 24L

private fun staticPalette(isDark: Boolean): KudoPalette {
    return if (isDark) {
        KudoPalette(
            isDark = true,
            background = DarkBackground,
            card = DarkCard,
            line = DarkLine,
            textMain = DarkTextMain,
            textSub = DarkTextSub,
            green = DarkGreen,
            greenBg = DarkGreenBg,
            orange = DarkOrange,
            orangeBg = DarkOrangeBg,
            gold = DarkGold,
            goldBg = DarkGoldBg,
            blue = DarkBlue,
            red = DarkRed
        )
    } else {
        KudoPalette(
            isDark = false,
            background = LightBackground,
            card = LightCard,
            line = LightLine,
            textMain = LightTextMain,
            textSub = LightTextSub,
            green = LightGreen,
            greenBg = LightGreenBg,
            orange = LightOrange,
            orangeBg = LightOrangeBg,
            gold = LightGold,
            goldBg = LightGoldBg,
            blue = LightBlue,
            red = LightRed
        )
    }
}

@Composable
private fun rememberPalette(isDark: Boolean): KudoPalette {
    val target = staticPalette(isDark)
    val animationSpec = tween<Color>(
        durationMillis = 420,
        easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    )

    val background by animateColorAsState(target.background, animationSpec, label = "paletteBackground")
    val card by animateColorAsState(target.card, animationSpec, label = "paletteCard")
    val line by animateColorAsState(target.line, animationSpec, label = "paletteLine")
    val textMain by animateColorAsState(target.textMain, animationSpec, label = "paletteTextMain")
    val textSub by animateColorAsState(target.textSub, animationSpec, label = "paletteTextSub")
    val green by animateColorAsState(target.green, animationSpec, label = "paletteGreen")
    val greenBg by animateColorAsState(target.greenBg, animationSpec, label = "paletteGreenBg")
    val orange by animateColorAsState(target.orange, animationSpec, label = "paletteOrange")
    val orangeBg by animateColorAsState(target.orangeBg, animationSpec, label = "paletteOrangeBg")
    val gold by animateColorAsState(target.gold, animationSpec, label = "paletteGold")
    val goldBg by animateColorAsState(target.goldBg, animationSpec, label = "paletteGoldBg")
    val blue by animateColorAsState(target.blue, animationSpec, label = "paletteBlue")
    val red by animateColorAsState(target.red, animationSpec, label = "paletteRed")

    return KudoPalette(
        isDark = target.isDark,
        background = background,
        card = card,
        line = line,
        textMain = textMain,
        textSub = textSub,
        green = green,
        greenBg = greenBg,
        orange = orange,
        orangeBg = orangeBg,
        gold = gold,
        goldBg = goldBg,
        blue = blue,
        red = red
    )
}

@Composable
private fun ComposerRevealPanel(
    revealProgress: Float,
    onMeasuredHeight: (Float) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val revealOffsetPx = with(density) { 10.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                onMeasuredHeight(placeable.height.toFloat())
                val revealHeight = (placeable.height * revealProgress).roundToInt()
                layout(placeable.width, revealHeight.coerceAtLeast(0)) {
                    placeable.placeRelative(0, revealHeight - placeable.height)
                }
            }
            .graphicsLayer {
                clip = true
                alpha = if (revealProgress <= 0f) 0f else lerpFloat(0.25f, 1f, revealProgress)
                translationY = (1f - revealProgress) * revealOffsetPx
            }
    ) {
        content()
    }
}

@Composable
private fun PullComposerPage(
    uiState: KudoUiState,
    palette: KudoPalette,
    title: String,
    value: String,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onTitleChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    onAdd: () -> Boolean,
    onRevealProgressChanged: (Float) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var composerRevealTargetPx by remember { mutableFloatStateOf(0f) }
    var composerMeasuredHeightPx by remember { mutableFloatStateOf(1f) }
    var composerOpenHapticArmed by remember { mutableStateOf(true) }
    var composerOpenHapticPending by remember { mutableStateOf(false) }

    val isListAtTop by remember(listState) {
        derivedStateOf {
            !listState.canScrollBackward ||
                (
                    listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset <= 1
                    )
        }
    }
    val haptics = rememberKudoHaptics()
    val density = LocalDensity.current
    val estimatedComposerHeightPx = with(density) { 120.dp.toPx() }
    val blankTapSlopPx = with(density) { 8.dp.toPx() }
    val openTriggerDistancePx = with(density) { 72.dp.toPx() }
    val closeTriggerDistancePx = with(density) { 52.dp.toPx() }
    val composerMaxHeightPx = composerMeasuredHeightPx.takeIf { it > 1f } ?: estimatedComposerHeightPx
    val composerClosedRearmProgress = 0.12f
    val composerVisuallyExpandedProgress = 0.995f
    val composerVisuallyExpandedPx = composerMaxHeightPx * composerVisuallyExpandedProgress
    val resolvedComposerTargetPx = composerRevealTargetPx.coerceIn(0f, composerMaxHeightPx)
    val composerRevealPx by animateFloatAsState(
        targetValue = resolvedComposerTargetPx,
        animationSpec = if (resolvedComposerTargetPx > 0f) {
            spring(
                stiffness = 420f,
                dampingRatio = 0.84f,
                visibilityThreshold = 0.25f
            )
        } else {
            spring(
                stiffness = 880f,
                dampingRatio = 0.92f,
                visibilityThreshold = 0.25f
            )
        },
        label = "composerReveal"
    )
    val composerRevealProgress = if (composerMeasuredHeightPx > 0f) {
        (composerRevealPx / composerMeasuredHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }

    fun triggerComposerOpenHaptic() {
        composerOpenHapticPending = false
        composerOpenHapticArmed = false
        haptics.vibrate(HapticTickMs)
    }

    fun armComposerOpenHapticIfNeeded(currentReveal: Float, shouldExpand: Boolean) {
        if (!shouldExpand || !composerOpenHapticArmed) {
            composerOpenHapticPending = false
            return
        }

        if (currentReveal >= composerVisuallyExpandedPx) {
            triggerComposerOpenHaptic()
        } else {
            composerOpenHapticPending = true
        }
    }

    fun openComposer() {
        if (composerRevealTargetPx >= composerMaxHeightPx - 0.5f) {
            return
        }
        composerRevealTargetPx = composerMaxHeightPx
        armComposerOpenHapticIfNeeded(
            currentReveal = composerRevealPx,
            shouldExpand = true
        )
    }

    fun collapseComposer() {
        composerOpenHapticPending = false
        composerRevealTargetPx = 0f
    }

    LaunchedEffect(composerRevealProgress) {
        onRevealProgressChanged(composerRevealProgress)
    }
    DisposableEffect(Unit) {
        onDispose { onRevealProgressChanged(0f) }
    }
    LaunchedEffect(
        composerRevealProgress,
        resolvedComposerTargetPx,
        composerMaxHeightPx,
        composerOpenHapticPending
    ) {
        val isVisuallyClosed = composerRevealProgress <= composerClosedRearmProgress
        if (isVisuallyClosed && !composerOpenHapticPending) {
            composerOpenHapticArmed = true
            return@LaunchedEffect
        }

        val shouldTriggerOpenHaptic =
            composerOpenHapticPending &&
                resolvedComposerTargetPx >= composerVisuallyExpandedPx &&
                composerRevealProgress >= composerVisuallyExpandedProgress
        if (shouldTriggerOpenHaptic) {
            triggerComposerOpenHaptic()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .pointerInput(isListAtTop, composerRevealProgress) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val canOpenThisGesture = isListAtTop && composerRevealProgress <= 0.02f
                    val canCloseThisGesture = composerRevealProgress >= 0.98f
                    var pointerId = down.id
                    var totalDx = 0f
                    var totalDy = 0f
                    var gestureHandled = false

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == pointerId }
                            ?: event.changes.firstOrNull()
                            ?: break
                        pointerId = change.id

                        val delta = change.position - change.previousPosition
                        totalDx += delta.x
                        totalDy += delta.y

                        if (!gestureHandled) {
                            val verticalDistance = kotlin.math.abs(totalDy)
                            val horizontalDistance = kotlin.math.abs(totalDx)
                            val isVerticalGesture = verticalDistance > horizontalDistance

                            if (isVerticalGesture && canOpenThisGesture && totalDy >= openTriggerDistancePx) {
                                openComposer()
                                change.consume()
                                gestureHandled = true
                            } else if (isVerticalGesture && canCloseThisGesture && totalDy <= -closeTriggerDistancePx) {
                                collapseComposer()
                                change.consume()
                                gestureHandled = true
                            }
                        }

                        if (change.changedToUpIgnoreConsumed()) {
                            break
                        }
                    }
                }
            }
            .pointerInput(composerRevealProgress, blankTapSlopPx) {
                if (composerRevealProgress < 0.98f) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Final
                    )
                    val start = down.position
                    var tapEligible = !down.isConsumed

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: continue
                        val delta = change.position - start
                        if ((delta.x * delta.x) + (delta.y * delta.y) > blankTapSlopPx * blankTapSlopPx) {
                            tapEligible = false
                        }
                        if (change.isConsumed) {
                            tapEligible = false
                        }
                        if (change.changedToUpIgnoreConsumed()) {
                            if (tapEligible && !change.isConsumed) {
                                collapseComposer()
                            }
                            break
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ComposerRevealPanel(
                revealProgress = composerRevealProgress,
                onMeasuredHeight = { measuredHeight ->
                    if (measuredHeight > 1f) {
                        composerMeasuredHeightPx = measuredHeight
                        composerRevealTargetPx = composerRevealTargetPx
                            .coerceIn(0f, measuredHeight)
                    }
                }
            ) {
                DashboardCard(
                    uiState = uiState,
                    palette = palette,
                    revealProgress = composerRevealProgress,
                    title = title,
                    value = value,
                    onTitleChange = onTitleChange,
                    onValueChange = onValueChange,
                    onModeToggle = onModeToggle,
                    onAdd = {
                        val added = onAdd()
                        if (added) {
                            collapseComposer()
                        }
                        added
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                content(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun Header(
    state: KudoState,
    palette: KudoPalette,
    showPullHint: Boolean,
    pullHintAlpha: Float,
    onOpenSettings: () -> Unit
) {
    val animatedCoins by animateIntAsState(targetValue = state.coins, label = "coins")
    val xpProgress by animateFloatAsState(
        targetValue = xpProgress(state),
        label = "xp"
    )

    Surface(
        color = palette.card,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "LVL ${state.level}",
                            color = palette.gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.goldBg)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(palette.line)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpProgress)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(palette.gold)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = palette.textSub,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${'$'}",
                            color = palette.green,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = animatedCoins.toString(),
                            color = palette.textMain,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Text(
                        text = "${formatMultiplier(state.finalMultiplier)}x",
                        color = palette.textSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, palette.line, RoundedCornerShape(6.dp))
                            .background(palette.card)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "swipe down to add...",
                color = palette.textSub.copy(alpha = if (showPullHint) pullHintAlpha else 0f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashboardCard(
    uiState: KudoUiState,
    palette: KudoPalette,
    revealProgress: Float,
    title: String,
    value: String,
    onTitleChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    onAdd: () -> Boolean
) {
    val haptics = rememberKudoHaptics()
    val titlePlaceholder = when {
        uiState.currentView == KudoViewModel.VIEW_STORE -> "Add to Store..."
        uiState.taskCreationTarget == TaskCreationTarget.HABIT -> "Add Habit..."
        else -> "Add Task..."
    }
    val valuePlaceholder = when {
        uiState.currentView == KudoViewModel.VIEW_STORE -> "0"
        uiState.taskCreationTarget == TaskCreationTarget.HABIT -> "10"
        else -> "" // Suggests it is completely optional
    }
    val modeText = when {
        uiState.currentView == KudoViewModel.VIEW_STORE && uiState.storeMode == KudoState.STORE_INFINITE -> "INFIN"
        uiState.currentView == KudoViewModel.VIEW_STORE -> "ONCE"
        uiState.taskCreationTarget == TaskCreationTarget.INBOX -> "INBOX"
        uiState.taskCreationTarget == TaskCreationTarget.FOCUS -> "FOCUS"
        else -> "HABIT"
    }
    val modeColor = when {
        uiState.currentView == KudoViewModel.VIEW_STORE -> palette.orange
        uiState.taskCreationTarget == TaskCreationTarget.INBOX -> palette.blue
        uiState.taskCreationTarget == TaskCreationTarget.FOCUS -> palette.green
        else -> palette.gold
    }
    val valueColor = when {
        uiState.currentView == KudoViewModel.VIEW_STORE -> palette.orange
        value.isBlank() || value == "0" -> palette.textSub
        else -> palette.green
    }
    val addButtonProgress = revealProgress.coerceIn(0f, 1f)
    val addButtonScale = lerpFloat(0.7f, 1f, addButtonProgress)
    val addButtonAlpha = lerpFloat(0.56f, 1f, addButtonProgress)
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            .border(1.dp, palette.line, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.line, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                DashboardTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = titlePlaceholder,
                    textColor = palette.textMain,
                    placeholderColor = palette.textSub.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Divider(color = palette.line)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${'$'}",
                        color = palette.textSub,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    DashboardTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.width(50.dp),
                        placeholder = valuePlaceholder,
                        textColor = valueColor,
                        placeholderColor = palette.textSub.copy(alpha = 0.4f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textAlign = TextAlign.Start
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(1.dp)
                        .height(16.dp)
                        .background(palette.line)
                )

                Text(
                    text = modeText,
                    color = modeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptics.vibrate(HapticTickMs)
                            onModeToggle()
                        }
                        .padding(horizontal = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = addButtonScale
                            scaleY = addButtonScale
                            alpha = addButtonAlpha
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.textMain)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val added = onAdd()
                            haptics.vibrate(if (added) HapticConfirmMs else HapticErrorMs)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = palette.background,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TasksPage(
    uiState: KudoUiState,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    onToggleHabits: () -> Unit,
    onSetListMode: (String) -> Unit,
    onExitHabitJiggle: () -> Unit,
    onEnterHabitJiggle: () -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onReorderTask: (List<Long>) -> Unit,
    onReorderHabits: (List<Long>) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onMoveTaskGesture: (Long) -> Boolean,
    onEditTask: (Long) -> Unit,
    onCompleteHabit: (Long) -> Unit,
    listState: LazyListState
) {
    val haptics = rememberKudoHaptics()
    val habits = remember(uiState.data.tasks) {
        uiState.data.tasks.filter { it.type == KudoState.TYPE_HABIT }
    }
    var localHabits by remember { mutableStateOf(habits) }
    LaunchedEffect(habits) {
        localHabits = habits
    }
    val tasks = remember(uiState.data.tasks, uiState.data.listMode) {
        uiState.data.tasks.filter {
            it.type == KudoState.TYPE_TASK && it.list == uiState.data.listMode
        }
    }

    var localTasks by remember(uiState.data.listMode) { mutableStateOf(tasks) }
    LaunchedEffect(tasks) {
        localTasks = tasks
    }
    val newTopTaskId = localTasks.firstOrNull()?.id?.takeIf { it == uiState.recentTaskInsertId }

    val localTaskIds = remember(localTasks) { localTasks.map(KudoTask::id).toSet() }

    key(uiState.data.listMode, uiState.habitsCollapsed, habits.size, tasks.size) {
        val state = rememberReorderableLazyListState(
            onMove = { from, to ->
                val fromId = from.key as? Long
                val toId = to.key as? Long
                if (fromId != null && toId != null) {
                    localTasks = localTasks.toMutableList().apply {
                        val fromIndex = indexOfFirst { it.id == fromId }
                        val toIndex = indexOfFirst { it.id == toId }
                        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                            add(toIndex, removeAt(fromIndex))
                        }
                    }
                }
            },
            canDragOver = { draggedOver, dragging ->
                (draggedOver.key as? Long)?.let(localTaskIds::contains) == true &&
                    (dragging.key as? Long)?.let(localTaskIds::contains) == true
            },
            listState = listState,
            onDragEnd = { _, _ ->
                if (localTasks.map(KudoTask::id) != tasks.map(KudoTask::id)) {
                    haptics.vibrate(8)
                    onReorderTask(localTasks.map(KudoTask::id))
                }
            },
            maxScrollPerFrame = 56.dp
        )

        var pendingDeleteHabitId by remember { mutableStateOf<Long?>(null) }

        LazyColumn(
            state = state.listState,
            modifier = modifier
                .reorderable(state)
        ) {
        if (habits.isNotEmpty()) {
            item(key = "habits_header") {
                SectionHeader(
                    title = "Habits",
                    palette = palette,
                    collapsible = true,
                    collapsed = uiState.habitsCollapsed,
                    onClick = {
                        onExitHabitJiggle()
                        onToggleHabits()
                    }
                )
            }
            if (!uiState.habitsCollapsed) {
                item(key = "habits_grid") {
                    HabitsGrid(
                        habits = localHabits,
                        palette = palette,
                        finalMultiplier = uiState.data.finalMultiplier,
                        isJiggleMode = uiState.isHabitJiggleMode,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onHabitsChange = { localHabits = it },
                        onDragFinished = {
                            if (localHabits.map(KudoTask::id) != habits.map(KudoTask::id)) {
                                onReorderHabits(localHabits.map(KudoTask::id))
                                haptics.vibrate(8)
                            }
                        },
                        onCompleteHabit = onCompleteHabit,
                        onEnterJiggle = onEnterHabitJiggle,
                        onDeleteHabit = { pendingDeleteHabitId = it }
                    )
                }
            }
        }

        item(key = "focus_inbox_switcher") {
            FocusInboxSwitcher(
                currentMode = uiState.data.listMode,
                palette = palette,
                onSetListMode = {
                    onExitHabitJiggle()
                    onSetListMode(it)
                }
            )
        }

        if (tasks.isEmpty()) {
            item(key = "empty_${uiState.data.listMode}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (uiState.isHabitJiggleMode) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onExitHabitJiggle() }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    EmptyState(
                        text = "Empty ${uiState.data.listMode}",
                        palette = palette
                    )
                }
            }
        } else {
            items(localTasks, key = { it.id }) { task ->
                ReorderableItem(
                    state = state,
                    key = task.id,
                    defaultDraggingModifier = Modifier
                ) { isDragging ->
                    if (task.id == newTopTaskId) {
                        StackInsertItem {
                            TaskRow(
                                task = task,
                                palette = palette,
                                finalMultiplier = uiState.data.finalMultiplier,
                                isDragging = isDragging,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 2.dp)
                                    .detectReorderAfterLongPress(state),
                                swipeEnabled = !isDragging,
                                onComplete = {
                                    onExitHabitJiggle()
                                    onCompleteTask(task.id)
                                },
                                onMoveGesture = {
                                    onExitHabitJiggle()
                                    onMoveTaskGesture(task.id)
                                },
                                onEdit = {
                                    onExitHabitJiggle()
                                    onEditTask(task.id)
                                }
                            )
                        }
                    } else {
                        TaskRow(
                            task = task,
                            palette = palette,
                            finalMultiplier = uiState.data.finalMultiplier,
                            isDragging = isDragging,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .detectReorderAfterLongPress(state),
                            swipeEnabled = !isDragging,
                            onComplete = {
                                onExitHabitJiggle()
                                onCompleteTask(task.id)
                            },
                            onMoveGesture = {
                                onExitHabitJiggle()
                                onMoveTaskGesture(task.id)
                            },
                            onEdit = {
                                onExitHabitJiggle()
                                onEditTask(task.id)
                            }
                        )
                    }
                }
            }
        }

        item(key = "tasks_bottom_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (pendingDeleteHabitId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteHabitId = null },
            title = { Text("Delete habit?") },
            text = { Text("This habit will be removed from your list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = pendingDeleteHabitId ?: return@TextButton
                        pendingDeleteHabitId = null
                        onDeleteHabit(id)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteHabitId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    }
}

@Composable
private fun StorePage(
    uiState: KudoUiState,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    onReorderStore: (List<Long>) -> Unit,
    onBuyGesture: (Long) -> Boolean,
    onEdit: (Long) -> Unit,
    listState: LazyListState
) {
    val haptics = rememberKudoHaptics()
    var localStore by remember { mutableStateOf(uiState.data.store) }
    LaunchedEffect(uiState.data.store) {
        localStore = uiState.data.store
    }
    val newTopStoreId = localStore.firstOrNull()?.id?.takeIf { it == uiState.recentStoreInsertId }
    val storeIds = remember(localStore) { localStore.map(KudoStoreItem::id).toSet() }

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            localStore = localStore.toMutableList().apply {
                val fromIndex = indexOfFirst { it.id == from.key }
                val toIndex = indexOfFirst { it.id == to.key }
                if (fromIndex != -1 && toIndex != -1) {
                    add(toIndex, removeAt(fromIndex))
                }
            }
        },
        canDragOver = { draggedOver, dragging ->
            (draggedOver.key as? Long)?.let(storeIds::contains) == true &&
                (dragging.key as? Long)?.let(storeIds::contains) == true
        },
        listState = listState,
        onDragEnd = { _, _ ->
            if (localStore.map(KudoStoreItem::id) != uiState.data.store.map(KudoStoreItem::id)) {
                haptics.vibrate(8)
                onReorderStore(localStore.map(KudoStoreItem::id))
            }
        },
        maxScrollPerFrame = 56.dp
    )

    LazyColumn(
        state = state.listState,
        modifier = modifier
            .reorderable(state)
    ) {
        item {
            SectionHeader(
                title = "Rewards Store",
                palette = palette
            )
            if (uiState.data.store.isEmpty()) {
                EmptyState(text = "Empty", palette = palette)
            }
        }

        items(localStore, key = { it.id }) { item ->
            ReorderableItem(
                state = state,
                key = item.id,
                defaultDraggingModifier = Modifier
            ) { isDragging ->
                if (item.id == newTopStoreId) {
                    StackInsertItem {
                        StoreRow(
                            item = item,
                            coins = uiState.data.coins,
                            palette = palette,
                            isDragging = isDragging,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .detectReorderAfterLongPress(state),
                            swipeEnabled = !isDragging,
                            onBuyGesture = { onBuyGesture(item.id) },
                            onEdit = { onEdit(item.id) }
                        )
                    }
                } else {
                    StoreRow(
                        item = item,
                        coins = uiState.data.coins,
                        palette = palette,
                        isDragging = isDragging,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .detectReorderAfterLongPress(state),
                        swipeEnabled = !isDragging,
                        onBuyGesture = { onBuyGesture(item.id) },
                        onEdit = { onEdit(item.id) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private val HabitGridSpacing = 10.dp
private val HabitGridItemHeight = 76.dp

@Composable
private fun HabitsGrid(
    habits: List<KudoTask>,
    palette: KudoPalette,
    finalMultiplier: Float,
    isJiggleMode: Boolean,
    modifier: Modifier = Modifier,
    onHabitsChange: (List<KudoTask>) -> Unit,
    onDragFinished: () -> Unit,
    onCompleteHabit: (Long) -> Unit,
    onEnterJiggle: () -> Unit,
    onDeleteHabit: (Long) -> Unit
) {
    if (habits.isEmpty()) return

    val density = LocalDensity.current
    val latestHabits by rememberUpdatedState(habits)
    val latestOnHabitsChange by rememberUpdatedState(onHabitsChange)
    val latestOnDragFinished by rememberUpdatedState(onDragFinished)

    var draggingHabitId by remember { mutableStateOf<Long?>(null) }
    var draggingPosition by remember { mutableStateOf(Offset.Zero) }
    var dragChangedOrder by remember { mutableStateOf(false) }

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
                        animationSpec = tween(durationMillis = if (isDragging) 0 else 220),
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
                                                }
                                            },
                                            onDragEnd = {
                                                draggingHabitId = null
                                                if (dragChangedOrder) {
                                                    latestOnDragFinished()
                                                }
                                                dragChangedOrder = false
                                            },
                                            onDragCancel = {
                                                draggingHabitId = null
                                                if (dragChangedOrder) {
                                                    latestOnDragFinished()
                                                }
                                                dragChangedOrder = false
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

private fun <T> moveListItem(
    list: List<T>,
    fromIndex: Int,
    toIndex: Int
): List<T> {
    if (fromIndex == toIndex || fromIndex !in list.indices || toIndex !in list.indices) {
        return list
    }

    return list.toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

@Composable
private fun StackInsertItem(
    content: @Composable () -> Unit
) {
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(120)) + expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = 420f
            )
        ),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
private fun LogPage(
    uiState: KudoUiState,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    onUndo: (Int) -> Unit,
    listState: LazyListState
) {
    if (uiState.data.logs.isEmpty()) {
        Column(modifier = modifier.fillMaxSize()) {
            TrendChart(logs = uiState.data.logs, palette = palette)
            EmptyState(text = "No history", palette = palette)
        }
        return
    }

    val grouped = remember(uiState.data.logs) {
        uiState.data.logs
            .mapIndexed { index, log -> index to log }
            .groupBy { formatDayHeader(it.second.timestamp) }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        item {
            TrendChart(logs = uiState.data.logs, palette = palette)
        }

        grouped.forEach { (day, entries) ->
            item {
                Text(
                    text = day,
                    color = palette.textSub,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            items(
                items = entries,
                key = { (_, log) -> log.timestamp }
            ) { (index, log) ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.dp)) {
                    LogRow(
                        log = log,
                        palette = palette,
                        onUndo = { onUndo(index) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    palette: KudoPalette,
    collapsible: Boolean = false,
    collapsed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (collapsed) -90f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "sectionArrow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = palette.textSub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (collapsible) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = palette.textSub,
                modifier = Modifier.graphicsLayer {
                    rotationZ = arrowRotation
                }
            )
        }
    }
}

@Composable
private fun FocusInboxSwitcher(
    modifier: Modifier = Modifier,
    currentMode: String,
    palette: KudoPalette,
    onSetListMode: (String) -> Unit
) {
    val focusSelected = currentMode == KudoState.LIST_FOCUS
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val thumbOffsetProgress by animateFloatAsState(
        targetValue = if (focusSelected) 0f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "switcherThumb"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.line)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .offset {
                        IntOffset(
                            x = ((trackWidthPx / 2f) * thumbOffsetProgress).roundToInt(),
                            y = 0
                        )
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.card)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (focusSelected) palette.card else Color.Transparent)
                    .clickable {
                        if (!focusSelected) {
                            onSetListMode(KudoState.LIST_FOCUS)
                        }
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Focus",
                    color = if (focusSelected) palette.textMain else palette.textSub,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!focusSelected) palette.card else Color.Transparent)
                    .clickable {
                        if (focusSelected) {
                            onSetListMode(KudoState.LIST_INBOX)
                        }
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Inbox",
                    color = if (!focusSelected) palette.textMain else palette.textSub,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
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

@OptIn(ExperimentalFoundationApi::class)
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

@OptIn(ExperimentalFoundationApi::class)
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
    val jiggleRotation = rememberHabitJiggleRotation(isJiggleMode)
    val completedToday = remember(task.last) { isToday(task.last) }
    val value = remember(task.valAmount, finalMultiplier) {
        (task.valAmount * finalMultiplier).toInt()
    }
    var isCharging by remember(task.id) { mutableStateOf(false) }
    var chargeJob by remember(task.id) { mutableStateOf<Job?>(null) }
    val chargeProgress = remember(task.id) { Animatable(0f) }
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "habitDragScale"
    )
    val dragAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.92f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "habitDragAlpha"
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
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
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
                        size = androidx.compose.ui.geometry.Size(
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

@Composable
private fun TaskRow(
    task: KudoTask,
    palette: KudoPalette,
    finalMultiplier: Float,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onComplete: () -> Unit,
    onMoveGesture: () -> Boolean,
    onEdit: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val reward = (task.valAmount * finalMultiplier).toInt()
    val canMoveTask = task.list != KudoState.LIST_INBOX || task.valAmount > 0
    SwipeActionCard(
        modifier = modifier,
        palette = palette,
        lifted = isDragging,
        positiveAction = SwipeVisualAction(
            color = palette.green,
            icon = Icons.Rounded.Check,
            alignStart = true
        ),
        negativeAction = SwipeVisualAction(
            color = palette.blue,
            icon = Icons.Rounded.MoveDown,
            alignStart = false
        ),
        onPositiveAllowed = { true },
        onNegativeAllowed = { canMoveTask },
        onPositiveCommit = onComplete,
        onNegativeCommit = { onMoveGesture() },
        onPositiveCommitHaptic = haptics::vibrateDoubleTick,
        onNegativeReleaseDurationMs = 280,
        onNegativeDismissScale = 0.95f,
        onNegativeRejected = { onMoveGesture() },
        onTap = onEdit,
        swipeEnabled = swipeEnabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.line, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                color = palette.textMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(14.dp))
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
}

@Composable
private fun StoreRow(
    item: KudoStoreItem,
    coins: Int,
    palette: KudoPalette,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onBuyGesture: () -> Boolean,
    onEdit: () -> Unit
) {
    val canBuy = coins >= item.cost
    SwipeActionCard(
        modifier = modifier,
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
                    text = if (item.type == KudoState.STORE_INFINITE) "Infinite" else "One-time",
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

@Composable
private fun rememberKudoHaptics(): KudoHaptics {
    val context = LocalContext.current
    return remember(context) { KudoHaptics(context) }
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
        targetValue = if (lifted) 1.02f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "swipeLiftScale"
    )
    val liftAlpha by animateFloatAsState(
        targetValue = if (lifted) 0.96f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "swipeLiftAlpha"
    )
    val liftElevation by animateDpAsState(
        targetValue = if (lifted) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 140),
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
                .then(
                    if (swipeEnabled) {
                        Modifier.draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                isDragging = true
                                dismissAlphaTarget = 1f
                                rawOffsetX = (rawOffsetX + delta).coerceIn(-widthPx, widthPx)
                            },
                            onDragStopped = {
                                scope.launch {
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
                .clickable { onTap() },
            colors = CardDefaults.cardColors(containerColor = animatedCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = liftElevation),
            shape = RoundedCornerShape(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun TrendChart(
    logs: List<KudoLogEntry>,
    palette: KudoPalette
) {
    val income = remember(logs) { buildTrend(logs, positive = true) }
    val expense = remember(logs) { buildTrend(logs, positive = false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = "TREND",
                color = palette.textMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val max = maxOf(
                    income.maxOrNull() ?: 0f,
                    expense.maxOrNull() ?: 0f,
                    10f
                )
                if (max <= 0f) return@Canvas

                fun linePath(points: List<Float>): Path {
                    val stepX = if (points.size <= 1) size.width else size.width / (points.size - 1)
                    val offsets = points.mapIndexed { index, value ->
                        Offset(
                            x = stepX * index,
                            y = size.height - (value / max) * size.height
                        )
                    }
                    return Path().apply {
                        if (offsets.isEmpty()) return@apply
                        if (offsets.size == 1) {
                            moveTo(offsets.first().x, offsets.first().y)
                            return@apply
                        }

                        moveTo(offsets.first().x, offsets.first().y)
                        for (index in 1 until offsets.lastIndex) {
                            val current = offsets[index]
                            val next = offsets[index + 1]
                            val midPoint = Offset(
                                x = (current.x + next.x) / 2f,
                                y = (current.y + next.y) / 2f
                            )
                            quadraticBezierTo(current.x, current.y, midPoint.x, midPoint.y)
                        }
                        val lastControl = offsets[offsets.lastIndex - 1]
                        val lastPoint = offsets.last()
                        quadraticBezierTo(
                            lastControl.x,
                            lastControl.y,
                            lastPoint.x,
                            lastPoint.y
                        )
                    }
                }

                drawLine(
                    color = palette.line,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawPath(
                    path = linePath(income),
                    color = palette.green,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = linePath(expense),
                    color = palette.orange,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun LogRow(
    log: KudoLogEntry,
    palette: KudoPalette,
    onUndo: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val valueColor = if (log.type == "store") palette.orange else palette.green
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.card)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.text,
                color = palette.textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTime(log.timestamp),
                color = palette.textSub,
                fontSize = 11.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (log.value > 0) "+${log.value}" else log.value.toString(),
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    haptics.vibrate(HapticTickMs)
                    onUndo()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Undo",
                    tint = palette.textSub
                )
            }
        }
    }
}

@Composable
private fun BottomTabs(
    uiState: KudoUiState,
    palette: KudoPalette,
    onViewSelected: (String) -> Unit
) {
    Surface(
        color = palette.card,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BottomTabItem(
                modifier = Modifier.weight(1f),
                selected = uiState.currentView == KudoViewModel.VIEW_TASKS,
                label = "Tasks",
                icon = Icons.Rounded.CheckCircle,
                selectedColor = palette.green,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoViewModel.VIEW_TASKS) }
            )
            BottomTabItem(
                modifier = Modifier.weight(1f),
                selected = uiState.currentView == KudoViewModel.VIEW_STORE,
                label = "Store",
                icon = Icons.Rounded.CardGiftcard,
                selectedColor = palette.orange,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoViewModel.VIEW_STORE) }
            )
            BottomTabItem(
                modifier = Modifier.weight(1f),
                selected = uiState.currentView == KudoViewModel.VIEW_LOG,
                label = "Kudo",
                icon = Icons.Rounded.BarChart,
                selectedColor = palette.textMain,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoViewModel.VIEW_LOG) }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (selected) selectedColor else mutedColor,
        label = "tabColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) selectedColor.copy(alpha = 0.12f) else Color.Transparent,
        label = "tabBackground"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (!selected) {
                    onClick()
                }
            }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    uiState: KudoUiState,
    palette: KudoPalette,
    isFileActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onToggleHelp: () -> Unit,
    onSetTheme: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val ratio = remember(uiState.data.logs) {
        var income = 0
        var expense = 0
        uiState.data.logs.forEach { log ->
            if (log.value > 0) {
                income += log.value
            } else if (log.value < 0) {
                expense += -log.value
            }
        }
        when {
            expense == 0 && income > 0 -> "∞"
            expense == 0 -> "0.00"
            else -> String.format(Locale.US, "%.2f", income.toFloat() / expense.toFloat())
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.card,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isHelpVisible) "Help" else "Settings",
                    color = palette.textMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onToggleHelp) {
                    Icon(
                        imageVector = if (uiState.isHelpVisible) {
                            Icons.Rounded.ArrowBack
                        } else {
                            Icons.Rounded.HelpOutline
                        },
                        contentDescription = if (uiState.isHelpVisible) {
                            "Back to settings"
                        } else {
                            "Open help"
                        },
                        tint = palette.textSub
                    )
                }
            }

            if (!uiState.isHelpVisible) {
                StatGrid(
                    palette = palette,
                    maxCoins = uiState.data.maxCoins,
                    ratio = ratio
                )

                Text(
                    text = "Theme",
                    color = palette.textMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeModeChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.BrightnessAuto,
                        contentDescription = "Follow system theme",
                        selected = uiState.theme == KudoStateRepository.THEME_SYSTEM,
                        palette = palette,
                        onClick = { onSetTheme(KudoStateRepository.THEME_SYSTEM) }
                    )
                    ThemeModeChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LightMode,
                        contentDescription = "Light theme",
                        selected = uiState.theme == KudoStateRepository.THEME_LIGHT,
                        palette = palette,
                        onClick = { onSetTheme(KudoStateRepository.THEME_LIGHT) }
                    )
                    ThemeModeChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.DarkMode,
                        contentDescription = "Dark theme",
                        selected = uiState.theme == KudoStateRepository.THEME_DARK,
                        palette = palette,
                        onClick = { onSetTheme(KudoStateRepository.THEME_DARK) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ActionRow(
                    title = "Backup Data",
                    palette = palette,
                    icon = Icons.Rounded.Download,
                    enabled = !isFileActionInProgress,
                    onClick = onExport
                )
                ActionRow(
                    title = "Restore Data",
                    palette = palette,
                    icon = Icons.Rounded.Upload,
                    enabled = !isFileActionInProgress,
                    onClick = onImport
                )
                if (isFileActionInProgress) {
                    Text(
                        text = "Processing file in the system file manager…",
                        color = palette.textSub,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            } else {
                HelpContent(palette = palette)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textColor: Color,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textAlign: TextAlign = TextAlign.Start
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle.Default.copy(
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = textAlign
        ),
        cursorBrush = SolidColor(textColor),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun StatGrid(
    palette: KudoPalette,
    maxCoins: Int,
    ratio: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Lifetime High",
            value = maxCoins.toString(),
            subtitle = "MAX COINS",
            palette = palette,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Income / Expense",
            value = ratio,
            subtitle = "RATIO",
            palette = palette,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    palette: KudoPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title, color = palette.textSub, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, color = palette.textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = subtitle, color = palette.green, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThemeModeChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    palette: KudoPalette,
    onClick: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) palette.card else palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
            .clickable {
                if (!selected) {
                    haptics.vibrate(HapticTickMs)
                    onClick()
                }
            }
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) palette.textMain else palette.textSub,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    palette: KudoPalette,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val contentColor = if (enabled) palette.textMain else palette.textSub
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) {
                haptics.vibrate(HapticTickMs)
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = contentColor)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HelpContent(
    palette: KudoPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "💡 Kudo 使用指南",
            color = palette.textMain,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "奖励驱动的效率工具：赚取金币，兑换自我奖励。",
            color = palette.textSub,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(text = "📌 任务分类", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "Habits：重复进行的日常项。\nTasks：一次性事项，分为 Focus 与 Inbox。",
            color = palette.textSub,
            fontSize = 13.sp
        )
        Text(text = "🖐️ 手势操作", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "顶部：在页面顶部下划展开添加面板，提交后会自动收起。\nHabits：点击充能，长按进入编辑 / 排序 / 删除模式。\nTasks：右滑完成，左滑流转，点击编辑，长按排序。\nStore：滑动消费金币，点击编辑奖励内容，长按排序。",
            color = palette.textSub,
            fontSize = 13.sp
        )
        Text(text = "💾 数据安全", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "备份：通过系统文件管理器导出 JSON 文件。\n恢复：通过系统文件管理器导入备份文件并覆盖当前数据。",
            color = palette.textSub,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSheet(
    uiState: KudoUiState,
    palette: KudoPalette,
    target: EditingTarget,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val targetTask = uiState.data.tasks.firstOrNull { it.id == target.id }
    val targetStore = uiState.data.store.firstOrNull { it.id == target.id }
    val isPendingMove = uiState.pendingMoveTaskId != null

    var title by remember(target.id, isPendingMove) {
        mutableStateOf(targetTask?.title ?: targetStore?.title.orEmpty())
    }
    var value by remember(target.id, isPendingMove) {
        mutableStateOf(
            when {
                isPendingMove -> ""
                targetTask != null -> targetTask.valAmount.toString()
                targetStore != null -> targetStore.cost.toString()
                else -> ""
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isPendingMove) "Set Value & Move" else "Edit Item",
                color = palette.textMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title", color = palette.textSub) },
                colors = textFieldColors(palette),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = value,
                onValueChange = { value = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isPendingMove) "Set value for Focus" else "0",
                        color = palette.textSub
                    )
                },
                colors = textFieldColors(palette),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.background)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete",
                        tint = palette.orange
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Delete", color = palette.orange)
                }
                TextButton(
                    onClick = { onSave(title, value) },
                    modifier = Modifier
                        .weight(2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.textMain)
                ) {
                    Text(text = "Save", color = palette.background)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmptyState(
    text: String,
    palette: KudoPalette
) {
    Text(
        text = text,
        color = palette.textSub,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun textFieldColors(palette: KudoPalette) = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = palette.textMain,
    focusedTextColor = palette.textMain,
    unfocusedTextColor = palette.textMain
)

private fun xpProgress(state: KudoState): Float {
    val level = state.level
    val base = 100 * (level - 1) * (level - 1)
    val next = 100 * level * level
    return ((state.life - base).toFloat() / (next - base).toFloat()).coerceIn(0f, 1f)
}

private fun formatMultiplier(multiplier: Float): String {
    return String.format(Locale.US, "%.2f", multiplier)
}

private fun lerpFloat(start: Float, stop: Float, progress: Float): Float {
    return start + (stop - start) * progress.coerceIn(0f, 1f)
}

private fun formatTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .format(TimeFormatter)
}

private fun formatDayHeader(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .format(DayHeaderFormatter)
}

private fun isToday(timestamp: Long): Boolean {
    if (timestamp == 0L) return false
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .toLocalDate() == LocalDate.now(AppZoneId)
}

private fun buildTrend(logs: List<KudoLogEntry>, positive: Boolean): List<Float> {
    val today = LocalDate.now(AppZoneId)
    val windowStart = today.minusDays(13)
    val buckets = FloatArray(14)

    logs.forEach { log ->
        val amount = when {
            positive && log.value > 0 -> log.value.toFloat()
            !positive && log.value < 0 -> (-log.value).toFloat()
            else -> return@forEach
        }
        val logDate = Instant.ofEpochMilli(log.timestamp)
            .atZone(AppZoneId)
            .toLocalDate()
        if (logDate.isBefore(windowStart) || logDate.isAfter(today)) {
            return@forEach
        }

        val index = (logDate.toEpochDay() - windowStart.toEpochDay()).toInt()
        buckets[index] += amount
    }

    return buckets.toList()
}

private val AppZoneId: ZoneId = ZoneId.systemDefault()
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
private val DayHeaderFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
