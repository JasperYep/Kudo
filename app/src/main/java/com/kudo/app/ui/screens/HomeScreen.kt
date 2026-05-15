@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.kudo.app.ui.screens

import android.app.DatePickerDialog
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.platform.LocalViewConfiguration
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.kudo.app.core.platform.KudoHaptics
import com.kudo.app.core.model.KudoHabit
import com.kudo.app.core.model.KudoLogEntry
import com.kudo.app.core.model.KudoLogKind
import com.kudo.app.core.model.KudoState
import com.kudo.app.core.model.KudoStoreItem
import com.kudo.app.core.model.KudoStoreKind
import com.kudo.app.core.model.KudoSubtask
import com.kudo.app.core.model.KudoSubtaskDraft
import com.kudo.app.core.model.KudoTask
import com.kudo.app.core.model.KudoTaskKind
import com.kudo.app.core.model.KudoTaskSortMode
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
import com.kudo.app.ui.viewmodel.KudoView
import com.kudo.app.ui.viewmodel.KudoViewModel
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
    var isTaskImportDialogVisible by rememberSaveable { mutableStateOf(false) }
    var taskImportText by rememberSaveable { mutableStateOf("") }
    var isFileTransferInProgress by remember { mutableStateOf(false) }
    var activeComposerRevealProgress by remember { mutableFloatStateOf(0f) }
    val tasksListState = rememberLazyListState()
    val storeListState = rememberLazyListState()
    val logListState = rememberLazyListState()
    LaunchedEffect(uiState.view.currentView) {
        activeComposerRevealProgress = 0f
    }
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
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    var notificationPermissionPrompted by rememberSaveable { mutableStateOf(false) }
    var exactAlarmPrompted by rememberSaveable { mutableStateOf(false) }
    val hasFutureReminders by remember(uiState.data.tasks) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            uiState.data.tasks.any { task ->
                (task.dueAtEpochMillis ?: 0L) > now
            }
        }
    }

    LaunchedEffect(hasFutureReminders) {
        if (!hasFutureReminders) {
            notificationPermissionPrompted = false
            exactAlarmPrompted = false
            return@LaunchedEffect
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPermissionPrompted &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionPrompted = true
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !exactAlarmPrompted &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            exactAlarmPrompted = true
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
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
                onOpenSettings = viewModel::openSettings
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.view.currentView) {
                    KudoView.Tasks -> PullComposerPage(
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
                    ) { modifier, pageScrollEnabled, onGestureLockChange ->
                        TasksPage(
                            uiState = uiState,
                            palette = palette,
                            modifier = modifier,
                            userScrollEnabled = pageScrollEnabled,
                            onGestureLockChange = onGestureLockChange,
                            onToggleHabits = viewModel::toggleHabitsCollapsed,
                            onResetTaskSortMode = viewModel::resetTaskSortMode,
                            onExitHabitJiggle = viewModel::exitHabitJiggleMode,
                            onEnterHabitJiggle = viewModel::enterHabitJiggleMode,
                            onDeleteHabit = viewModel::deleteHabitItem,
                            onReorderTask = viewModel::reorderTasks,
                            onReorderHabits = viewModel::reorderHabits,
                            onCompleteTask = viewModel::completeTask,
                            onCompleteSubtask = viewModel::completeSubtask,
                            onEditTask = viewModel::openEditTask,
                            onCompleteHabit = viewModel::completeHabit,
                            listState = tasksListState
                        )
                    }

                    KudoView.Store -> PullComposerPage(
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
                    ) { modifier, pageScrollEnabled, onGestureLockChange ->
                        StorePage(
                            uiState = uiState,
                            palette = palette,
                            modifier = modifier,
                            userScrollEnabled = pageScrollEnabled,
                            onGestureLockChange = onGestureLockChange,
                            onReorderStore = viewModel::reorderStore,
                            onBuyGesture = viewModel::purchaseItemFromGesture,
                            onEdit = viewModel::openEditStore,
                            listState = storeListState
                        )
                    }

                    KudoView.Log -> LogPage(
                        uiState = uiState,
                        palette = palette,
                        modifier = Modifier.fillMaxSize(),
                        onUndo = viewModel::undoLog,
                        onLongPressTrend = viewModel::openNotebook,
                        listState = logListState
                    )
                }

                UndoBanner(
                    visible = uiState.view.showUndoBanner,
                    latestLog = uiState.data.logs.firstOrNull(),
                    palette = palette,
                    onUndo = { viewModel.undoLog(0) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp)
                )
            }
        }
    }

    if (uiState.view.isNotebookVisible) {
        NotebookOverlay(
            uiState = uiState,
            palette = palette,
            onCreate = viewModel::addNotebookNote,
            onDelete = viewModel::deleteNotebookNote,
            onSelect = viewModel::selectNotebookNote,
            onTitleChange = viewModel::updateNotebookNoteTitle,
            onContentChange = viewModel::updateNotebookNoteContent,
            onClose = viewModel::closeNotebook
        )
    }

    if (uiState.view.settingsVisible) {
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
            },
            onImportTasks = {
                isTaskImportDialogVisible = true
            }
        )
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Restore Backup") },
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

    if (isTaskImportDialogVisible) {
        AlertDialog(
            onDismissRequest = { isTaskImportDialogVisible = false },
            title = { Text("Import Tasks") },
            text = {
                Column {
                    Text(
                        text = "Paste one task per line. A trailing number becomes the value.",
                        color = palette.textSub.copy(alpha = 0.72f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = taskImportText,
                        onValueChange = { taskImportText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = {
                            Text(
                                text = "- Draft project outline 20\n- Review weekly notes 15\n- Plan next milestone",
                                color = palette.textSub.copy(alpha = 0.42f)
                            )
                        },
                        colors = textFieldColors(palette),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importTasksFromText(taskImportText) { count ->
                            if (count > 0) {
                                Toast.makeText(context, "Imported $count tasks", Toast.LENGTH_SHORT).show()
                                taskImportText = ""
                                isTaskImportDialogVisible = false
                                viewModel.closeSettings()
                            } else {
                                Toast.makeText(context, "No valid tasks found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { isTaskImportDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val editTarget = uiState.view.editingTarget

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

private val HeaderHeight = 80.dp

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
    content: @Composable (Modifier, Boolean, (Boolean) -> Unit) -> Unit
) {
    var composerMeasuredHeightPx by remember { mutableFloatStateOf(1f) }
    var composerOpenHapticArmed by remember { mutableStateOf(true) }
    var composerOpenHapticPending by remember { mutableStateOf(false) }
    var composerAnimationTargetPx by remember { mutableFloatStateOf(0f) }
    var composerAnimationJob by remember { mutableStateOf<Job?>(null) }
    var childGestureLocked by remember { mutableStateOf(false) }

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
    val scope = rememberCoroutineScope()
    val estimatedComposerHeightPx = with(density) { 120.dp.toPx() }
    val blankTapSlopPx = with(density) { 8.dp.toPx() }
    val openTriggerDistancePx = with(density) { 72.dp.toPx() }
    val closeTriggerDistancePx = with(density) { 18.dp.toPx() }
    val reverseInterruptDistancePx = with(density) { 8.dp.toPx() }
    val composerMaxHeightPx = composerMeasuredHeightPx.takeIf { it > 1f } ?: estimatedComposerHeightPx
    val composerClosedRearmProgress = 0.12f
    val composerVisuallyExpandedProgress = 0.995f
    val composerVisuallyExpandedPx = composerMaxHeightPx * composerVisuallyExpandedProgress
    val composerRevealPx = remember { Animatable(0f) }
    val composerRevealProgress = if (composerMeasuredHeightPx > 0f) {
        (composerRevealPx.value / composerMeasuredHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val pageScrollEnabled =
        !childGestureLocked &&
        composerAnimationTargetPx <= 1f &&
            composerRevealProgress <= composerClosedRearmProgress

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

    fun animateComposerTo(targetPx: Float) {
        val resolvedTarget = targetPx.coerceIn(0f, composerMaxHeightPx)
        composerAnimationTargetPx = resolvedTarget
        composerAnimationJob?.cancel()
        composerAnimationJob = scope.launch {
            composerRevealPx.stop()
            composerRevealPx.animateTo(
                targetValue = resolvedTarget,
                animationSpec = if (resolvedTarget > composerRevealPx.value) {
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
                }
            )
        }
    }

    fun openComposer(currentReveal: Float = composerRevealPx.value) {
        if (
            composerAnimationTargetPx >= composerMaxHeightPx - 0.5f &&
            composerRevealPx.value >= composerMaxHeightPx - 0.5f
        ) {
            return
        }
        animateComposerTo(composerMaxHeightPx)
        armComposerOpenHapticIfNeeded(
            currentReveal = currentReveal,
            shouldExpand = true
        )
    }

    fun collapseComposer() {
        composerOpenHapticPending = false
        animateComposerTo(0f)
    }

    LaunchedEffect(composerRevealProgress) {
        onRevealProgressChanged(composerRevealProgress)
    }
    DisposableEffect(Unit) {
        onDispose { onRevealProgressChanged(0f) }
    }
    LaunchedEffect(
        composerRevealProgress,
        composerAnimationTargetPx,
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
                composerAnimationTargetPx >= composerVisuallyExpandedPx &&
                composerRevealProgress >= composerVisuallyExpandedProgress
        if (shouldTriggerOpenHaptic) {
            triggerComposerOpenHaptic()
        }
    }
    LaunchedEffect(composerMaxHeightPx) {
        val clampedCurrent = composerRevealPx.value.coerceIn(0f, composerMaxHeightPx)
        if (clampedCurrent != composerRevealPx.value) {
            composerRevealPx.snapTo(clampedCurrent)
        }
        composerAnimationTargetPx = composerAnimationTargetPx.coerceIn(0f, composerMaxHeightPx)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .pointerInput(
                isListAtTop,
                composerRevealProgress,
                composerAnimationTargetPx,
                childGestureLocked
            ) {
                if (childGestureLocked) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val startRevealProgress = composerRevealProgress
                    val canOpenThisGesture = isListAtTop && startRevealProgress <= 0.02f
                    val canCloseThisGesture =
                        composerAnimationTargetPx > 1f ||
                            startRevealProgress > composerClosedRearmProgress
                    var pointerId = down.id
                    var totalDx = 0f
                    var totalDy = 0f
                    var gestureLockedToComposer = false
                    var openTriggered = false
                    var openPeakDy = 0f

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == pointerId }
                            ?: event.changes.firstOrNull()
                            ?: break
                        pointerId = change.id

                        val delta = change.position - change.previousPosition
                        totalDx += delta.x
                        totalDy += delta.y

                        val verticalDistance = kotlin.math.abs(totalDy)
                        val horizontalDistance = kotlin.math.abs(totalDx)
                        val isVerticalGesture =
                            verticalDistance > blankTapSlopPx &&
                                verticalDistance > horizontalDistance

                        if (openTriggered) {
                            openPeakDy = maxOf(openPeakDy, totalDy)
                            change.consume()
                            if (openPeakDy - totalDy >= reverseInterruptDistancePx) {
                                collapseComposer()
                                gestureLockedToComposer = true
                                openTriggered = false
                            }
                        } else if (isVerticalGesture && canCloseThisGesture && totalDy < 0f) {
                            gestureLockedToComposer = true
                            change.consume()
                            if (kotlin.math.abs(totalDy) >= closeTriggerDistancePx) {
                                collapseComposer()
                            }
                        } else if (
                            isVerticalGesture &&
                            canOpenThisGesture &&
                            totalDy >= openTriggerDistancePx
                        ) {
                            openComposer()
                            openTriggered = true
                            openPeakDy = totalDy
                            gestureLockedToComposer = true
                            change.consume()
                        } else if (gestureLockedToComposer) {
                            change.consume()
                        }

                        if (change.changedToUpIgnoreConsumed()) {
                            if (openTriggered) {
                                openComposer()
                            }
                            break
                        }
                    }
                }
            }
            .pointerInput(composerRevealProgress, blankTapSlopPx, childGestureLocked) {
                if (childGestureLocked || composerRevealProgress < 0.98f) return@pointerInput
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
                content(
                    Modifier.fillMaxSize(),
                    pageScrollEnabled
                ) { locked ->
                    childGestureLocked = locked
                }

                val hintAlpha = if (isListAtTop && composerRevealProgress <= 0.02f) 0.35f else 0f
                Text(
                    text = "swipe down to add",
                    color = palette.textSub.copy(alpha = hintAlpha),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Header(
    state: KudoState,
    palette: KudoPalette,
    onOpenSettings: () -> Unit
) {
    val animatedCoins by animateIntAsState(targetValue = state.coins, label = "coins")

    Surface(
        color = palette.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MULT",
                    color = palette.textSub,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatMultiplier(state.multiplier)}x",
                    color = palette.textSub,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, palette.line, RoundedCornerShape(6.dp))
                        .background(palette.card)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${'$'}",
                    color = palette.green,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = animatedCoins.toString(),
                    color = palette.textMain,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
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
        uiState.view.currentView == KudoView.Store -> "Add to Store..."
        uiState.view.taskCreationTarget == KudoTaskKind.Habit -> "Add Habit..."
        else -> "Add Task..."
    }
    val valuePlaceholder = when {
        uiState.view.currentView == KudoView.Store -> "0"
        uiState.view.taskCreationTarget == KudoTaskKind.Habit -> "10"
        else -> "" // Suggests it is completely optional
    }
    val modeText = when {
        uiState.view.currentView == KudoView.Store && uiState.view.storeMode == KudoStoreKind.Repeatable -> "INFIN"
        uiState.view.currentView == KudoView.Store -> "ONCE"
        uiState.view.taskCreationTarget == KudoTaskKind.Habit -> "HABIT"
        else -> "TASK"
    }
    val modeColor = when {
        uiState.view.currentView == KudoView.Store -> palette.orange
        uiState.view.taskCreationTarget == KudoTaskKind.Habit -> palette.gold
        else -> palette.green
    }
    val valueColor = when {
        uiState.view.currentView == KudoView.Store -> palette.orange
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

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptics.vibrate(HapticTickMs)
                            onModeToggle()
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modeText,
                        color = modeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = modeColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

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
    userScrollEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit,
    onToggleHabits: () -> Unit,
    onResetTaskSortMode: () -> Unit,
    onExitHabitJiggle: () -> Unit,
    onEnterHabitJiggle: () -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onReorderTask: (List<Long>) -> Unit,
    onReorderHabits: (List<Long>) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onCompleteSubtask: (Long, Long) -> Unit,
    onEditTask: (Long) -> Unit,
    onCompleteHabit: (Long) -> Unit,
    listState: LazyListState
) {
    val haptics = rememberKudoHaptics()
    var isHabitGestureLocked by remember { mutableStateOf(false) }
    var isTaskSwipeGestureLocked by remember { mutableStateOf(false) }
    var isTaskLongPressGestureLocked by remember { mutableStateOf(false) }
    val habits = uiState.data.habits
    var localHabits by remember { mutableStateOf(habits) }
    val currentSortMode = uiState.data.taskSortMode
    val tasks = remember(uiState.data.tasks, currentSortMode) {
        sortTasksForDisplay(
            tasks = uiState.data.tasks,
            sortMode = currentSortMode
        )
    }

    var localTasks by remember { mutableStateOf(tasks) }
    var lastTaskSwapHapticAtMs by remember { mutableStateOf(0L) }
    val newTopTaskId = localTasks.lastOrNull()?.id?.takeIf { it == uiState.view.recentTaskInsertId }
    var expandedTaskIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    LaunchedEffect(tasks.map(KudoTask::id)) {
        expandedTaskIds = expandedTaskIds.filter { expandedId ->
            tasks.any { it.id == expandedId && it.hasSubtasks }
        }
    }

    val localTaskIds = remember(localTasks) { localTasks.map(KudoTask::id).toSet() }
    val taskOrderIds = remember(tasks) { tasks.map(KudoTask::id) }
    val localTaskOrderIds = remember(localTasks) { localTasks.map(KudoTask::id) }
    val latestLocalTasks by rememberUpdatedState(localTasks)
    val latestLocalTaskIds by rememberUpdatedState(localTaskIds)
    val latestTaskOrderIds by rememberUpdatedState(taskOrderIds)
    val latestLocalTaskOrderIds by rememberUpdatedState(localTaskOrderIds)
    val latestOnReorderTask by rememberUpdatedState(onReorderTask)
    var pendingDeleteHabitId by remember { mutableStateOf<Long?>(null) }
    val dragCancelledAnimation = remember {
        SpringDragCancelledAnimation(stiffness = ReorderCancelAnimationStiffness)
    }
    val state = key(listState) {
        rememberReorderableLazyListState(
            onMove = { from, to ->
                val fromId = from.key as? Long
                val toId = to.key as? Long
                if (fromId != null && toId != null) {
                    val currentTasks = latestLocalTasks
                    val fromIndex = currentTasks.indexOfFirst { it.id == fromId }
                    val toIndex = currentTasks.indexOfFirst { it.id == toId }
                    val movedTasks = moveListItem(currentTasks, fromIndex, toIndex)
                    if (movedTasks !== currentTasks) {
                        localTasks = movedTasks
                        lastTaskSwapHapticAtMs = maybeTriggerReorderSwapHaptic(
                            haptics = haptics,
                            lastHapticAtMs = lastTaskSwapHapticAtMs
                        )
                    }
                }
            },
            canDragOver = { draggedOver, dragging ->
                (draggedOver.key as? Long)?.let(latestLocalTaskIds::contains) == true &&
                    (dragging.key as? Long)?.let(latestLocalTaskIds::contains) == true
            },
            listState = listState,
            onDragEnd = { _, _ ->
                lastTaskSwapHapticAtMs = 0L
                if (latestLocalTaskOrderIds != latestTaskOrderIds) {
                    haptics.vibrate(HapticConfirmMs)
                    latestOnReorderTask(latestLocalTaskOrderIds)
                }
            },
            maxScrollPerFrame = ReorderAutoScrollMax,
            dragCancelledAnimation = dragCancelledAnimation
        )
    }
    val isTaskReordering by remember(state) {
        derivedStateOf { state.draggingItemIndex != null }
    }
    val isListGestureLocked =
        isTaskReordering ||
            isHabitGestureLocked ||
            isTaskSwipeGestureLocked ||
            isTaskLongPressGestureLocked

    LaunchedEffect(habits) {
        if (!isHabitGestureLocked) {
            localHabits = habits
        }
    }
    LaunchedEffect(tasks) {
        if (!isTaskReordering) {
            localTasks = tasks
        }
    }

    LaunchedEffect(isListGestureLocked) {
        onGestureLockChange(isListGestureLocked)
    }
    LaunchedEffect(uiState.view.habitJiggleMode) {
        if (!uiState.view.habitJiggleMode) {
            isHabitGestureLocked = false
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            isHabitGestureLocked = false
            isTaskSwipeGestureLocked = false
            isTaskLongPressGestureLocked = false
            onGestureLockChange(false)
        }
    }

    LazyColumn(
        state = state.listState,
        userScrollEnabled = userScrollEnabled && !isListGestureLocked,
        modifier = modifier
            .reorderable(state)
    ) {
        if (habits.isNotEmpty()) {
            item(key = "habits_header") {
                SectionHeader(
                    title = "Habits",
                    palette = palette,
                    collapsible = true,
                    collapsed = uiState.view.habitsCollapsed,
                    onClick = {
                        onExitHabitJiggle()
                        onToggleHabits()
                    }
                )
            }
            item(key = "habits_grid") {
                AnimatedVisibility(
                    visible = !uiState.view.habitsCollapsed,
                    modifier = Modifier.clipToBounds(),
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = 0.86f,
                            stiffness = 520f
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(
                        animationSpec = tween(durationMillis = 180)
                    )
                ) {
                    HabitsGrid(
                        habits = localHabits,
                        palette = palette,
                        finalMultiplier = uiState.data.multiplier,
                        isJiggleMode = uiState.view.habitJiggleMode,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onGestureLockChange = { isHabitGestureLocked = it },
                        onHabitsChange = { localHabits = it },
                        onDragFinished = {
                            if (localHabits.map(KudoHabit::id) != habits.map(KudoHabit::id)) {
                                onReorderHabits(localHabits.map(KudoHabit::id))
                            }
                        },
                        onCompleteHabit = onCompleteHabit,
                        onEnterJiggle = onEnterHabitJiggle,
                        onDeleteHabit = { pendingDeleteHabitId = it }
                    )
                }
            }
        }

        item(key = "tasks_header") {
            TaskQueueHeader(
                sortMode = uiState.data.taskSortMode,
                palette = palette,
                onLongPress = {
                    onExitHabitJiggle()
                    onResetTaskSortMode()
                }
            )
        }

        if (tasks.isEmpty()) {
            item(key = "empty_tasks") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (uiState.view.habitJiggleMode) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onExitHabitJiggle() }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    EmptyState(text = "No tasks", palette = palette)
                }
            }
        } else {
            items(localTasks, key = { it.id }) { task ->
                val isActive = localTasks.firstOrNull()?.id == task.id
                ReorderableItem(
                    state = state,
                    key = task.id,
                    defaultDraggingModifier = Modifier
                ) { isDragging ->
                    AnimatedInsertedItem(animate = task.id == newTopTaskId) {
                        TaskRow(
                            task = task,
                            palette = palette,
                            finalMultiplier = uiState.data.multiplier,
                            isActive = isActive,
                            isDragging = isDragging,
                            expanded = task.id in expandedTaskIds,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .lockParentGestureOnLongPress {
                                    isTaskLongPressGestureLocked = it
                                }
                                .reorderLongPressFeedback(
                                    enabled = true,
                                    haptics = haptics
                                )
                                .detectReorderAfterLongPress(state),
                            swipeEnabled = isActive && !isDragging && !isTaskLongPressGestureLocked,
                            onGestureLockChange = { isTaskSwipeGestureLocked = it },
                            onComplete = {
                                onExitHabitJiggle()
                                onCompleteTask(task.id)
                            },
                            onCompleteSubtask = { subtaskId ->
                                onExitHabitJiggle()
                                onCompleteSubtask(task.id, subtaskId)
                            },
                            onToggleExpanded = {
                                expandedTaskIds = toggleId(expandedTaskIds, task.id)
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

@Composable
private fun StorePage(
    uiState: KudoUiState,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit,
    onReorderStore: (List<Long>) -> Unit,
    onBuyGesture: (Long) -> Boolean,
    onEdit: (Long) -> Unit,
    listState: LazyListState
) {
    val haptics = rememberKudoHaptics()
    var localStore by remember { mutableStateOf(uiState.data.store) }
    var isStoreItemGestureLocked by remember { mutableStateOf(false) }
    val storeOrderIds = remember(uiState.data.store) { uiState.data.store.map(KudoStoreItem::id) }
    val localStoreOrderIds = remember(localStore) { localStore.map(KudoStoreItem::id) }
    val newTopStoreId = localStore.firstOrNull()?.id?.takeIf { it == uiState.view.recentStoreInsertId }
    val storeIds = remember(localStore) { localStore.map(KudoStoreItem::id).toSet() }
    val latestLocalStore by rememberUpdatedState(localStore)
    val latestStoreOrderIds by rememberUpdatedState(storeOrderIds)
    val latestLocalStoreOrderIds by rememberUpdatedState(localStoreOrderIds)
    val latestStoreIds by rememberUpdatedState(storeIds)
    val latestOnReorderStore by rememberUpdatedState(onReorderStore)
    var lastStoreSwapHapticAtMs by remember { mutableStateOf(0L) }
    val dragCancelledAnimation = remember {
        SpringDragCancelledAnimation(stiffness = ReorderCancelAnimationStiffness)
    }

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            val fromId = from.key as? Long
            val toId = to.key as? Long
            if (fromId != null && toId != null) {
                val currentStore = latestLocalStore
                val fromIndex = currentStore.indexOfFirst { it.id == fromId }
                val toIndex = currentStore.indexOfFirst { it.id == toId }
                val movedStore = moveListItem(currentStore, fromIndex, toIndex)
                if (movedStore !== currentStore) {
                    localStore = movedStore
                    lastStoreSwapHapticAtMs = maybeTriggerReorderSwapHaptic(
                        haptics = haptics,
                        lastHapticAtMs = lastStoreSwapHapticAtMs
                    )
                }
            }
        },
        canDragOver = { draggedOver, dragging ->
            (draggedOver.key as? Long)?.let(latestStoreIds::contains) == true &&
                (dragging.key as? Long)?.let(latestStoreIds::contains) == true
        },
        listState = listState,
        onDragEnd = { _, _ ->
            lastStoreSwapHapticAtMs = 0L
            if (latestLocalStoreOrderIds != latestStoreOrderIds) {
                haptics.vibrate(HapticConfirmMs)
                latestOnReorderStore(latestLocalStoreOrderIds)
            }
        },
        maxScrollPerFrame = ReorderAutoScrollMax,
        dragCancelledAnimation = dragCancelledAnimation
    )
    val isStoreReordering by remember(state) {
        derivedStateOf { state.draggingItemIndex != null }
    }
    val isStoreGestureLocked = isStoreReordering || isStoreItemGestureLocked

    LaunchedEffect(uiState.data.store) {
        if (!isStoreReordering) {
            localStore = uiState.data.store
        }
    }
    LaunchedEffect(isStoreGestureLocked) {
        onGestureLockChange(isStoreGestureLocked)
    }
    DisposableEffect(Unit) {
        onDispose {
            isStoreItemGestureLocked = false
            onGestureLockChange(false)
        }
    }

    LazyColumn(
        state = state.listState,
        userScrollEnabled = userScrollEnabled && !isStoreGestureLocked,
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
                AnimatedInsertedItem(animate = item.id == newTopStoreId) {
                    StoreRow(
                        item = item,
                        coins = uiState.data.coins,
                        palette = palette,
                        isDragging = isDragging,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .lockParentGestureOnLongPress {
                                isStoreItemGestureLocked = it
                            }
                            .reorderLongPressFeedback(
                                enabled = true,
                                haptics = haptics
                            )
                            .detectReorderAfterLongPress(state),
                        swipeEnabled = !isDragging,
                        onGestureLockChange = { isStoreItemGestureLocked = it },
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


internal fun <T> moveListItem(
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

private fun Modifier.reorderLongPressFeedback(
    enabled: Boolean,
    haptics: KudoHaptics
): Modifier {
    if (!enabled) return this

    return pointerInput(haptics) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial
            )
            val longPress = awaitLongPressOrCancellation(down.id)
            if (longPress == null) {
                return@awaitEachGesture
            }

            haptics.vibrate(HapticTickMs)
        }
    }
}

private fun Modifier.lockParentGestureOnLongPress(
    onLockChange: (Boolean) -> Unit
): Modifier {
    return pointerInput(onLockChange) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial
            )
            val longPress = awaitLongPressOrCancellation(down.id)
            if (longPress == null) {
                onLockChange(false)
                return@awaitEachGesture
            }

            onLockChange(true)
            try {
                var hasPressedPointers = true
                while (hasPressedPointers) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Final)
                    hasPressedPointers = event.changes.any { it.pressed }
                }
            } finally {
                onLockChange(false)
            }
        }
    }
}

internal fun maybeTriggerReorderSwapHaptic(
    haptics: KudoHaptics,
    lastHapticAtMs: Long,
    nowMs: Long = SystemClock.elapsedRealtime()
): Long {
    if (nowMs - lastHapticAtMs < ReorderSwapHapticCooldownMs) {
        return lastHapticAtMs
    }

    haptics.vibrate(ReorderSwapHapticMs)
    return nowMs
}

private fun toggleId(ids: List<Long>, targetId: Long): List<Long> {
    return if (targetId in ids) {
        ids - targetId
    } else {
        ids + targetId
    }
}

@Composable
private fun AnimatedInsertedItem(
    animate: Boolean,
    content: @Composable () -> Unit
) {
    if (animate) {
        StackInsertItem(content = content)
    } else {
        content()
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
private fun TaskQueueHeader(
    sortMode: KudoTaskSortMode,
    palette: KudoPalette,
    onLongPress: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptics = rememberKudoHaptics()
    val viewConfiguration = LocalViewConfiguration.current
    val holdProgress = remember { Animatable(0f) }
    var longPressTriggered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .pointerInput(sortMode) {
                detectTapGestures(
                    onPress = {
                        longPressTriggered = false
                        val holdJob = scope.launch {
                            holdProgress.snapTo(0f)
                            holdProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = viewConfiguration.longPressTimeoutMillis.toInt(),
                                    easing = LinearEasing
                                )
                            )
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            holdJob.cancel()
                            if (!longPressTriggered) {
                                scope.launch {
                                    holdProgress.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 110)
                                    )
                                }
                            }
                        }
                    },
                    onLongPress = {
                        longPressTriggered = true
                        haptics.vibrate(HapticTickMs)
                        scope.launch { holdProgress.snapTo(0f) }
                        onLongPress()
                    }
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TASKS",
            color = palette.textSub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (sortMode == KudoTaskSortMode.AutoDue) {
            Text(
                text = "by date",
                color = palette.textSub.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "hold to reset",
                color = palette.textSub.copy(alpha = if (holdProgress.value > 0.1f) holdProgress.value else 0.3f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
internal fun rememberKudoHaptics(): KudoHaptics {
    val context = LocalContext.current
    return remember(context) { KudoHaptics(context) }
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
    val fieldTextStyle = TextStyle.Default.copy(
        color = textColor,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign,
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = fieldTextStyle,
        cursorBrush = SolidColor(textColor),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        }
    )
}



private fun formatMultiplier(multiplier: Float): String {
    return String.format(Locale.US, "%.2f", multiplier)
}

private fun lerpFloat(start: Float, stop: Float, progress: Float): Float {
    return start + (stop - start) * progress.coerceIn(0f, 1f)
}

internal data class DueBadge(
    val text: String,
    val color: Color
)

private fun sortTasksForDisplay(
    tasks: List<KudoTask>,
    sortMode: KudoTaskSortMode
): List<KudoTask> {
    return when (sortMode) {
        KudoTaskSortMode.Manual -> tasks.sortedWith(
            compareBy<KudoTask>({ it.order }, { it.id })
        )

        KudoTaskSortMode.AutoDue -> tasks.sortedWith(
            compareBy<KudoTask>(
                { it.dueAtEpochMillis == null },
                { it.dueAtEpochMillis ?: Long.MAX_VALUE }
            )
        )
    }
}


internal fun dueBadgeFor(
    dueAtEpochMillis: Long,
    palette: KudoPalette
): DueBadge {
    val dueDateTime = Instant.ofEpochMilli(dueAtEpochMillis).atZone(AppZoneId)
    val dueDate = dueDateTime.toLocalDate()
    val dueLabel = dueDateTime.format(DueDateTimeFormatter)
    val today = LocalDate.now(AppZoneId)
    val now = System.currentTimeMillis()
    return when {
        dueAtEpochMillis < now -> DueBadge(
            text = "Overdue · $dueLabel",
            color = palette.red
        )

        dueDate == today -> DueBadge(
            text = "Today · ${dueDateTime.format(TimeFormatter)}",
            color = palette.orange
        )

        dueDate == today.plusDays(1) -> DueBadge(
            text = "Tomorrow · ${dueDateTime.format(TimeFormatter)}",
            color = palette.blue
        )

        else -> DueBadge(
            text = dueLabel,
            color = palette.textSub
        )
    }
}

internal fun formatCompactDueDate(dueAtEpochMillis: Long): String {
    return Instant.ofEpochMilli(dueAtEpochMillis)
        .atZone(AppZoneId)
        .format(EditableDueDateTimeFormatter)
}

private fun formatTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .format(TimeFormatter)
}

internal fun formatDayHeader(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .format(DayHeaderFormatter)
}

internal fun isToday(timestamp: Long): Boolean {
    if (timestamp == 0L) return false
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .toLocalDate() == LocalDate.now(AppZoneId)
}

internal val AppZoneId: ZoneId = ZoneId.systemDefault()
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
private val DayHeaderFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
private val DueDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d · H:mm", Locale.getDefault())
private val EditableDueDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d · H:mm", Locale.getDefault())
