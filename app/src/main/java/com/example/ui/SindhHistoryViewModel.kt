package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SindhDataRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab {
    TIMELINE,
    STORY_MODE,
    ARTIFACT_CODEX,
    SINDH_MAP,
    ACHIEVEMENTS
}

enum class LanguageMode {
    ENGLISH,
    ROMAN_SINDHI_URDU
}

data class QuizFeedback(
    val quizId: String,
    val isCorrect: Boolean,
    val selectedIndex: Int,
    val rewardPoints: Int,
    val message: String
)

data class SindhUiState(
    val userProgress: UserProgressData = UserProgressData(),
    val currentTab: AppTab = AppTab.TIMELINE,
    val selectedEraId: String = "era_indus_valley",
    val activeStoryNode: StoryNode? = null,
    val lastChoiceConsequence: StoryChoice? = null,
    val selectedArtifact: CulturalArtifact? = null,
    val showArtifactDialog: Boolean = false,
    val selectedLandmark: Landmark? = null,
    val quizFeedback: QuizFeedback? = null,
    val levelUpMessage: String? = null,
    val isAudioNarrating: Boolean = false,
    val isSoundscapeEnabled: Boolean = false, // Soundscape off by default
    val soundscapeTrack: String = "Indus Valley Ancient Bazaar",
    val languageMode: LanguageMode = LanguageMode.ENGLISH,
    val storyPerspective: StoryPerspective = StoryPerspective.TRADER,
    val isAcademicModeEnabled: Boolean = false, // Primary Source Academic Drawer
    val activeThemeStyle: AppThemeStyle = AppThemeStyle.MODERN_SINDHI,
    val playingPronunciationTerm: PronunciationTerm? = null,
    val showProfileModal: Boolean = false
)

class SindhHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SindhDataRepository
    private val _uiState = MutableStateFlow(SindhUiState())
    val uiState: StateFlow<SindhUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SindhDataRepository(database.progressDao())

        // Start collecting user progress
        viewModelScope.launch {
            repository.userProgressFlow.collect { progress ->
                val prevRank = _uiState.value.userProgress.rankTitle
                val levelUp = if (prevRank.isNotBlank() && prevRank != progress.rankTitle) {
                    "🎉 Promoted to ${progress.rankTitle}!"
                } else {
                    null
                }

                _uiState.update { current ->
                    current.copy(
                        userProgress = progress,
                        levelUpMessage = levelUp ?: current.levelUpMessage
                    )
                }
            }
        }

        // Initialize default story node for Indus Valley
        loadStoryForEra("era_indus_valley")
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectEra(eraId: String) {
        _uiState.update { 
            it.copy(
                selectedEraId = eraId,
                lastChoiceConsequence = null
            ) 
        }
        loadStoryForEra(eraId)
    }

    fun startStoryModeForEra(eraId: String) {
        selectEra(eraId)
        selectTab(AppTab.STORY_MODE)
    }

    private fun loadStoryForEra(eraId: String) {
        val nodes = repository.getStoryNodesForEra(eraId)
        _uiState.update { current ->
            current.copy(
                activeStoryNode = nodes.firstOrNull(),
                lastChoiceConsequence = null
            )
        }
    }

    fun makeStoryDecision(choice: StoryChoice) {
        viewModelScope.launch {
            repository.addKnowledgePoints(
                points = choice.knowledgePointsReward,
                unlockArtifactId = choice.artifactUnlockedId,
                completeNodeId = choice.id
            )

            val currentEraId = _uiState.value.selectedEraId
            val nodes = repository.getStoryNodesForEra(currentEraId)
            val nextNode = if (choice.nextNodeId != null) {
                nodes.find { it.id == choice.nextNodeId }
            } else {
                nodes.firstOrNull()
            }

            _uiState.update { current ->
                current.copy(
                    lastChoiceConsequence = choice,
                    activeStoryNode = nextNode ?: current.activeStoryNode
                )
            }
        }
    }

    fun resetStoryForCurrentEra() {
        val eraId = _uiState.value.selectedEraId
        loadStoryForEra(eraId)
    }

    fun openArtifactDetails(artifact: CulturalArtifact) {
        _uiState.update { 
            it.copy(
                selectedArtifact = artifact,
                showArtifactDialog = true,
                quizFeedback = null
            ) 
        }
    }

    fun closeArtifactDialog() {
        _uiState.update { 
            it.copy(
                showArtifactDialog = false,
                selectedArtifact = null,
                quizFeedback = null
            ) 
        }
    }

    fun submitQuizAnswer(artifact: CulturalArtifact, selectedOptionIndex: Int) {
        val quiz = artifact.quizQuestion
        val isCorrect = selectedOptionIndex == quiz.correctAnswerIndex

        if (isCorrect) {
            viewModelScope.launch {
                repository.recordQuizCompleted(quiz.id, quiz.rewardPoints, artifact.id)
            }
            _uiState.update { current ->
                current.copy(
                    quizFeedback = QuizFeedback(
                        quizId = quiz.id,
                        isCorrect = true,
                        selectedIndex = selectedOptionIndex,
                        rewardPoints = quiz.rewardPoints,
                        message = "✨ Correct! +${quiz.rewardPoints} Knowledge Points added to your ledger."
                    )
                )
            }
        } else {
            _uiState.update { current ->
                current.copy(
                    quizFeedback = QuizFeedback(
                        quizId = quiz.id,
                        isCorrect = false,
                        selectedIndex = selectedOptionIndex,
                        rewardPoints = 0,
                        message = "❌ Not quite. ${quiz.explanation}"
                    )
                )
            }
        }
    }

    fun selectLandmark(landmark: Landmark?) {
        _uiState.update { it.copy(selectedLandmark = landmark) }
    }

    fun dismissLevelUpMessage() {
        _uiState.update { it.copy(levelUpMessage = null) }
    }

    fun toggleAudioNarration() {
        _uiState.update { it.copy(isAudioNarrating = !it.isAudioNarrating) }
    }

    fun toggleSoundscape() {
        _uiState.update { it.copy(isSoundscapeEnabled = !it.isSoundscapeEnabled) }
    }

    fun toggleLanguageMode() {
        _uiState.update {
            val nextMode = if (it.languageMode == LanguageMode.ENGLISH) {
                LanguageMode.ROMAN_SINDHI_URDU
            } else {
                LanguageMode.ENGLISH
            }
            it.copy(languageMode = nextMode)
        }
    }

    fun setStoryPerspective(perspective: StoryPerspective) {
        _uiState.update { it.copy(storyPerspective = perspective) }
    }

    fun toggleAcademicMode() {
        _uiState.update { it.copy(isAcademicModeEnabled = !it.isAcademicModeEnabled) }
    }

    fun selectSoundscapeTrack(trackName: String) {
        _uiState.update { it.copy(soundscapeTrack = trackName, isSoundscapeEnabled = true) }
    }

    fun selectThemeStyle(themeStyle: AppThemeStyle) {
        _uiState.update { it.copy(activeThemeStyle = themeStyle) }
    }

    fun playPronunciation(term: PronunciationTerm?) {
        _uiState.update { it.copy(playingPronunciationTerm = term) }
    }

    fun openProfileModal() {
        _uiState.update { it.copy(showProfileModal = true) }
    }

    fun closeProfileModal() {
        _uiState.update { it.copy(showProfileModal = false) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _uiState.update { 
                it.copy(
                    selectedEraId = "era_indus_valley",
                    lastChoiceConsequence = null,
                    levelUpMessage = "Progress reset to fresh explorer state."
                ) 
            }
            loadStoryForEra("era_indus_valley")
        }
    }

    // Helper queries
    fun getEras(): List<HistoricalEra> = repository.getAllEras()
    fun getArtifacts(): List<CulturalArtifact> = repository.getAllCulturalArtifacts()
    fun getLandmarks(): List<Landmark> = repository.getAllLandmarks()
    fun getPronunciationTerms(): List<PronunciationTerm> = repository.getAllPronunciationTerms()
}
