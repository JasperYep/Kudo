package com.kudo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.ui.theme.*

@Composable
fun Dashboard(
    onAddTask: (String, Int, String) -> Unit,
    mode: String,
    onModeToggle: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var title by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                if (isDark) DarkCard else LightCard,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isDark) DarkLine else LightLine,
                RoundedCornerShape(14.dp)
            )
            .padding(4.dp)
    ) {
        // Input row
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { 
                Text(
                    "Add new item…",
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            singleLine = true
        )
        
        // Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Value input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "$",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.Gray
                )
                TextField(
                    value = value,
                    onValueChange = { value = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("10") },
                    modifier = Modifier.width(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = if (mode == "focus") {
                            if (isDark) DarkGreen else LightGreen
                        } else {
                            if (isDark) DarkOrange else LightOrange
                        }
                    )
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(if (isDark) DarkLine else LightLine)
            )
            
            // Mode toggle
            Text(
                text = if (mode == "focus") "ONCE" else "STORE",
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (mode == "focus") {
                    if (isDark) DarkGreen else LightGreen
                } else {
                    if (isDark) DarkOrange else LightOrange
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onModeToggle() }
                    .padding(horizontal = 8.dp)
            )
            
            // Add button
            androidx.compose.material3.IconButton(
                onClick = {
                    if (title.isNotBlank() && value.isNotBlank()) {
                        onAddTask(title, value.toInt(), mode)
                        title = ""
                        value = ""
                    }
                }
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "Add",
                    tint = if (isDark) DarkGreen else LightGreen
                )
            }
        }
    }
}
