package com.example.data.model

data class UserProgress(
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val streakDays: Int = 3,
    val lastStudyDate: String = "",
    val totalXp: Int = 120,
    val coins: Int = 45,
    val completedLessonIds: Set<String> = emptySet(),
    val unlockedModuleIds: Set<String> = setOf("mod_1", "mod_2", "mod_3"),
    val dailyGoalXp: Int = 50,
    val todayXp: Int = 20,
    val activeMistakeExerciseIds: Set<String> = emptySet(),
    val bookmarkedWordIds: Set<String> = emptySet(),
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isDarkMode: Boolean? = null,
    val voicePracticesCount: Int = 0,
    val totalStudyMinutes: Int = 45,
    val srsWordLevels: Map<String, Int> = emptyMap(),
    val topicMistakeCounts: Map<String, Int> = emptyMap(),
    val completedDialogueIds: Set<String> = emptySet()
)
