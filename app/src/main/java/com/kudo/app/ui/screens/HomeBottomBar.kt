package com.kudo.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.ui.viewmodel.KudoUiState
import com.kudo.app.ui.viewmodel.KudoView

@Composable
internal fun BottomTabs(
    uiState: KudoUiState,
    palette: KudoPalette,
    onViewSelected: (KudoView) -> Unit
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
                selected = uiState.view.currentView == KudoView.Tasks,
                label = "Tasks",
                icon = Icons.Rounded.CheckCircle,
                selectedColor = palette.green,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoView.Tasks) }
            )
            BottomTabItem(
                modifier = Modifier.weight(1f),
                selected = uiState.view.currentView == KudoView.Store,
                label = "Store",
                icon = Icons.Rounded.CardGiftcard,
                selectedColor = palette.orange,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoView.Store) }
            )
            BottomTabItem(
                modifier = Modifier.weight(1f),
                selected = uiState.view.currentView == KudoView.Log,
                label = "Log",
                icon = Icons.Rounded.BarChart,
                selectedColor = palette.textMain,
                mutedColor = palette.textSub,
                onClick = { onViewSelected(KudoView.Log) }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    icon: ImageVector,
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
