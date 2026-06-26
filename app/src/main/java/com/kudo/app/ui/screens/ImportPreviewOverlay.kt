package com.kudo.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kudo.app.core.model.KudoTaskImportDraft
import com.kudo.app.ui.viewmodel.KudoUiState

@Composable
internal fun ImportPreviewOverlay(
    uiState: KudoUiState,
    palette: KudoPalette,
    onUpdateDraft: (Int, String, Int) -> Unit,
    onDeleteDraft: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = uiState.isImportPreviewVisible) { onDismiss() }

    var presented by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { presented = true }

    val cardScale by animateFloatAsState(
        targetValue = if (presented) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
        label = "importCardScale"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (presented) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 560f),
        label = "importScrimAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f * scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.75f)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    },
                colors = CardDefaults.cardColors(containerColor = palette.card),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Import Preview",
                            color = palette.textMain,
                            fontSize = 18.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Cancel",
                                color = palette.textSub,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${uiState.importPreviewDrafts.size} tasks to import",
                        color = palette.textSub,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Task list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.importPreviewDrafts.forEachIndexed { index, draft ->
                            ImportDraftRow(
                                draft = draft,
                                palette = palette,
                                onTitleChange = { title ->
                                    onUpdateDraft(index, title, draft.value)
                                },
                                onValueChange = { value ->
                                    onUpdateDraft(index, draft.title, value)
                                },
                                onDelete = { onDeleteDraft(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add button
                    TextButton(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.textMain),
                    ) {
                        Text(
                            text = "Add ${uiState.importPreviewDrafts.size} Tasks",
                            color = palette.background,
                            fontSize = 15.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportDraftRow(
    draft: KudoTaskImportDraft,
    palette: KudoPalette,
    onTitleChange: (String) -> Unit,
    onValueChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var titleText by rememberSaveable(draft.hashCode()) { mutableStateOf(draft.title) }
    var valueText by rememberSaveable(draft.hashCode()) { mutableStateOf(draft.value.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete button
        TextButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "×",
                color = palette.orange,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        // Title
        BasicTextField(
            value = titleText,
            onValueChange = {
                titleText = it
                onTitleChange(it)
            },
            textStyle = TextStyle(
                color = palette.textMain,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            cursorBrush = SolidColor(palette.textMain),
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "+$",
                color = palette.green,
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            BasicTextField(
                value = valueText,
                onValueChange = { newValue ->
                    val filtered = newValue.filter(Char::isDigit)
                    if (filtered.isEmpty() || filtered.toIntOrNull() != null) {
                        valueText = filtered
                        filtered.toIntOrNull()?.let { onValueChange(it) }
                    }
                },
                textStyle = TextStyle(
                    color = palette.green,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                ),
                cursorBrush = SolidColor(palette.green),
                modifier = Modifier.width(40.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
