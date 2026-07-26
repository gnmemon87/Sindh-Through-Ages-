package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = 1,
    val knowledgePoints: Int = 0,
    val rankTitle: String = "Sindh Novice Explorer",
    val dailyStreak: Int = 3,
    val unlockedEraIdsString: String = "era_indus_valley",
    val completedNodeIdsString: String = "",
    val unlockedArtifactIdsString: String = "art_priest_king,art_great_bath",
    val answeredQuizIdsString: String = "",
    val unlockedBadgeIdsString: String = "badge_indus_explorer"
)
