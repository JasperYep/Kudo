package com.kudo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kudo.app.domain.model.StoreItem
import com.kudo.app.ui.theme.*

@Composable
fun StoreScreen(
    storeItems: List<StoreItem>,
    coins: Int,
    onPurchase: (StoreItem) -> Unit,
    onAddItem: () -> Unit
) {
    val isDark = false // TODO
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎁 奖励商店",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isDark) DarkTextMain else LightTextMain
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    fontSize = 18.sp
                )
                Text(
                    text = coins.toString(),
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDark) DarkGreen else LightGreen
                )
            }
        }
        
        // Store items grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(storeItems) { item ->
                StoreItemCard(
                    item = item,
                    coins = coins,
                    onPurchase = { onPurchase(item) }
                )
            }
            
            // Add new item button
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            2.dp,
                            if (isDark) DarkLine else LightLine,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onAddItem() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ 自定义奖励",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StoreItemCard(
    item: StoreItem,
    coins: Int,
    onPurchase: () -> Unit
) {
    val isDark = false
    val canAfford = coins >= item.cost
    
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                if (isDark) DarkCard else LightCard,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isDark) DarkLine else LightLine,
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = canAfford && !item.isPurchased) { onPurchase() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Icon
        Text(
            text = item.icon,
            fontSize = 40.sp
        )
        
        // Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isDark) DarkTextMain else LightTextMain
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    fontSize = 12.sp
                )
                Text(
                    text = item.cost.toString(),
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (canAfford) {
                        if (isDark) DarkGreen else LightGreen
                    } else {
                        Color.Gray
                    }
                )
            }
        }
        
        // Status
        if (item.isPurchased) {
            Text(
                text = "✓ 已兑换",
                fontSize = 12.sp,
                color = if (isDark) DarkGreen else LightGreen,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        } else if (canAfford) {
            Text(
                text = "点击兑换",
                fontSize = 12.sp,
                color = if (isDark) DarkGreen else LightGreen
            )
        } else {
            Text(
                text = "金币不足",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
