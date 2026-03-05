package com.kudo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.domain.model.Task
import com.kudo.app.ui.theme.*

@Composable
fun TaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = false // TODO: Get from theme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                if (isDark) DarkCard else LightCard,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isDark) DarkLine else LightLine,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        2.dp,
                        if (task.isCompleted) {
                            if (isDark) DarkGreen else LightGreen
                        } else {
                            Color.Gray.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (task.isCompleted) {
                            if (isDark) DarkGreen else LightGreen
                        } else {
                            Color.Transparent
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            
            // Task info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    color = if (isDark) DarkTextMain else LightTextMain,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+$${task.value}",
                    fontSize = 12.sp,
                    color = if (isDark) DarkGreen else LightGreen
                )
            }
        }
        
        // Delete button
        Text(
            text = "🗑",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { onDelete() }
                .padding(8.dp)
        )
    }
}

@Composable
fun HabitItem(
    habit: Task,
    onCharge: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = false // TODO: Get from theme
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                if (isDark) DarkCard else LightCard,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isDark) DarkLine else LightLine,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    fontSize = 14.sp,
                    color = if (isDark) DarkTextMain else LightTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+$${habit.value}",
                        fontSize = 12.sp,
                        color = if (isDark) DarkGold else LightGold
                    )
                    if (habit.habitCount > 0) {
                        Text(
                            text = "x${habit.habitCount}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Charge button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isDark) DarkGoldBg else LightGoldBg,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onCharge() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 18.sp
                    )
                }
                
                // Delete button
                Text(
                    text = "🗑",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { onDelete() }
                        .padding(4.dp)
                )
            }
        }
    }
}
