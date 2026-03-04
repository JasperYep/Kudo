package com.kudo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.ui.theme.*

@Composable
fun Header(
    coins: Int,
    level: Int,
    xpProgress: Float,
    multiplier: Float
) {
    val isDark = false // TODO: Get from theme
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) DarkCard else LightCard)
            .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        // Top row: Level + Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level tag + XP bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LVL $level",
                    fontSize = 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDark) DarkGold else LightGold,
                    modifier = Modifier
                        .background(
                            if (isDark) DarkGold.copy(alpha = 0.15f) else LightGold.copy(alpha = 0.12f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                
                // XP bar
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(3.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(xpProgress)
                            .background(if (isDark) DarkGold else LightGold, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    )
                }
            }
            
            // Settings button
            androidx.compose.material3.IconButton(onClick = { /* TODO */ }) {
                androidx.compose.material3.Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = if (isDark) Color.Gray else Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Main row: Coins + Multiplier
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Coins
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDark) DarkGreen else LightGreen
                )
                Text(
                    text = coins.toString(),
                    fontSize = 34.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDark) DarkTextMain else LightTextMain,
                    fontSize = 34.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            
            // Multiplier
            Text(
                text = "${String.format("%.1f", multiplier)}x",
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isDark) Color.Gray else Color.Gray,
                modifier = Modifier
                    .background(
                        if (isDark) DarkCard else LightCard,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                        androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
