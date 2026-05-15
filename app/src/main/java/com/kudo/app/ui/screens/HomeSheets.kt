package com.kudo.app.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kudo.app.core.model.KudoSubtask
import com.kudo.app.core.model.KudoSubtaskDraft
import com.kudo.app.core.repository.KudoStateRepository
import com.kudo.app.ui.viewmodel.EditingTarget
import com.kudo.app.ui.viewmodel.KudoEditKind
import com.kudo.app.ui.viewmodel.KudoUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSheet(
    uiState: KudoUiState,
    palette: KudoPalette,
    isFileActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onToggleHelp: () -> Unit,
    onSetTheme: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onImportTasks: () -> Unit
) {
    val ratio = remember(uiState.data.logs) {
        var income = 0
        var expense = 0
        uiState.data.logs.forEach { log ->
            if (log.coins > 0) {
                income += log.coins
            } else if (log.coins < 0) {
                expense += -log.coins
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
                    text = if (uiState.view.helpVisible) "Help" else "Settings",
                    color = palette.textMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onToggleHelp) {
                    Icon(
                        imageVector = if (uiState.view.helpVisible) {
                            Icons.Rounded.ArrowBack
                        } else {
                            Icons.Rounded.HelpOutline
                        },
                        contentDescription = if (uiState.view.helpVisible) {
                            "Back to settings"
                        } else {
                            "Open help"
                        },
                        tint = palette.textSub
                    )
                }
            }

            if (!uiState.view.helpVisible) {
                StatGrid(
                    palette = palette,
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

                Spacer(modifier = Modifier.height(16.dp))
                NotificationPermissionsSection(palette = palette)

                Spacer(modifier = Modifier.height(20.dp))

                ActionRow(
                    title = "Backup Data",
                    palette = palette,
                    icon = Icons.Rounded.Download,
                    enabled = !isFileActionInProgress,
                    onClick = onExport
                )
                ActionRow(
                    title = "Restore Backup",
                    palette = palette,
                    icon = Icons.Rounded.Upload,
                    enabled = !isFileActionInProgress,
                    onClick = onImport
                )
                ActionRow(
                    title = "Import Tasks",
                    palette = palette,
                    icon = Icons.Rounded.Upload,
                    enabled = !isFileActionInProgress,
                    onClick = onImportTasks
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
private fun StatGrid(
    palette: KudoPalette,
    ratio: String
) {
    StatCard(
        title = "Income / Expense",
        value = ratio,
        subtitle = "RATIO",
        palette = palette,
        modifier = Modifier.fillMaxWidth()
    )
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
    icon: ImageVector,
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
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    palette: KudoPalette,
    onToggle: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .clickable {
                haptics.vibrate(HapticTickMs)
                onToggle()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = palette.textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = palette.textSub,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (enabled) "ON" else "OFF",
            color = if (enabled) palette.green else palette.textSub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (enabled) palette.greenBg else palette.card)
                .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    palette: KudoPalette,
    icon: ImageVector,
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
            text = "奖励驱动的效率工具：完成任务赚取金币，在商店兑换自我奖励。",
            color = palette.textSub,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(text = "📌 任务分类", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "Habits：重复进行的日常项，每次点击完成即可充能并获得金币。\nTasks：有序队列，只有队首任务（高亮）可以完成。拖拽可调整顺序。",
            color = palette.textSub,
            fontSize = 13.sp
        )
        Text(text = "🖐️ 手势操作", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "添加：从顶部下划展开输入面板，提交后自动收起。\nHabits：点击完成，长按进入排序 / 删除模式。\nTasks：左右滑均可完成队首任务并获得金币，点击可编辑标题、金币值、截止日及子任务，长按拖动排序，长按 TASKS 标签可恢复默认日期排序。\nStore：右滑消费金币购买奖励，点击编辑内容，长按排序。",
            color = palette.textSub,
            fontSize = 13.sp
        )
        Text(text = "💾 数据安全", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "备份：通过系统文件管理器将数据导出为 JSON 文件。\n恢复：导入备份文件，会覆盖当前全部数据，请谨慎操作。\n导入任务：粘贴纯文本或 Markdown 列表，会追加到 Tasks。",
            color = palette.textSub,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CompactEditLabel(
    text: String,
    palette: KudoPalette
) {
    Text(
        text = text.uppercase(Locale.US),
        color = palette.textSub,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditSheet(
    uiState: KudoUiState,
    palette: KudoPalette,
    target: EditingTarget,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?, List<KudoSubtaskDraft>?) -> Unit,
    onDelete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val targetTask = if (target.kind == KudoEditKind.Task) {
        uiState.data.tasks.firstOrNull { it.id == target.id }
    } else null
    val targetHabit = if (target.kind == KudoEditKind.Habit) {
        uiState.data.habits.firstOrNull { it.id == target.id }
    } else null
    val targetStore = if (target.kind == KudoEditKind.Store) {
        uiState.data.store.firstOrNull { it.id == target.id }
    } else null
    val isTaskEditor = target.kind == KudoEditKind.Task && targetTask != null
    val isSubtaskLocked = targetTask?.isSubtaskStructureLocked == true
    val hasStableSubtaskViewport = isTaskEditor
    val stableSheetViewportHeight = remember(configuration.screenHeightDp) {
        (configuration.screenHeightDp.dp * 0.72f).coerceAtMost(560.dp)
    }
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(target.id, false) {
        mutableStateOf(
            targetTask?.title
                ?: targetHabit?.title
                ?: targetStore?.title.orEmpty()
        )
    }
    var value by remember(target.id, false) {
        mutableStateOf(
            when {
                targetTask != null -> targetTask.coins.toString()
                targetHabit != null -> targetHabit.coins.toString()
                targetStore != null -> targetStore.cost.toString()
                else -> ""
            }
        )
    }
    val initialDueDateTime = remember(target.id, false, targetTask?.dueAtEpochMillis) {
        targetTask?.dueAtEpochMillis?.let { Instant.ofEpochMilli(it).atZone(AppZoneId).toLocalDateTime() }
    }
    var dueDate by remember(target.id, false, targetTask?.dueAtEpochMillis) {
        mutableStateOf(initialDueDateTime?.toLocalDate())
    }
    var hasCustomDueTime by remember(target.id, false, targetTask?.dueAtEpochMillis) {
        mutableStateOf(initialDueDateTime?.toLocalTime() != null && initialDueDateTime.toLocalTime() != DefaultDueTime)
    }
    var customDueTime by remember(target.id, false, targetTask?.dueAtEpochMillis) {
        mutableStateOf(initialDueDateTime?.toLocalTime() ?: DefaultDueTime)
    }
    var subtaskDrafts by remember(target.id, false, targetTask?.subtasks) {
        mutableStateOf(
            targetTask?.subtasks?.map { subtask ->
                KudoSubtaskDraft(title = subtask.title)
            } ?: emptyList()
        )
    }
    var newSubtaskTitle by remember(target.id, false) {
        mutableStateOf("")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.card,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasStableSubtaskViewport) {
                        Modifier.height(stableSheetViewportHeight)
                    } else {
                        Modifier
                    }
                )
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Edit Item",
                color = palette.textMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = EditFieldMinHeight),
                placeholder = { Text("Title", color = palette.textSub) },
                colors = textFieldColors(palette),
                shape = EditControlShape,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (isTaskEditor) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactEditLabel(text = "Value", palette = palette)
                    TextField(
                        value = value,
                        onValueChange = { value = it.filter(Char::isDigit) },
                        enabled = !isSubtaskLocked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = EditFieldMinHeight),
                        placeholder = {
                            Text(
                                text = "0",
                                color = palette.textSub
                            )
                        },
                        colors = textFieldColors(palette),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = EditControlShape,
                        singleLine = true
                    )

                    DeadlinePanel(
                        palette = palette,
                        dueDate = dueDate,
                        hasCustomDueTime = hasCustomDueTime,
                        customDueTime = customDueTime,
                        onPickDate = {
                            val initialDate = dueDate ?: LocalDate.now(AppZoneId)
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    dueDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                initialDate.year,
                                initialDate.monthValue - 1,
                                initialDate.dayOfMonth
                            ).show()
                        },
                        onClearDate = { dueDate = null },
                        onPickCustomTime = {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    hasCustomDueTime = true
                                    customDueTime = LocalTime.of(hourOfDay, minute)
                                },
                                customDueTime.hour,
                                customDueTime.minute,
                                true
                            ).show()
                        },
                        onResetTime = {
                            hasCustomDueTime = false
                            customDueTime = DefaultDueTime
                        }
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                CompactEditLabel(text = "Subtasks", palette = palette)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.background)
                            .border(1.dp, palette.line, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (subtaskDrafts.isEmpty()) {
                            Text(
                                text = "Add subtasks only when you want partial rewards.",
                                color = palette.textSub,
                                fontSize = 12.sp
                            )
                        } else {
                            subtaskDrafts.forEachIndexed { index, draft ->
                                val existingSubtask = targetTask?.subtasks?.getOrNull(index)
                                SubtaskEditorRow(
                                    title = draft.title,
                                    completed = existingSubtask?.isCompleted == true,
                                    palette = palette,
                                    locked = isSubtaskLocked,
                                    onRemove = {
                                        if (!isSubtaskLocked) {
                                            subtaskDrafts = subtaskDrafts.filterIndexed { itemIndex, _ ->
                                                itemIndex != index
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        if (!isSubtaskLocked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = newSubtaskTitle,
                                    onValueChange = { newSubtaskTitle = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = EditFieldMinHeight),
                                    placeholder = { Text("Add subtask", color = palette.textSub) },
                                    colors = textFieldColors(palette),
                                    shape = EditControlShape,
                                    singleLine = true
                                )
                                TextButton(
                                    onClick = {
                                        val trimmed = newSubtaskTitle.trim()
                                        if (trimmed.isNotBlank()) {
                                            subtaskDrafts = subtaskDrafts + KudoSubtaskDraft(title = trimmed)
                                            newSubtaskTitle = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = EditFieldMinHeight)
                                        .clip(EditControlShape)
                                        .background(palette.card)
                                        .border(1.dp, palette.line, EditControlShape)
                                ) {
                                    Text(
                                        "Add",
                                        color = palette.textMain,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = "Reward is split equally across subtasks.",
                                color = palette.textSub,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = "Reward split locks after the first subtask is checked.",
                                color = palette.textSub,
                                fontSize = 11.sp
                            )
                        }
                    }
            } else {
                CompactEditLabel(text = "Value", palette = palette)
                TextField(
                    value = value,
                    onValueChange = { value = it.filter(Char::isDigit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = EditFieldMinHeight),
                    placeholder = {
                        Text(
                            text = "0",
                            color = palette.textSub
                        )
                    },
                    colors = textFieldColors(palette),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = EditControlShape,
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = EditButtonHeight)
                        .clip(EditControlShape)
                        .background(palette.background)
                        .border(1.dp, palette.orange.copy(alpha = 0.22f), EditControlShape)
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
                    onClick = {
                        onSave(
                            title,
                            value,
                            dueDate?.let {
                                resolveDueAtEpochMillis(
                                    date = it,
                                    customTime = customDueTime.takeIf { hasCustomDueTime }
                                )
                            },
                            subtaskDrafts.takeIf { isTaskEditor }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = EditButtonHeight)
                        .clip(EditControlShape)
                        .background(palette.textMain)
                ) {
                    Text(
                        text = "Save",
                        color = palette.background,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SubtaskEditorRow(
    title: String,
    completed: Boolean,
    palette: KudoPalette,
    locked: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(EditControlShape)
            .background(palette.card)
            .border(1.dp, palette.line, EditControlShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (completed) palette.textSub else palette.textMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (completed) TextDecoration.LineThrough else null,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (!locked) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(EditChipSize)
                    .clip(CircleShape)
                    .background(palette.background)
                    .border(1.dp, palette.line, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemove
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Remove subtask",
                    tint = palette.orange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
internal fun EmptyState(
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
internal fun textFieldColors(palette: KudoPalette) = TextFieldDefaults.colors(
    focusedContainerColor = palette.background,
    unfocusedContainerColor = palette.background,
    disabledContainerColor = palette.background,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = palette.textMain,
    focusedTextColor = palette.textMain,
    unfocusedTextColor = palette.textMain,
    disabledTextColor = palette.textSub
)

@Composable
private fun DeadlinePanel(
    palette: KudoPalette,
    dueDate: LocalDate?,
    hasCustomDueTime: Boolean,
    customDueTime: LocalTime,
    onPickDate: () -> Unit,
    onClearDate: () -> Unit,
    onPickCustomTime: () -> Unit,
    onResetTime: () -> Unit
) {
    val hasDeadline = dueDate != null
    val dueTime = if (hasCustomDueTime) customDueTime else DefaultDueTime
    val summary = when {
        !hasDeadline -> "No deadline"
        else -> "${dueDate?.format(DeadlineDateFormatter).orEmpty()} at ${dueTime.format(DeadlineTimeFormatter)}"
    }
    val detail = when {
        !hasDeadline -> "Pick a date. If you skip time, the reminder stays at 09:00."
        hasCustomDueTime -> "Custom time"
        else -> "Default time"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactEditLabel(text = "Deadline", palette = palette)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(palette.background)
                .border(1.dp, palette.line, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary,
                        color = palette.textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detail,
                        color = palette.textSub,
                        fontSize = 12.sp
                    )
                }
                if (hasDeadline) {
                    TextButton(onClick = onClearDate) {
                        Text(
                            text = "Clear",
                            color = palette.orange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            DeadlineRowButton(
                label = "Date",
                value = dueDate?.format(DeadlineDateFormatter) ?: "Choose",
                hint = if (hasDeadline) "Tap to change" else "Tap to set",
                palette = palette,
                onClick = onPickDate
            )

            if (hasDeadline) {
                DeadlineRowButton(
                    label = "Time",
                    value = dueTime.format(DeadlineTimeFormatter),
                    hint = if (hasCustomDueTime) "Tap to change" else "Optional. Defaults to 09:00",
                    palette = palette,
                    onClick = onPickCustomTime
                )
            }

            if (hasDeadline && hasCustomDueTime) {
                TextButton(
                    onClick = onResetTime,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Use default 09:00",
                        color = palette.textSub,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DeadlineRowButton(
    label: String,
    value: String,
    hint: String,
    palette: KudoPalette,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.card)
            .border(1.dp, palette.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = palette.textSub,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = palette.textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = hint,
            color = palette.textSub,
            fontSize = 12.sp,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun NotificationPermissionsSection(palette: KudoPalette) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var canScheduleExact by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
            } else true
        )
    }
    var isIgnoringBatteryOpts by remember {
        mutableStateOf(
            context.getSystemService(PowerManager::class.java)
                ?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
                } else true
                isIgnoringBatteryOpts = context.getSystemService(PowerManager::class.java)
                    ?.isIgnoringBatteryOptimizations(context.packageName) ?: true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val showExactAlarm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    if (notificationsEnabled && (!showExactAlarm || canScheduleExact) && isIgnoringBatteryOpts) return

    Text(
        text = "Reminders",
        color = palette.textMain,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!notificationsEnabled) {
            NotificationPermRow(
                title = "Notifications",
                ok = false,
                palette = palette,
                onClick = {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    }
                }
            )
        }
        if (showExactAlarm) {
            NotificationPermRow(
                title = "Exact Alarms",
                ok = canScheduleExact,
                palette = palette,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                }
            )
        }
        if (!isIgnoringBatteryOpts) {
            NotificationPermRow(
                title = "Battery Optimization",
                ok = false,
                palette = palette,
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                }
            )
        }
        Text(
            text = "On Vivo: also enable Auto-start and set Battery Usage to Unrestricted in your phone manager.",
            color = palette.textSub,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun NotificationPermRow(
    title: String,
    ok: Boolean,
    palette: KudoPalette,
    onClick: () -> Unit
) {
    val haptics = rememberKudoHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .then(
                if (!ok) Modifier.clickable {
                    haptics.vibrate(HapticTickMs)
                    onClick()
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = palette.textMain,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (ok) "OK" else "Fix →",
            color = if (ok) palette.green else palette.orange,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (ok) palette.greenBg else palette.orangeBg)
                .border(1.dp, palette.line, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun resolveDueAtEpochMillis(
    date: LocalDate,
    customTime: LocalTime?
): Long {
    return LocalDateTime.of(date, customTime ?: DefaultDueTime)
        .atZone(AppZoneId)
        .toInstant()
        .toEpochMilli()
}

private val DeadlineDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())

private val DeadlineTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())

private val DefaultDueTime: LocalTime = LocalTime.of(9, 0)
