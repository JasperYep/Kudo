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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
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
    var isTaskImportDialogVisible by rememberSaveable { mutableStateOf(false) }
    var taskImportText by rememberSaveable { mutableStateOf("") }
    var isFileTransferInProgress by remember { mutableStateOf(false) }
    var activeComposerRevealProgress by remember { mutableFloatStateOf(0f) }
    val tasksListState = rememberLazyListState()
    val storeListState = rememberLazyListState()
    val logListState = rememberLazyListState()
    LaunchedEffect(uiState.currentView) {
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
                task.type == KudoState.TYPE_TASK &&
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
                when (uiState.currentView) {
                    KudoViewModel.VIEW_TASKS -> PullComposerPage(
                        currentView = uiState.currentView,
                        taskCreationTarget = uiState.taskCreationTarget,
                        storeMode = uiState.storeMode,
                        palette = palette,
                        title = taskDraftTitle,
                        value = taskDraftValue,
                        listState = tasksListState,
                        onTitleChange = {
                            if (it.contains("\n") && it.lines().size > 1) {
                                // Multi-line paste detected, show import preview
                                viewModel.showImportPreview(it)
                                taskDraftTitle = ""
                            } else {
                                taskDraftTitle = it
                            }
                        },
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
                            tasks = uiState.data.tasks,
                            multiplier = uiState.data.multiplier,
                            taskSortMode = uiState.data.taskSortMode,
                            habitsCollapsed = uiState.habitsCollapsed,
                            isHabitJiggleMode = uiState.isHabitJiggleMode,
                            recentTaskInsertId = uiState.recentTaskInsertId,
                            palette = palette,
                            modifier = modifier,
                            userScrollEnabled = pageScrollEnabled,
                            onGestureLockChange = onGestureLockChange,
                            onToggleHabits = viewModel::toggleHabitsCollapsed,
                            onExitHabitJiggle = viewModel::exitHabitJiggleMode,
                            onEnterHabitJiggle = viewModel::enterHabitJiggleMode,
                            onDeleteHabit = viewModel::deleteTaskItem,
                            onDeleteTasks = viewModel::deleteTaskItems,
                            onReorderTask = viewModel::reorderTasks,
                            onReorderHabits = viewModel::reorderHabits,
                            onCompleteTask = viewModel::completeTask,
                            onEditTask = viewModel::openEditTask,
                            onToggleTimer = viewModel::toggleTimer,
                            onCompleteHabit = viewModel::completeHabit,
                            listState = tasksListState
                        )
                    }

                    KudoViewModel.VIEW_STORE -> PullComposerPage(
                        currentView = uiState.currentView,
                        taskCreationTarget = uiState.taskCreationTarget,
                        storeMode = uiState.storeMode,
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
                            storeItems = uiState.data.store,
                            coins = uiState.data.coins,
                            recentStoreInsertId = uiState.recentStoreInsertId,
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

                    KudoViewModel.VIEW_LOG -> LogPage(
                        uiState = uiState,
                        palette = palette,
                        modifier = Modifier.fillMaxSize(),
                        onUndo = viewModel::undoLog,
                        onLongPressTrend = { /* no-op */ },
                        listState = logListState
                    )
                }

                UndoBanner(
                    visible = uiState.showUndoBanner,
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

    if (uiState.isImportPreviewVisible) {
        ImportPreviewOverlay(
            uiState = uiState,
            palette = palette,
            onUpdateDraft = viewModel::updateImportDraft,
            onDeleteDraft = viewModel::deleteImportDraft,
            onConfirm = viewModel::confirmImportPreview,
            onDismiss = viewModel::dismissImportPreview
        )
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

    val editTarget = uiState.editingTarget

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
internal data class KudoPalette(
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

private val HeaderHeight = 80.dp
internal const val HapticTickMs = 8L
internal const val HapticConfirmMs = 12L
internal const val HapticErrorMs = 24L
internal const val ReorderSwapHapticMs = 4L
internal const val ReorderSwapHapticCooldownMs = 42L
internal const val ReorderCancelAnimationStiffness = 520f
internal val ReorderAutoScrollMax = 34.dp
internal val EditControlShape = RoundedCornerShape(16.dp)
internal val EditFieldMinHeight = 54.dp
internal val EditButtonHeight = 50.dp
internal val EditChipSize = 34.dp
internal val TaskTabBalanceRowEstimate = 60.dp

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
    currentView: String,
    taskCreationTarget: TaskCreationTarget,
    storeMode: Int,
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
                    currentView = currentView,
                    taskCreationTarget = taskCreationTarget,
                    storeMode = storeMode,
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
    currentView: String,
    taskCreationTarget: TaskCreationTarget,
    storeMode: Int,
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
        currentView == KudoViewModel.VIEW_STORE -> "Add to Store..."
        taskCreationTarget == TaskCreationTarget.HABIT -> "Add Habit..."
        else -> "Add Task..."
    }
    val valuePlaceholder = when {
        currentView == KudoViewModel.VIEW_STORE -> "0"
        else -> "" // Optional - empty means use timer
    }
    val modeText = when {
        currentView == KudoViewModel.VIEW_STORE && storeMode == KudoState.STORE_INFINITE -> "INFIN"
        currentView == KudoViewModel.VIEW_STORE -> "ONCE"
        taskCreationTarget == TaskCreationTarget.HABIT -> "HABIT"
        else -> "TASK"
    }
    val modeColor = when {
        currentView == KudoViewModel.VIEW_STORE -> palette.orange
        taskCreationTarget == TaskCreationTarget.HABIT -> palette.gold
        else -> palette.green
    }
    val valueColor = when {
        currentView == KudoViewModel.VIEW_STORE -> palette.orange
        value.isBlank() || value == "0" -> palette.textSub
        else -> palette.green
    }
    val addButtonProgress = revealProgress.coerceIn(0f, 1f)
    val addButtonScale = lerpFloat(0.7f, 1f, addButtonProgress)
    val addButtonAlpha = lerpFloat(0.56f, 1f, addButtonProgress)
    val showValueInput = currentView != KudoViewModel.VIEW_STORE && taskCreationTarget != TaskCreationTarget.HABIT
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
                // Always show value input for tasks (Store uses "0", Habits have their fixed value)
                if (showValueInput) {
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
                } else {
                    // Store and Habits use fixed cost/val display, show $ indicator
                    if (currentView == KudoViewModel.VIEW_STORE || taskCreationTarget == TaskCreationTarget.HABIT) {
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
                    }
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
    tasks: List<KudoTask>,
    multiplier: Float,
    taskSortMode: Int,
    habitsCollapsed: Boolean,
    isHabitJiggleMode: Boolean,
    recentTaskInsertId: Long?,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit,
    onToggleHabits: () -> Unit,
    onExitHabitJiggle: () -> Unit,
    onEnterHabitJiggle: () -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onDeleteTasks: (Set<Long>) -> Unit,
    onReorderTask: (List<Long>) -> Unit,
    onReorderHabits: (List<Long>) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onEditTask: (Long) -> Unit,
    onToggleTimer: (Long) -> Unit,
    onCompleteHabit: (Long) -> Unit,
    listState: LazyListState
) {
    val haptics = rememberKudoHaptics()
    var isHabitGestureLocked by remember { mutableStateOf(false) }
    var isTaskSwipeGestureLocked by remember { mutableStateOf(false) }
    var isTaskLongPressGestureLocked by remember { mutableStateOf(false) }
    val habits = remember(tasks) {
        tasks.filter { it.type == KudoState.TYPE_HABIT }
    }
    var localHabits by remember { mutableStateOf(habits) }
    val currentSortMode = taskSortMode
    val taskItems = remember(tasks, currentSortMode) {
        sortTasksForDisplay(
            tasks = tasks.filter { it.type == KudoState.TYPE_TASK },
            sortMode = currentSortMode
        )
    }

    var localTasks by remember { mutableStateOf(taskItems) }
    var lastTaskSwapHapticAtMs by remember { mutableStateOf(0L) }
    val newTopTaskId = localTasks.lastOrNull()?.id?.takeIf { it == recentTaskInsertId }

    val localTaskIds = remember(localTasks) { localTasks.map(KudoTask::id).toSet() }
    val taskOrderIds = remember(taskItems) { taskItems.map(KudoTask::id) }
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
    LaunchedEffect(taskItems) {
        if (!isTaskReordering) {
            localTasks = taskItems
        }
    }

    LaunchedEffect(isListGestureLocked) {
        onGestureLockChange(isListGestureLocked)
    }
    LaunchedEffect(isHabitJiggleMode) {
        if (!isHabitJiggleMode) {
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
                    collapsed = habitsCollapsed,
                    onClick = {
                        onExitHabitJiggle()
                        onToggleHabits()
                    }
                )
            }
            item(key = "habits_grid") {
                AnimatedVisibility(
                    visible = !habitsCollapsed,
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
                        finalMultiplier = multiplier,
                        isJiggleMode = isHabitJiggleMode,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onGestureLockChange = { isHabitGestureLocked = it },
                        onHabitsChange = { localHabits = it },
                        onDragFinished = {
                            if (localHabits.map(KudoTask::id) != habits.map(KudoTask::id)) {
                                onReorderHabits(localHabits.map(KudoTask::id))
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
            SectionHeader(
                title = "TASKS",
                palette = palette
            )
        }

        if (taskItems.isEmpty()) {
            item(key = "empty_tasks") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isHabitJiggleMode) {
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
                            finalMultiplier = multiplier,
                            isActive = isActive,
                            isDragging = isDragging,
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
                            swipeEnabled = !isDragging && !isTaskLongPressGestureLocked,
                            onGestureLockChange = { isTaskSwipeGestureLocked = it },
                            onComplete = {
                                onExitHabitJiggle()
                                onCompleteTask(task.id)
                            },
                            onEdit = {
                                onExitHabitJiggle()
                                onEditTask(task.id)
                            },
                            onToggleTimer = {
                                onToggleTimer(task.id)
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
    storeItems: List<KudoStoreItem>,
    coins: Int,
    recentStoreInsertId: Long?,
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
    var localStore by remember { mutableStateOf(storeItems) }
    var isStoreItemGestureLocked by remember { mutableStateOf(false) }
    val storeOrderIds = remember(storeItems) { storeItems.map(KudoStoreItem::id) }
    val localStoreOrderIds = remember(localStore) { localStore.map(KudoStoreItem::id) }
    val newTopStoreId = localStore.firstOrNull()?.id?.takeIf { it == recentStoreInsertId }
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

    LaunchedEffect(storeItems) {
        if (!isStoreReordering) {
            localStore = storeItems
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
            if (storeItems.isEmpty()) {
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
                        coins = coins,
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

private val HabitGridSpacing = 10.dp
private val HabitGridItemHeight = 64.dp

@Composable
private fun HabitsGrid(
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
        val jiggleRotation = rememberHabitJiggleRotation(isJiggleMode)

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
                        jiggleRotation = jiggleRotation,
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

private fun maybeTriggerReorderSwapHaptic(
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
private fun LogPage(
    uiState: KudoUiState,
    palette: KudoPalette,
    modifier: Modifier = Modifier,
    onUndo: (Int) -> Unit,
    onLongPressTrend: () -> Unit,
    listState: LazyListState
) {
    if (uiState.data.logs.isEmpty()) {
        Column(modifier = modifier.fillMaxSize()) {
            TrendChart(
                logs = uiState.data.logs,
                palette = palette,
                onLongPressTrend = onLongPressTrend
            )
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
            TrendChart(
                logs = uiState.data.logs,
                palette = palette,
                onLongPressTrend = onLongPressTrend
            )
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
    jiggleRotation: Float,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit,
    onEnterJiggle: () -> Unit,
    onDelete: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)
    val completedToday = remember(task.last) { isToday(task.last) }
    val value = remember(task.valAmount, finalMultiplier) {
        (task.valAmount * finalMultiplier).toInt()
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
    isActive: Boolean = false,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onGestureLockChange: (Boolean) -> Unit = {},
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onToggleTimer: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    var currentNow by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(task.isTimerRunning) {
        if (task.isTimerRunning) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                currentNow = System.currentTimeMillis()
            }
        }
    }

    val runningElapsed = if (task.isTimerRunning) {
        (currentNow - task.lastTimerStart).coerceAtLeast(0L)
    } else {
        0L
    }
    val totalMillis = (task.accumulatedTimeMillis + runningElapsed).coerceAtLeast(0L)
    val totalMinutes = (totalMillis / 60_000L).toInt()
    val timeBasedValue = totalMinutes  // 1 coin per minute
    val hasManualValue = task.valAmount > 0
    val baseValue = if (hasManualValue) task.valAmount else timeBasedValue
    val reward = (baseValue * finalMultiplier).toInt()

    val timeFormatted = remember(totalMillis) {
        val totalSec = totalMillis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        String.format("%02d:%02d", min, sec)
    }

    val dueBadge = remember(task.dueAtEpochMillis, palette) {
        task.dueAtEpochMillis?.let { dueBadgeFor(it, palette) }
    }
    val dueText = dueBadge?.text ?: " "
    val dueColor = dueBadge?.color ?: palette.textSub.copy(alpha = 0f)
    val timerTextColor = when {
        task.isTimerRunning -> palette.orange
        totalMillis > 0L -> palette.green
        else -> palette.textSub.copy(alpha = 0.58f)
    }
    val rowShape = RoundedCornerShape(14.dp)
    // Row always uses default style - highlight only on badge area
    val rowBackgroundColor = Color.Transparent
    val rowBorderColor = palette.line
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(rowBackgroundColor, rowShape)
                .border(1.dp, rowBorderColor, rowShape)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasDueDate = dueBadge != null && dueText.isNotBlank() && dueText != " "
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = if (hasDueDate) Alignment.CenterStart else Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = if (hasDueDate) Arrangement.Center else Arrangement.Center
                ) {
                    Text(
                        text = task.title,
                        color = palette.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasDueDate) {
                        Text(
                            text = dueText,
                            color = dueColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Badge: show value if set and no timer, otherwise show timer (always clickable)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            task.isTimerRunning -> palette.orangeBg.copy(alpha = 0.3f)
                            totalMillis > 0L -> palette.greenBg.copy(alpha = 0.2f)
                            hasManualValue -> palette.greenBg.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptics.vibrate(HapticTickMs)
                        onToggleTimer()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // If timer is running or has time, show timer; otherwise show fixed value
                val showTimer = task.isTimerRunning || totalMillis > 0
                if (showTimer) {
                    // Show timer
                    Text(
                        text = timeFormatted,
                        color = timerTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                } else {
                    // Show fixed value
                    Text(
                        text = "+$${task.valAmount}",
                        color = palette.green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
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
internal fun rememberKudoHaptics(): KudoHaptics {
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

@Composable
private fun TrendChart(
    logs: List<KudoLogEntry>,
    palette: KudoPalette,
    onLongPressTrend: () -> Unit
) {
    val income = remember(logs) { buildTrend(logs, positive = true) }
    val expense = remember(logs) { buildTrend(logs, positive = false) }
    val haptics = rememberKudoHaptics()

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
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            haptics.vibrate(HapticTickMs)
                            onLongPressTrend()
                        })
                    }
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
private fun UndoBanner(
    visible: Boolean,
    latestLog: KudoLogEntry?,
    palette: KudoPalette,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(220)) + slideInVertically(tween(260, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))) { it / 2 },
        exit = fadeOut(tween(160))
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(palette.card)
                .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                .padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = latestLog?.text?.let { if (it.length > 30) it.take(30) + "…" else it } ?: "Done",
                color = palette.textSub,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(palette.line)
            )
            Text(
                text = "undo",
                color = palette.textMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onUndo() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
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
                label = "Log",
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
    sortMode: Int
): List<KudoTask> {
    return when (sortMode) {
        KudoState.TASK_SORT_MANUAL -> tasks.sortedWith(
            compareBy<KudoTask>({ it.order }, { it.id })
        )

        else -> tasks.sortedWith(
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

internal val AppZoneId: ZoneId = ZoneId.systemDefault()
private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
private val DayHeaderFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
private val DueDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d · H:mm", Locale.getDefault())
private val EditableDueDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d · H:mm", Locale.getDefault())
