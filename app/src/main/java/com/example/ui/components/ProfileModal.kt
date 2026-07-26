package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.UserBadge
import com.example.model.UserProgressData
import com.example.ui.theme.*

@Composable
fun ProfileRewardModal(
    progress: UserProgressData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMilestones = 23f
    val currentAchieved = (progress.unlockedEraIds.size + progress.completedNodeIds.size + progress.unlockedArtifactIds.size + progress.answeredQuizIds.size).toFloat()
    val completionPercentage = ((currentAchieved / totalMilestones) * 100).coerceIn(0f, 100f).toInt()

    val badgesList = listOf(
        UserBadge(
            id = "badge_indus_explorer",
            title = "Indus Explorer",
            description = "Unlocked the ancient urban grid and Great Bath of Mohenjo-Daro.",
            category = "Archaeology",
            isUnlocked = progress.unlockedBadgeIds.contains("badge_indus_explorer")
        ),
        UserBadge(
            id = "badge_sufi_scholar",
            title = "Sufi Scholar",
            description = "Discovered Sufi poetry, Shah Jo Risalo and traditional Alghoza music.",
            category = "Literature & Art",
            isUnlocked = progress.unlockedBadgeIds.contains("badge_sufi_scholar")
        ),
        UserBadge(
            id = "badge_fortress_master",
            title = "Fortress Master",
            description = "Explored Pacco Qillo citadel, Ranikot walls and Talpur defense heritage.",
            category = "Fortifications",
            isUnlocked = progress.unlockedBadgeIds.contains("badge_fortress_master")
        ),
        UserBadge(
            id = "badge_silk_road_merchant",
            title = "Silk Road Merchant",
            description = "Mastered standardized chert weights and Banbhore maritime trade routes.",
            category = "Commerce",
            isUnlocked = progress.unlockedBadgeIds.contains("badge_silk_road_merchant")
        ),
        UserBadge(
            id = "badge_master_historian",
            title = "Master Historian",
            description = "Accumulated 350+ Knowledge Points and mastered multiple historical choices.",
            category = "Academic Honor",
            isUnlocked = progress.unlockedBadgeIds.contains("badge_master_historian")
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .testTag("profile_reward_modal"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.5.dp, SindhiTerracotta.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SindhiTerracotta.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile Icon",
                                    tint = SindhiTerracotta,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scholar Profile & Rewards",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SindhiDeepIndigo
                            )
                            Text(
                                text = "Local Storage Synchronized 💾",
                                style = MaterialTheme.typography.labelSmall,
                                color = SindhiKashiBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_profile_modal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Modal",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Summary Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SindhiIndigoContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = progress.rankTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SindhiDeepIndigo
                                )
                                Text(
                                    text = "Academic Status in Sindh History",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SindhiWarmCrimson,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Whatshot,
                                        contentDescription = "Streak",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${progress.dailyStreak} Day Streak",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Row (KP & Badges Count)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, SindhiKashiBlue.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${progress.knowledgePoints} KP",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SindhiKashiBlue
                                    )
                                    Text(
                                        text = "Knowledge Points",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, SindhiTerracotta.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${badgesList.count { it.isUnlocked }}/${badgesList.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SindhiTerracotta
                                    )
                                    Text(
                                        text = "Badges Unlocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overall Completion Percentage Progress Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completion Percentage",
                                    tint = SindhiKashiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Overall History Completion",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SindhiDeepIndigo
                                )
                            }

                            Text(
                                text = "$completionPercentage%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SindhiTerracotta
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { completionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = SindhiTerracotta,
                            trackColor = SindhiTerracotta.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Completion Metrics Breakdown Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🏛️ ${progress.unlockedEraIds.size}/5 Eras",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "📜 ${progress.unlockedArtifactIds.size}/6 Artifacts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "🧠 ${progress.answeredQuizIds.size}/6 Quizzes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Honor Badges & Achievements:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SindhiDeepIndigo
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable List of Badges
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(badgesList) { badge ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) SindhiTerracotta.copy(alpha = 0.5f) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (badge.isUnlocked) SindhiTerracotta else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (badge.id) {
                                                "badge_indus_explorer" -> Icons.Default.Explore
                                                "badge_sufi_scholar" -> Icons.Default.AutoAwesome
                                                "badge_fortress_master" -> Icons.Default.Castle
                                                "badge_silk_road_merchant" -> Icons.Default.LocalShipping
                                                else -> Icons.Default.Verified
                                            },
                                            contentDescription = badge.title,
                                            tint = if (badge.isUnlocked) Color.White else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = badge.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (badge.isUnlocked) SindhiDeepIndigo else Color.Gray
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (badge.isUnlocked) SindhiKashiBlue.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (badge.isUnlocked) "Unlocked ✅" else "Locked 🔒",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (badge.isUnlocked) SindhiKashiBlue else Color.Gray,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = badge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SindhiTerracotta)
                ) {
                    Text(text = "Close Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
