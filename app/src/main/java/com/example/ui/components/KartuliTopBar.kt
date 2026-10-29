package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProgress
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianWinePrimary

@Composable
fun KartuliTopBar(
    userProgress: UserProgress,
    onHeartsClick: () -> Unit,
    onStreakClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak
            StatBadge(
                icon = Icons.Default.LocalFireDepartment,
                value = "${userProgress.streakDays}",
                color = Color(0xFFF97316),
                testTag = "badge_streak",
                onClick = onStreakClick
            )

            // Total XP
            StatBadge(
                icon = Icons.Default.OfflineBolt,
                value = "${userProgress.totalXp}",
                color = AmberGold,
                testTag = "badge_xp"
            )

            // Coins
            StatBadge(
                icon = Icons.Default.Paid,
                value = "${userProgress.coins}",
                color = AmberGold,
                testTag = "badge_coins"
            )

            // Hearts
            StatBadge(
                icon = Icons.Default.Favorite,
                value = "${userProgress.hearts}/${userProgress.maxHearts}",
                color = if (userProgress.hearts > 1) Color(0xFFEF4444) else Color(0xFFDC2626),
                showPlus = userProgress.hearts < userProgress.maxHearts,
                testTag = "badge_hearts",
                onClick = onHeartsClick
            )
        }
    }
}

@Composable
fun StatBadge(
    icon: ImageVector,
    value: String,
    color: Color,
    showPlus: Boolean = false,
    testTag: String,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .then(clickableModifier)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (showPlus) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(CaucasusEmerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Пополнить",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
