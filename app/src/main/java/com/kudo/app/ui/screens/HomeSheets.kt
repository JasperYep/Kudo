package com.kudo.app.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kudo.app.core.model.KudoSubtask
import com.kudo.app.core.model.KudoSubtaskDraft
import com.kudo.app.core.repository.KudoStateRepository
import com.kudo.app.ui.viewmodel.EditingTarget
import com.kudo.app.ui.viewmodel.KudoUiState
import com.kudo.app.ui.viewmodel.KudoViewModel
import java.time.LocalDate
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
    onSetSubtaskModeEnabled: (Boolean) -> Unit,
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

                Spacer(modifier = Modifier.height(16.dp))

                SettingsToggleRow(
                    title = "Subtask Mode",
                    subtitle = "Enable partial coin rewards for larger tasks.",
                    enabled = uiState.isSubtaskModeEnabled,
                    palette = palette,
                    onToggle = {
                        onSetSubtaskModeEnabled(!uiState.isSubtaskModeEnabled)
                    }
                )

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
            text = "奖励驱动的效率工具：赚取金币，兑换自我奖励。",
            color = palette.textSub,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(text = "📌 任务分类", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "Habits：重复进行的日常项。\nTasks：一次性事项，分为 Focus 与 Inbox。\n子任务模式可在 Settings 中单独开启。",
            color = palette.textSub,
            fontSize = 13.sp
        )
        Text(text = "🖐️ 手势操作", color = palette.textMain, fontWeight = FontWeight.Bold)
        Text(
            text = "顶部：在页面顶部下划展开添加面板，提交后会自动收起。\nHabits：点击充能，长按进入编辑 / 排序 / 删除模式。\nTasks：右滑完成，左滑流转，点击编辑可设置截止日；如果在 Settings 开启子任务模式，还可添加子任务并按 S / M / L 自动切分金币；长按排序，长按 Focus / Inbox 标签可恢复默认日期排序。\nStore：滑动消费金币，点击编辑奖励内容，长按排序。",
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
    val targetTask = uiState.data.tasks.firstOrNull { it.id == target.id }
    val targetStore = uiState.data.store.firstOrNull { it.id == target.id }
    val isPendingMove = uiState.pendingMoveTaskId != null
    val isTaskEditor = target.kind == KudoViewModel.KIND_TASK && targetTask != null
    val isSubtaskModeEnabled = uiState.isSubtaskModeEnabled
    val isSubtaskLocked = targetTask?.isSubtaskStructureLocked == true
    val hasStableSubtaskViewport = isTaskEditor && isSubtaskModeEnabled
    val stableSheetViewportHeight = remember(configuration.screenHeightDp) {
        (configuration.screenHeightDp.dp * 0.72f).coerceAtMost(560.dp)
    }
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
    var dueEpochDay by remember(target.id, isPendingMove, targetTask?.dueEpochDay) {
        mutableStateOf(targetTask?.dueEpochDay)
    }
    var subtaskDrafts by remember(target.id, isPendingMove, targetTask?.subtasks) {
        mutableStateOf(
            targetTask?.subtasks?.map { subtask ->
                KudoSubtaskDraft(
                    title = subtask.title,
                    difficulty = subtask.difficulty
                )
            } ?: emptyList()
        )
    }
    var newSubtaskTitle by remember(target.id, isPendingMove) {
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
                text = if (isPendingMove) "Set Value & Move" else "Edit Item",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        CompactEditLabel(text = "Value", palette = palette)
                        TextField(
                            value = value,
                            onValueChange = { value = it.filter(Char::isDigit) },
                            enabled = !(isSubtaskModeEnabled && isSubtaskLocked),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = EditFieldMinHeight),
                            placeholder = {
                                Text(
                                    text = if (isPendingMove) "Set for Focus" else "0",
                                    color = palette.textSub
                                )
                            },
                            colors = textFieldColors(palette),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = EditControlShape,
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CompactEditLabel(text = "Deadline", palette = palette)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = EditFieldMinHeight)
                                .clip(EditControlShape)
                                .background(palette.background)
                                .border(1.dp, palette.line, EditControlShape)
                                .clickable {
                                    val initialDate = dueEpochDay
                                        ?.let(LocalDate::ofEpochDay)
                                        ?: LocalDate.now()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            dueEpochDay = LocalDate.of(year, month + 1, dayOfMonth).toEpochDay()
                                        },
                                        initialDate.year,
                                        initialDate.monthValue - 1,
                                        initialDate.dayOfMonth
                                    ).show()
                                }
                                .padding(horizontal = 15.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dueEpochDay?.let(::formatCompactDueDate) ?: "No date",
                                    color = dueEpochDay?.let { dueBadgeFor(it, palette).color } ?: palette.textMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                dueEpochDay?.let {
                                    Text(
                                        text = "Clear",
                                        color = palette.textSub,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { dueEpochDay = null }
                                    )
                                }
                            }
                        }
                    }
                }
                if (isSubtaskModeEnabled) {
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
                                    difficulty = draft.difficulty,
                                    completed = existingSubtask?.isCompleted == true,
                                    palette = palette,
                                    locked = isSubtaskLocked,
                                    onCycleDifficulty = {
                                        if (!isSubtaskLocked) {
                                            subtaskDrafts = subtaskDrafts.toMutableList().apply {
                                                this[index] = draft.copy(
                                                    difficulty = nextSubtaskDifficulty(draft.difficulty)
                                                )
                                            }
                                        }
                                    },
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
                                text = "S / M / L changes each subtask's share. Total reward stays fixed.",
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
                            text = if (isPendingMove) "Set value for Focus" else "0",
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
                            dueEpochDay,
                            subtaskDrafts.takeIf { isTaskEditor && isSubtaskModeEnabled }
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
    difficulty: Int,
    completed: Boolean,
    palette: KudoPalette,
    locked: Boolean,
    onCycleDifficulty: () -> Unit,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (completed) palette.textSub else palette.textMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (completed) TextDecoration.LineThrough else null,
                maxLines = 1
            )
        }
        DifficultyChip(
            difficulty = difficulty,
            palette = palette,
            enabled = !locked,
            onClick = onCycleDifficulty
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
private fun DifficultyChip(
    difficulty: Int,
    palette: KudoPalette,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val label = subtaskDifficultyLabel(difficulty)
    Box(
        modifier = Modifier
            .size(EditChipSize)
            .clip(CircleShape)
            .background(if (enabled) palette.background else palette.card)
            .border(1.dp, palette.line, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) palette.textMain else palette.textSub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
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
