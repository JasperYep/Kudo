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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kudo.app.core.model.KudoNote
import com.kudo.app.ui.viewmodel.KudoUiState
import kotlinx.coroutines.delay

@Composable
internal fun NotebookOverlay(
    uiState: KudoUiState,
    palette: KudoPalette,
    onCreate: () -> Unit,
    onDelete: (Long) -> Unit,
    onSelect: (Long) -> Unit,
    onTitleChange: (Long, String) -> Unit,
    onContentChange: (Long, String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(enabled = uiState.view.isNotebookVisible) { onClose() }

    var presented by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    val currentNote = uiState.data.notes.firstOrNull { it.id == uiState.view.selectedNotebookNoteId }
        ?: uiState.data.notes.firstOrNull()
    var titleDraft by rememberSaveable(currentNote?.id ?: -1L, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentNote?.title.orEmpty()))
    }
    var contentDraft by rememberSaveable(currentNote?.id ?: -1L, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentNote?.content.orEmpty()))
    }
    val persistedTitle by rememberUpdatedState(currentNote?.title.orEmpty())
    val persistedContent by rememberUpdatedState(currentNote?.content.orEmpty())
    val latestTitleDraft by rememberUpdatedState(titleDraft)
    val latestContentDraft by rememberUpdatedState(contentDraft)

    LaunchedEffect(Unit) { presented = true }
    LaunchedEffect(currentNote?.id, titleDraft.text) {
        val noteId = currentNote?.id ?: return@LaunchedEffect
        delay(180)
        if (titleDraft.text != persistedTitle) {
            onTitleChange(noteId, titleDraft.text)
        }
    }
    LaunchedEffect(currentNote?.id, contentDraft.text) {
        val noteId = currentNote?.id ?: return@LaunchedEffect
        delay(180)
        if (contentDraft.text != persistedContent) {
            onContentChange(noteId, contentDraft.text)
        }
    }
    DisposableEffect(currentNote?.id) {
        onDispose {
            val noteId = currentNote?.id ?: return@onDispose
            if (latestTitleDraft.text != persistedTitle) {
                onTitleChange(noteId, latestTitleDraft.text)
            }
            if (latestContentDraft.text != persistedContent) {
                onContentChange(noteId, latestContentDraft.text)
            }
        }
    }

    val cardScale by animateFloatAsState(
        targetValue = if (presented) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
        label = "notebookCardScale"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (presented) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 560f),
        label = "notebookScrimAlpha"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (presented) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 620f),
        label = "notebookCardAlpha"
    )
    val cardInteractionSource = remember { MutableInteractionSource() }
    val bodyScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onClose,
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
                    onClick = onClose
                )
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.72f)
                    .widthIn(max = 620.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    }
                    .clickable(
                        interactionSource = cardInteractionSource,
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = if (currentNote != null) 92.dp else 48.dp),
                            contentPadding = PaddingValues(end = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items = uiState.data.notes, key = { it.id }) { note ->
                                NotebookPreviewCard(
                                    note = note,
                                    selected = note.id == currentNote?.id,
                                    palette = palette,
                                    onClick = { onSelect(note.id) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentNote != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(palette.background)
                                        .clickable { pendingDeleteId = currentNote.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "−",
                                        color = palette.textMain,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(palette.background)
                                    .clickable(onClick = onCreate),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = palette.textMain,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (currentNote != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (titleDraft.text.isBlank()) {
                                Text(
                                    text = "Title",
                                    color = palette.textSub.copy(alpha = 0.65f),
                                    fontSize = 24.sp
                                )
                            }
                            BasicTextField(
                                value = titleDraft,
                                onValueChange = { titleDraft = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = palette.textMain,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                cursorBrush = SolidColor(palette.textMain),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(bodyScrollState)
                        ) {
                            if (contentDraft.text.isBlank()) {
                                Text(
                                    text = "Write anything...",
                                    color = palette.textSub.copy(alpha = 0.7f),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                            BasicTextField(
                                value = contentDraft,
                                onValueChange = { contentDraft = it },
                                textStyle = TextStyle(
                                    color = palette.textMain,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                cursorBrush = SolidColor(palette.green),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { noteId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(text = "Delete note?") },
            text = { Text(text = "This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId = null
                        onDelete(noteId)
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotebookPreviewCard(
    note: KudoNote,
    selected: Boolean,
    palette: KudoPalette,
    onClick: () -> Unit
) {
    val background = if (selected) palette.background else palette.background.copy(alpha = 0.34f)
    val outline = if (selected) palette.line.copy(alpha = 0.75f) else Color.Transparent
    Box(
        modifier = Modifier
            .width(136.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(background)
            .border(1.dp, outline, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = note.displayTitle,
            color = palette.textMain,
            fontSize = 13.sp,
            maxLines = 1,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal,
            overflow = TextOverflow.Ellipsis
        )
    }
}
