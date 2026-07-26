package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppTab
import com.example.ui.LanguageMode
import com.example.ui.SindhHistoryViewModel
import com.example.ui.components.*
import com.example.ui.theme.SindhThroughTheAgesTheme
import com.example.ui.theme.SindhiCrimson
import com.example.ui.theme.SindhiDeepIndigo
import com.example.ui.theme.SindhiSurfaceVariant
import com.example.ui.theme.SindhiTextSecondary
import com.example.ui.theme.SindhiTileBlue
import com.example.ui.theme.SindhiTileBlueDark
import com.example.ui.theme.SindhiTileBlueLight

class MainActivity : ComponentActivity() {

    private val viewModel: SindhHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SindhThroughTheAgesTheme {
                val uiState by viewModel.uiState.collectAsState()
                val eras = viewModel.getEras()
                val selectedEra = eras.find { it.id == uiState.selectedEraId } ?: eras.first()
                val artifacts = viewModel.getArtifacts()
                val landmarks = viewModel.getLandmarks()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        PersistentSindhTopBar(
                            knowledgePoints = uiState.userProgress.knowledgePoints,
                            isSoundscapeEnabled = uiState.isSoundscapeEnabled,
                            languageMode = uiState.languageMode,
                            onToggleSoundscape = { viewModel.toggleSoundscape() },
                            onToggleLanguage = { viewModel.toggleLanguageMode() },
                            onOpenProfile = { viewModel.openProfileModal() }
                        )
                    },
                    bottomBar = {
                        SindhBottomNavigationBar(
                            currentTab = uiState.currentTab,
                            languageMode = uiState.languageMode,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Soundscape Banner Indicator when ON
                        AnimatedVisibility(
                            visible = uiState.isSoundscapeEnabled,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("soundscape_active_banner"),
                                shape = RoundedCornerShape(12.dp),
                                color = SindhiTileBlueLight,
                                border = BorderStroke(1.dp, SindhiTileBlue.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Soundscape Playing",
                                        tint = SindhiTileBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.languageMode == LanguageMode.ENGLISH) 
                                            "♫ Ambient Audio Soundscape Playing (Folk Alghoza & Surindo)" 
                                        else 
                                            "♫ Awaz-e-Sindh Jari Aahi (Sufi & Folk Alghoza)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SindhiTileBlueDark
                                    )
                                }
                            }
                        }

                        // Global Knowledge Points Progress Bar
                        KnowledgeProgressBar(
                            progress = uiState.userProgress,
                            levelUpMessage = uiState.levelUpMessage,
                            onDismissLevelUp = { viewModel.dismissLevelUpMessage() }
                        )

                        // Tab View Content
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (uiState.currentTab) {
                                AppTab.TIMELINE -> {
                                    TimelineView(
                                        eras = eras,
                                        progress = uiState.userProgress,
                                        selectedEraId = uiState.selectedEraId,
                                        onSelectEra = { viewModel.selectEra(it) },
                                        onStartStoryMode = { viewModel.startStoryModeForEra(it) }
                                    )
                                }

                                AppTab.STORY_MODE -> {
                                    StoryModeView(
                                        selectedEra = selectedEra,
                                        activeNode = uiState.activeStoryNode,
                                        lastConsequence = uiState.lastChoiceConsequence,
                                        selectedPerspective = uiState.storyPerspective,
                                        onSelectPerspective = { viewModel.setStoryPerspective(it) },
                                        onMakeChoice = { viewModel.makeStoryDecision(it) },
                                        onResetStory = { viewModel.resetStoryForCurrentEra() },
                                        onViewArtifact = { artifactId ->
                                            val found = artifacts.find { it.id == artifactId }
                                            if (found != null) {
                                                viewModel.openArtifactDetails(found)
                                            }
                                        }
                                    )
                                }

                                AppTab.ARTIFACT_CODEX -> {
                                    ArtifactCodexView(
                                        artifacts = artifacts,
                                        progress = uiState.userProgress,
                                        onSelectArtifact = { viewModel.openArtifactDetails(it) }
                                    )
                                }

                                AppTab.SINDH_MAP -> {
                                    SindhMapView(
                                        landmarks = landmarks,
                                        selectedLandmark = uiState.selectedLandmark,
                                        onSelectLandmark = { viewModel.selectLandmark(it) },
                                        onStartStoryMode = { viewModel.startStoryModeForEra(it) }
                                    )
                                }

                                AppTab.ACHIEVEMENTS -> {
                                    AchievementsView(
                                        progress = uiState.userProgress,
                                        artifacts = artifacts,
                                        onResetProgress = { viewModel.resetAllProgress() }
                                    )
                                }
                            }
                        }
                    }

                    // Artifact Pop-Up Micro-Lesson Dialog
                    if (uiState.showArtifactDialog && uiState.selectedArtifact != null) {
                        ArtifactDialog(
                            artifact = uiState.selectedArtifact!!,
                            isAudioNarrating = uiState.isAudioNarrating,
                            quizFeedback = uiState.quizFeedback,
                            onDismiss = { viewModel.closeArtifactDialog() },
                            onToggleAudio = { viewModel.toggleAudioNarration() },
                            onSubmitQuiz = { optionIndex ->
                                viewModel.submitQuizAnswer(uiState.selectedArtifact!!, optionIndex)
                            }
                        )
                    }

                    // Profile & Rewards Modal
                    if (uiState.showProfileModal) {
                        ProfileRewardModal(
                            progress = uiState.userProgress,
                            onDismiss = { viewModel.closeProfileModal() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersistentSindhTopBar(
    knowledgePoints: Int,
    isSoundscapeEnabled: Boolean,
    languageMode: LanguageMode,
    onToggleSoundscape: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenProfile: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.ENGLISH) "Sindh Ages" else "Sindh Ji Tareekh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SindhiDeepIndigo,
                    maxLines = 1
                )
                Text(
                    text = if (languageMode == LanguageMode.ENGLISH) "Heritage & History" else "Aagahi aeen Virasat",
                    style = MaterialTheme.typography.labelSmall,
                    color = SindhiCrimson
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Language Toggle Pill (EN / Roman Sindhi)
                Surface(
                    shape = CircleShape,
                    color = if (languageMode == LanguageMode.ROMAN_SINDHI_URDU) SindhiCrimson else SindhiSurfaceVariant,
                    modifier = Modifier.testTag("language_toggle_btn")
                ) {
                    TextButton(
                        onClick = onToggleLanguage,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.ENGLISH) "EN" else "SD/UR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (languageMode == LanguageMode.ROMAN_SINDHI_URDU) Color.White else SindhiDeepIndigo
                        )
                    }
                }

                // Audio Soundscape Toggle Button
                IconButton(
                    onClick = onToggleSoundscape,
                    modifier = Modifier.testTag("soundscape_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isSoundscapeEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Audio Soundscape Toggle",
                        tint = if (isSoundscapeEnabled) SindhiTileBlue else SindhiTextSecondary
                    )
                }

                // Knowledge Points & Profile Badge Button
                Surface(
                    onClick = onOpenProfile,
                    shape = CircleShape,
                    color = SindhiTileBlueLight,
                    border = BorderStroke(1.dp, SindhiTileBlue),
                    modifier = Modifier.testTag("topbar_kp_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile & Badges",
                            tint = SindhiTileBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.ENGLISH) "$knowledgePoints KP" else "$knowledgePoints Ilm",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SindhiTileBlueDark
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = SindhiDeepIndigo
        )
    )
}

@Composable
fun SindhBottomNavigationBar(
    currentTab: AppTab,
    languageMode: LanguageMode,
    onTabSelected: (AppTab) -> Unit
) {
    val isEn = languageMode == LanguageMode.ENGLISH

    NavigationBar(
        modifier = Modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.TIMELINE,
            onClick = { onTabSelected(AppTab.TIMELINE) },
            icon = { Icon(imageVector = Icons.Default.Timeline, contentDescription = "Timeline") },
            label = { Text(if (isEn) "Timeline" else "Safar") },
            modifier = Modifier.testTag("tab_timeline")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.STORY_MODE,
            onClick = { onTabSelected(AppTab.STORY_MODE) },
            icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Story Mode") },
            label = { Text(if (isEn) "Story" else "Qissay") },
            modifier = Modifier.testTag("tab_story_mode")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.ARTIFACT_CODEX,
            onClick = { onTabSelected(AppTab.ARTIFACT_CODEX) },
            icon = { Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = "Codex") },
            label = { Text(if (isEn) "Codex" else "Virasat") },
            modifier = Modifier.testTag("tab_artifact_codex")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.SINDH_MAP,
            onClick = { onTabSelected(AppTab.SINDH_MAP) },
            icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Map") },
            label = { Text(if (isEn) "Map" else "Naqsha") },
            modifier = Modifier.testTag("tab_sindh_map")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.ACHIEVEMENTS,
            onClick = { onTabSelected(AppTab.ACHIEVEMENTS) },
            icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Badges") },
            label = { Text(if (isEn) "Badges" else "Inaam") },
            modifier = Modifier.testTag("tab_achievements")
        )
    }
}
