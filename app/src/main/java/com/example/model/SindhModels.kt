package com.example.model

import androidx.compose.ui.graphics.Color

enum class StoryPerspective(val label: String, val description: String) {
    TRADER("Silk Road Trader", "Focuses on commerce, maritime networks, weights & currency"),
    POET_CRAFTSMAN("Court Poet & Artisan", "Focuses on culture, oral epics, architecture & crafts"),
    RULER_LEADER("Ruler & Strategist", "Focuses on governance, diplomacy, forts & civic planning")
}

enum class AppThemeStyle(val displayName: String, val requiredPoints: Int) {
    MODERN_SINDHI("Modern Sindhi", 0),
    AJRAK_INDIGO("Ajrak Indigo & Crimson", 50),
    TERRACOTTA_EARTH("Terracotta Clay", 120),
    LAPIS_ROYAL("Royal Lapis & Gold", 200)
}

data class ArtifactHotspot(
    val id: String,
    val title: String,
    val xPercent: Float, // 0.0f to 1.0f on visual card
    val yPercent: Float,
    val significance: String,
    val detailNote: String
)

data class PronunciationTerm(
    val id: String,
    val term: String,
    val phonetic: String,
    val translation: String,
    val culturalContext: String
)

data class HistoriansCorner(
    val historicalNotes: String,
    val museumLocation: String,
    val primarySourceCitation: String
)

data class HistoricalEra(
    val id: String,
    val title: String,
    val subtitle: String,
    val dateRange: String,
    val location: String,
    val description: String,
    val primaryColorHex: Long,
    val keyTopics: List<String>,
    val requiredKnowledgePoints: Int = 0,
    val eraIndex: Int
)

data class StoryChoice(
    val id: String,
    val text: String,
    val consequenceText: String,
    val knowledgePointsReward: Int,
    val nextNodeId: String?,
    val artifactUnlockedId: String? = null,
    val historicalInsight: String,
    val historiansCorner: HistoriansCorner? = null
)

data class StoryNode(
    val id: String,
    val eraId: String,
    val title: String,
    val locationName: String,
    val speakerTitle: String,
    val narrativeText: String,
    val choices: List<StoryChoice>,
    val traderNarrative: String? = null,
    val poetNarrative: String? = null,
    val rulerNarrative: String? = null
)

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val rewardPoints: Int = 30
)

data class CulturalArtifact(
    val id: String,
    val name: String,
    val eraId: String,
    val eraName: String,
    val category: String, // e.g. "Sculpture", "Urban Planning", "Textile & Craft", "Architecture", "Engineering", "Sufi Music"
    val briefSummary: String,
    val microLessonContent: String,
    val keyFacts: List<String>,
    val quizQuestion: QuizQuestion,
    val defaultUnlocked: Boolean = false,
    val historiansCorner: HistoriansCorner? = null,
    val hotspots: List<ArtifactHotspot> = emptyList()
)

data class Landmark(
    val id: String,
    val name: String,
    val eraId: String,
    val region: String,
    val shortSummary: String,
    val mapX: Float, // 0.0 to 1.0 relative canvas position
    val mapY: Float
)

data class UserBadge(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean = false
)

data class UserProgressData(
    val knowledgePoints: Int = 0,
    val rankTitle: String = "Sindh Novice",
    val dailyStreak: Int = 3,
    val unlockedEraIds: Set<String> = setOf("era_indus_valley"),
    val completedNodeIds: Set<String> = emptySet(),
    val unlockedArtifactIds: Set<String> = setOf("art_priest_king", "art_great_bath"),
    val answeredQuizIds: Set<String> = emptySet(),
    val unlockedBadgeIds: Set<String> = setOf("badge_indus_explorer"),
    val unlockedThemeStyles: Set<String> = setOf("MODERN_SINDHI")
)
