package com.kudo.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.core.model.KudoLogEntry
import com.kudo.app.ui.viewmodel.KudoUiState
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun LogPage(
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
    val valueColor = if (log.kind == com.kudo.app.core.model.KudoLogKind.Store) palette.orange else palette.green
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
                text = formatLogTime(log.timestamp),
                color = palette.textSub,
                fontSize = 11.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (log.coins > 0) "+${log.coins}" else log.coins.toString(),
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
internal fun UndoBanner(
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

private fun buildTrend(logs: List<KudoLogEntry>, positive: Boolean): List<Float> {
    val today = LocalDate.now(AppZoneId)
    val windowStart = today.minusDays(13)
    val buckets = FloatArray(14)

    logs.forEach { log ->
        val amount = when {
            positive && log.coins > 0 -> log.coins.toFloat()
            !positive && log.coins < 0 -> (-log.coins).toFloat()
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

private fun formatLogTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(AppZoneId)
        .format(LogTimeFormatter)
}

private val LogTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
