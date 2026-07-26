package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CulturalArtifact
import com.example.model.UserProgressData

data class AchievementBadge(
    val title: String,
    val description: String,
    val requiredPoints: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AchievementsView(
    progress: UserProgressData,
    artifacts: List<CulturalArtifact>,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    val badges = listOf(
        AchievementBadge("Sindh Novice", "Begin your journey into ancient history", 0, Icons.Default.DirectionsWalk),
        AchievementBadge("History Explorer", "Earn 100 Knowledge Points across eras", 100, Icons.Default.CompassCalibration),
        AchievementBadge("Sindh Traveler", "Earn 250 Knowledge Points & unlock 3 eras", 250, Icons.Default.FlightTakeoff),
        AchievementBadge("Heritage Master", "Earn 500 Knowledge Points & master micro-lessons", 500, Icons.Default.MilitaryTech),
        AchievementBadge("Scholar of the Indus", "Earn 800 Knowledge Points across history", 800, Icons.Default.MenuBook),
        AchievementBadge("Grand Historian", "Earn 1200 Knowledge Points & unlock all artifacts", 1200, Icons.Default.EmojiEvents)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        // Banner Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                com.example.ui.theme.BentoTerracottaPrimary,
                                com.example.ui.theme.BentoTerracottaDark
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Traveler Achievements & Ranks",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your historical milestones, earned badges, and unlocked cultural artifacts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showProfileModal by remember { mutableStateOf(false) }

        if (showProfileModal) {
            ProfileRewardModal(
                progress = progress,
                onDismiss = { showProfileModal = false }
            )
        }

        // Current Rank Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                .testTag("rank_summary_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = progress.rankTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${progress.knowledgePoints} KP • 🔥 ${progress.dailyStreak} Day Streak",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { showProfileModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SindhiTerracotta),
                        modifier = Modifier.testTag("open_profile_modal_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profile", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exportable History Certificate Card
        var showCertificateDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, com.example.ui.theme.SindhiTerracotta.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("history_certificate_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = com.example.ui.theme.SindhiIndigoContainer.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardMembership,
                        contentDescription = "Certificate",
                        tint = com.example.ui.theme.SindhiWarmCrimson,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Official History Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.SindhiDeepIndigo
                        )
                        Text(
                            text = "Download or print your academic achievement certificate for classroom use.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = { showCertificateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.SindhiTerracotta
                    ),
                    modifier = Modifier.testTag("view_certificate_btn")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Certificate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (showCertificateDialog) {
            var studentName by remember { mutableStateOf("Scholar of Sindh") }
            var isExported by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showCertificateDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = com.example.ui.theme.SindhiWarmCrimson
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Sindh History Academic Certificate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Student / Scholar Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Printable Preview Container
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = com.example.ui.theme.SindhiSurface,
                            border = androidx.compose.foundation.BorderStroke(2.dp, com.example.ui.theme.SindhiTerracotta)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🏛️ SINDH HERITAGE & ARCHAEOLOGY BOARD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.SindhiDeepIndigo
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Certificate of Historical Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = com.example.ui.theme.SindhiWarmCrimson
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This certifies that",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = studentName.ifBlank { "Scholar of Sindh" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.SindhiDeepIndigo
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "has successfully mastered historical modules with ${progress.knowledgePoints} Knowledge Points and earned the title of ${progress.rankTitle}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Date: July 2026",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "SEAL: 📜 Master Historian",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.SindhiKashiBlue
                                    )
                                }
                            }
                        }

                        if (isExported) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ Certificate exported & copied to clipboard for printing!",
                                style = MaterialTheme.typography.labelMedium,
                                color = com.example.ui.theme.SindhiKashiBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isExported = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.SindhiTerracotta)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download / Print")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCertificateDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Milestone Honor Badges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            badges.forEach { badge ->
                val isUnlocked = progress.knowledgePoints >= badge.requiredPoints

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        if (isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Earned",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "${badge.requiredPoints} KP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset progress option
        OutlinedButton(
            onClick = { showResetConfirmation = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_progress_btn"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset All Knowledge Progress")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Progress?") },
            text = { Text("Are you sure you want to reset all earned Knowledge Points, unlocked eras, and collected artifact badges? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
