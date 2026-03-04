package com.kudo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.ui.theme.*

@Composable
fun BottomNavigation(
    currentView: String,
    onNavigate: (String) -> Unit
) {
    val isDark = false // TODO: Get from theme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(
                if (isDark) DarkCard else LightCard,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = "📋",
            label = "Tasks",
            isSelected = currentView == "tasks",
            onClick = { onNavigate("tasks") }
        )
        
        NavItem(
            icon = "🎁",
            label = "Store",
            isSelected = currentView == "store",
            onClick = { onNavigate("store") }
        )
        
        NavItem(
            icon = "📊",
            label = "Log",
            isSelected = currentView == "log",
            onClick = { onNavigate("log") }
        )
    }
}

@Composable
fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = false // TODO: Get from theme
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) {
                if (isDark) DarkGreen else LightGreen
            } else {
                Color.Gray
            }
        )
    }
}
