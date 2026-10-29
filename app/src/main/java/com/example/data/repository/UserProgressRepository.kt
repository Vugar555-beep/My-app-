package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.LearningStatistics
import com.example.data.model.UserProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProgressRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("kartuli_user_prefs", Context.MODE_PRIVATE)

    private val _userProgress = MutableStateFlow(loadProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    private fun loadProgress(): UserProgress {
        val completedLessons = prefs.getStringSet("completed_lessons", setOf("les_1_1")) ?: setOf("les_1_1")
        val unlockedModules = prefs.getStringSet("unlocked_modules", setOf("mod_1", "mod_2", "mod_3")) ?: setOf("mod_1", "mod_2", "mod_3")
        val activeMistakes = prefs.getStringSet("active_mistakes", setOf("ex_1_2_1", "ex_1_3_1")) ?: setOf("ex_1_2_1", "ex_1_3_1")
        val savedWords = prefs.getStringSet("saved_words", setOf("v1", "v2", "v20", "v21")) ?: setOf("v1", "v2", "v20", "v21")
        val completedDialogues = prefs.getStringSet("completed_dialogues", setOf("diag_cafe")) ?: setOf("diag_cafe")

        val hearts = prefs.getInt("hearts", 5)
        val streak = prefs.getInt("streak", 3)
        val totalXp = prefs.getInt("total_xp", 160)
        val coins = prefs.getInt("coins", 75)
        val dailyGoal = prefs.getInt("daily_goal", 50)
        val todayXp = prefs.getInt("today_xp", 25)
        val voicePractices = prefs.getInt("voice_practices", 4)
        val studyMinutes = prefs.getInt("study_minutes", 48)
        val sound = prefs.getBoolean("sound_enabled", true)
        val haptics = prefs.getBoolean("haptics_enabled", true)

        val hasDarkPref = prefs.contains("dark_theme")
        val isDark = if (hasDarkPref) prefs.getBoolean("dark_theme", false) else null

        return UserProgress(
            hearts = hearts,
            streakDays = streak,
            totalXp = totalXp,
            coins = coins,
            completedLessonIds = completedLessons,
            unlockedModuleIds = unlockedModules,
            dailyGoalXp = dailyGoal,
            todayXp = todayXp,
            activeMistakeExerciseIds = activeMistakes,
            bookmarkedWordIds = savedWords,
            isSoundEnabled = sound,
            isHapticsEnabled = haptics,
            isDarkMode = isDark,
            voicePracticesCount = voicePractices,
            totalStudyMinutes = studyMinutes,
            completedDialogueIds = completedDialogues
        )
    }

    private fun persistProgress(progress: UserProgress) {
        prefs.edit().apply {
            putInt("hearts", progress.hearts)
            putInt("streak", progress.streakDays)
            putInt("total_xp", progress.totalXp)
            putInt("coins", progress.coins)
            putInt("daily_goal", progress.dailyGoalXp)
            putInt("today_xp", progress.todayXp)
            putInt("voice_practices", progress.voicePracticesCount)
            putInt("study_minutes", progress.totalStudyMinutes)
            putStringSet("completed_lessons", progress.completedLessonIds)
            putStringSet("unlocked_modules", progress.unlockedModuleIds)
            putStringSet("active_mistakes", progress.activeMistakeExerciseIds)
            putStringSet("saved_words", progress.bookmarkedWordIds)
            putStringSet("completed_dialogues", progress.completedDialogueIds)
            putBoolean("sound_enabled", progress.isSoundEnabled)
            putBoolean("haptics_enabled", progress.isHapticsEnabled)
            if (progress.isDarkMode != null) {
                putBoolean("dark_theme", progress.isDarkMode)
            } else {
                remove("dark_theme")
            }
            apply()
        }
    }

    fun completeLesson(lessonId: String, xpEarned: Int, coinsEarned: Int, unlockModuleId: String? = null) {
        val current = _userProgress.value
        val newCompleted = current.completedLessonIds + lessonId
        val newUnlockedModules = if (unlockModuleId != null) current.unlockedModuleIds + unlockModuleId else current.unlockedModuleIds
        
        val updated = current.copy(
            completedLessonIds = newCompleted,
            unlockedModuleIds = newUnlockedModules,
            totalXp = current.totalXp + xpEarned,
            todayXp = current.todayXp + xpEarned,
            coins = current.coins + coinsEarned,
            totalStudyMinutes = current.totalStudyMinutes + 5
        )
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun recordVoicePractice() {
        val current = _userProgress.value
        val updated = current.copy(
            voicePracticesCount = current.voicePracticesCount + 1,
            totalXp = current.totalXp + 5,
            todayXp = current.todayXp + 5
        )
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun recordDialogueCompleted(dialogueId: String) {
        val current = _userProgress.value
        val updated = current.copy(
            completedDialogueIds = current.completedDialogueIds + dialogueId,
            totalXp = current.totalXp + 25,
            coins = current.coins + 10
        )
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun loseHeart(): Boolean {
        val current = _userProgress.value
        if (current.hearts <= 0) return false
        val newHearts = (current.hearts - 1).coerceAtLeast(0)
        val updated = current.copy(hearts = newHearts)
        _userProgress.value = updated
        persistProgress(updated)
        return true
    }

    fun refillHeartsFully() {
        val current = _userProgress.value
        val updated = current.copy(hearts = current.maxHearts)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun buyHeartsRefill(): Boolean {
        val current = _userProgress.value
        val cost = 20
        if (current.coins >= cost) {
            val updated = current.copy(
                coins = current.coins - cost,
                hearts = current.maxHearts
            )
            _userProgress.value = updated
            persistProgress(updated)
            return true
        }
        return false
    }

    fun recordMistake(exerciseId: String) {
        val current = _userProgress.value
        val updated = current.copy(activeMistakeExerciseIds = current.activeMistakeExerciseIds + exerciseId)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun resolveMistake(exerciseId: String) {
        val current = _userProgress.value
        val updated = current.copy(
            activeMistakeExerciseIds = current.activeMistakeExerciseIds - exerciseId,
            totalXp = current.totalXp + 5,
            todayXp = current.todayXp + 5,
            hearts = (current.hearts + 1).coerceAtMost(current.maxHearts)
        )
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun toggleBookmarkWord(wordId: String) {
        val current = _userProgress.value
        val newBookmarks = if (current.bookmarkedWordIds.contains(wordId)) {
            current.bookmarkedWordIds - wordId
        } else {
            current.bookmarkedWordIds + wordId
        }
        val updated = current.copy(bookmarkedWordIds = newBookmarks)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun setDailyGoal(goalXp: Int) {
        val current = _userProgress.value
        val updated = current.copy(dailyGoalXp = goalXp)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun toggleDarkMode(isDark: Boolean?) {
        val current = _userProgress.value
        val updated = current.copy(isDarkMode = isDark)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun toggleSound() {
        val current = _userProgress.value
        val updated = current.copy(isSoundEnabled = !current.isSoundEnabled)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun toggleHaptics() {
        val current = _userProgress.value
        val updated = current.copy(isHapticsEnabled = !current.isHapticsEnabled)
        _userProgress.value = updated
        persistProgress(updated)
    }

    fun getStatistics(): LearningStatistics {
        val p = _userProgress.value
        val wordsCount = (p.completedLessonIds.size * 5 + p.bookmarkedWordIds.size).coerceAtMost(CurriculumData.ALL_VOCABULARY.size)
        val lessonsCount = p.completedLessonIds.size
        val accuracy = if (p.activeMistakeExerciseIds.isEmpty()) 94 else 86
        
        return LearningStatistics(
            wordsLearnedCount = wordsCount,
            lessonsCompletedCount = lessonsCount,
            overallAccuracyRate = accuracy,
            totalStudyMinutes = p.totalStudyMinutes,
            voicePracticesCompleted = p.voicePracticesCount,
            strongTopics = listOf("Приветствия и этикет", "Алфавит Мхедрули", "Заказ в кафе"),
            weakTopics = if (p.activeMistakeExerciseIds.isNotEmpty()) listOf("Послелоги -ши и -зе", "Спряжение связки 'быть'") else listOf("Гортанные звуки [к']/ [п']"),
            weeklyXp = listOf(
                "Пн" to 30,
                "Вт" to 45,
                "Ср" to 50,
                "Чт" to p.todayXp,
                "Пт" to 0,
                "Сб" to 0,
                "Вс" to 0
            )
        )
    }
}
