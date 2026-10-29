package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.*
import com.example.data.repository.CurriculumData
import com.example.data.repository.UserProgressRepository
import com.example.util.HapticFeedbackHelper
import com.example.util.KartuliSpeechRecognizer
import com.example.util.KartuliTtsHelper
import com.example.util.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class LessonFeedback {
    data class Correct(val explanation: String) : LessonFeedback()
    data class Incorrect(val correctAnswer: String, val explanation: String) : LessonFeedback()
}

class KartuliViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserProgressRepository(application)
    val userProgress: StateFlow<UserProgress> = repository.userProgress

    val allModules: List<Module> = CurriculumData.ALL_MODULES
    val allLetters: List<AlphabetLetter> = CurriculumData.ALL_33_LETTERS
    val allVocabulary: List<VocabularyWord> = CurriculumData.ALL_VOCABULARY
    val allGrammarRules: List<GrammarRule> = CurriculumData.ALL_GRAMMAR_RULES
    val allAchievements: List<Achievement> = CurriculumData.ALL_ACHIEVEMENTS
    val dailyQuests: List<DailyQuest> = CurriculumData.DAILY_QUESTS
    val allDialogues: List<DialogueScenario> = CurriculumData.ALL_DIALOGUE_SCENARIOS

    private val ttsHelper = KartuliTtsHelper(application)
    private val hapticHelper = HapticFeedbackHelper(application)
    private val speechRecognizer = KartuliSpeechRecognizer(application)

    val speechState: StateFlow<SpeechState> = speechRecognizer.speechState

    // Current Lesson State
    private val _activeLesson = MutableStateFlow<Lesson?>(null)
    val activeLesson: StateFlow<Lesson?> = _activeLesson.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    // Multiple-choice selection or text selection
    private val _selectedOptions = MutableStateFlow<List<String>>(emptyList())
    val selectedOptions: StateFlow<List<String>> = _selectedOptions.asStateFlow()

    // Word bubbles selected for sentence building
    private val _selectedBubbles = MutableStateFlow<List<String>>(emptyList())
    val selectedBubbles: StateFlow<List<String>> = _selectedBubbles.asStateFlow()

    // Match pairs state
    private val _matchedPairs = MutableStateFlow<Map<String, String>>(emptyMap())
    val matchedPairs: StateFlow<Map<String, String>> = _matchedPairs.asStateFlow()
    private val _firstSelectedPairItem = MutableStateFlow<String?>(null)
    val firstSelectedPairItem: StateFlow<String?> = _firstSelectedPairItem.asStateFlow()

    // Bottom feedback sheet state
    private val _feedback = MutableStateFlow<LessonFeedback?>(null)
    val feedback: StateFlow<LessonFeedback?> = _feedback.asStateFlow()

    // Lesson completion state
    private val _isLessonCompleted = MutableStateFlow(false)
    val isLessonCompleted: StateFlow<Boolean> = _isLessonCompleted.asStateFlow()

    private val _lessonMistakesCount = MutableStateFlow(0)
    val lessonMistakesCount: StateFlow<Int> = _lessonMistakesCount.asStateFlow()

    // Vocabulary Search & Filter
    private val _vocabSearchQuery = MutableStateFlow("")
    val vocabSearchQuery: StateFlow<String> = _vocabSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Все")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active practice for mistakes mode
    private val _isMistakesPracticeMode = MutableStateFlow(false)
    val isMistakesPracticeMode: StateFlow<Boolean> = _isMistakesPracticeMode.asStateFlow()

    // Interactive Dialogue Scenario Simulation
    private val _activeDialogue = MutableStateFlow<DialogueScenario?>(null)
    val activeDialogue: StateFlow<DialogueScenario?> = _activeDialogue.asStateFlow()
    private val _currentDialogueTurnIndex = MutableStateFlow(0)
    val currentDialogueTurnIndex: StateFlow<Int> = _currentDialogueTurnIndex.asStateFlow()

    fun startLesson(lesson: Lesson, isMistakesPractice: Boolean = false) {
        _activeLesson.value = lesson
        _currentExerciseIndex.value = 0
        _isMistakesPracticeMode.value = isMistakesPractice
        _lessonMistakesCount.value = 0
        _isLessonCompleted.value = false
        resetExerciseInputs()

        val firstEx = lesson.exercises.firstOrNull()
        if (firstEx != null && firstEx.geText.isNotEmpty() && userProgress.value.isSoundEnabled) {
            playSpeech(firstEx.geText)
        }
    }

    fun startDailySmartPractice() {
        val mistakeIds = userProgress.value.activeMistakeExerciseIds
        val allEx = allModules.flatMap { it.lessons }.flatMap { it.exercises }
        val weakEx = allEx.filter { mistakeIds.contains(it.id) }
        val randomEx = allEx.shuffled().take(6)
        val combined = (weakEx + randomEx).distinctBy { it.id }.take(6)

        val smartLesson = Lesson(
            id = "smart_daily_practice",
            moduleId = "mod_smart",
            order = 0,
            title = "Ежедневная умная тренировка",
            description = "Индивидуальный подбор: повторение слабых тем и разговорная речь",
            iconName = "auto_awesome",
            exercises = combined,
            xpReward = 25,
            coinReward = 10
        )
        startLesson(smartLesson, isMistakesPractice = true)
    }

    fun startMistakesPractice() {
        val mistakeIds = userProgress.value.activeMistakeExerciseIds
        val allExercises = allModules.flatMap { it.lessons }.flatMap { it.exercises }
        val targetExercises = allExercises.filter { mistakeIds.contains(it.id) }
            .ifEmpty { allExercises.shuffled().take(4) }

        val mistakeLesson = Lesson(
            id = "practice_mistakes",
            moduleId = "mod_practice",
            order = 0,
            title = "Работа над ошибками",
            description = "Повторение трудных заданий и восстановление сердец",
            iconName = "healing",
            exercises = targetExercises,
            xpReward = 20,
            coinReward = 8
        )
        startLesson(mistakeLesson, isMistakesPractice = true)
    }

    private fun resetExerciseInputs() {
        _selectedOptions.value = emptyList()
        _selectedBubbles.value = emptyList()
        _matchedPairs.value = emptyMap()
        _firstSelectedPairItem.value = null
        _feedback.value = null
        speechRecognizer.cancel()
    }

    fun selectOption(option: String) {
        if (_feedback.value != null) return
        _selectedOptions.value = listOf(option)
    }

    fun addBubble(word: String) {
        if (_feedback.value != null) return
        _selectedBubbles.value = _selectedBubbles.value + word
    }

    fun removeBubble(index: Int) {
        if (_feedback.value != null) return
        val current = _selectedBubbles.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedBubbles.value = current
        }
    }

    fun onPairItemClicked(item: String, exercise: Exercise) {
        if (_feedback.value != null) return
        val first = _firstSelectedPairItem.value
        if (first == null) {
            _firstSelectedPairItem.value = item
        } else {
            val isMatch = (exercise.pairs[first] == item) || (exercise.pairs[item] == first)
            if (isMatch) {
                val ge = if (exercise.pairs.containsKey(first)) first else item
                val ru = if (exercise.pairs.containsKey(first)) item else first
                _matchedPairs.value = _matchedPairs.value + (ge to ru)
                if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateSuccess()
            } else {
                if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateError()
            }
            _firstSelectedPairItem.value = null
        }
    }

    // Voice recognition handlers
    fun startSpeechPractice(targetText: String) {
        speechRecognizer.startListening(targetText)
    }

    fun stopSpeechPractice() {
        speechRecognizer.stopListening()
    }

    fun checkAnswer() {
        val lesson = _activeLesson.value ?: return
        val exercise = lesson.exercises.getOrNull(_currentExerciseIndex.value) ?: return

        var isCorrect = false

        when (exercise.type) {
            ExerciseType.MATCH_PAIRS -> {
                isCorrect = _matchedPairs.value.size == exercise.pairs.size
            }
            ExerciseType.WORD_BUBBLES -> {
                isCorrect = _selectedBubbles.value == exercise.correctAnswers
            }
            ExerciseType.SPEAK_PRACTICE -> {
                val currentSpeech = speechState.value
                if (currentSpeech is SpeechState.Result) {
                    isCorrect = currentSpeech.isPassed
                    if (isCorrect) repository.recordVoicePractice()
                } else {
                    isCorrect = false
                }
            }
            else -> {
                val chosen = _selectedOptions.value.firstOrNull().orEmpty()
                isCorrect = exercise.correctAnswers.contains(chosen)
            }
        }

        if (isCorrect) {
            if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateSuccess()
            _feedback.value = LessonFeedback.Correct(exercise.explanation)
            if (_isMistakesPracticeMode.value) {
                repository.resolveMistake(exercise.id)
            }
        } else {
            if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateError()
            val correctDisplay = if (exercise.type == ExerciseType.WORD_BUBBLES) {
                exercise.correctAnswers.joinToString(" ")
            } else {
                exercise.correctAnswers.firstOrNull().orEmpty()
            }
            _feedback.value = LessonFeedback.Incorrect(
                correctAnswer = correctDisplay,
                explanation = exercise.explanation
            )
            _lessonMistakesCount.value = _lessonMistakesCount.value + 1
            repository.recordMistake(exercise.id)
            if (!_isMistakesPracticeMode.value) {
                repository.loseHeart()
            }
        }
    }

    fun nextExercise() {
        val lesson = _activeLesson.value ?: return
        val nextIndex = _currentExerciseIndex.value + 1

        if (nextIndex < lesson.exercises.size) {
            _currentExerciseIndex.value = nextIndex
            resetExerciseInputs()

            val nextEx = lesson.exercises[nextIndex]
            if (nextEx.geText.isNotEmpty() && userProgress.value.isSoundEnabled) {
                playSpeech(nextEx.geText)
            }
        } else {
            _isLessonCompleted.value = true
            _feedback.value = null

            val xp = if (_lessonMistakesCount.value == 0) lesson.xpReward + 5 else lesson.xpReward
            val coins = lesson.coinReward
            
            val currentMod = allModules.find { it.id == lesson.moduleId }
            val nextMod = allModules.find { it.order == (currentMod?.order ?: 1) + 1 }
            repository.completeLesson(
                lessonId = lesson.id,
                xpEarned = xp,
                coinsEarned = coins,
                unlockModuleId = nextMod?.id
            )
        }
    }

    fun closeLesson() {
        _activeLesson.value = null
        _isLessonCompleted.value = false
        resetExerciseInputs()
    }

    // Dialogue Simulation
    fun startDialogue(scenario: DialogueScenario) {
        _activeDialogue.value = scenario
        _currentDialogueTurnIndex.value = 0
        val firstTurn = scenario.turns.firstOrNull()
        if (firstTurn != null && !firstTurn.isUserTurn && userProgress.value.isSoundEnabled) {
            playSpeech(firstTurn.textGe)
        }
    }

    fun answerDialogue(optionIndex: Int) {
        val scenario = _activeDialogue.value ?: return
        val turn = scenario.turns.getOrNull(_currentDialogueTurnIndex.value) ?: return

        if (optionIndex == turn.correctReplyIndex) {
            if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateSuccess()
            advanceDialogue()
        } else {
            if (userProgress.value.isHapticsEnabled) hapticHelper.vibrateError()
        }
    }

    fun advanceDialogue() {
        val scenario = _activeDialogue.value ?: return
        val nextIndex = _currentDialogueTurnIndex.value + 1
        if (nextIndex < scenario.turns.size) {
            _currentDialogueTurnIndex.value = nextIndex
            val turn = scenario.turns[nextIndex]
            if (!turn.isUserTurn && userProgress.value.isSoundEnabled) {
                playSpeech(turn.textGe)
            }
        } else {
            // Dialogue completed!
            repository.recordDialogueCompleted(scenario.id)
            _activeDialogue.value = null
        }
    }

    fun closeDialogue() {
        _activeDialogue.value = null
        _currentDialogueTurnIndex.value = 0
    }

    fun playSpeech(text: String, isSlow: Boolean = false) {
        ttsHelper.speak(text, isSlow)
    }

    fun refillHeartsWithCoins(): Boolean {
        return repository.buyHeartsRefill()
    }

    fun setVocabSearchQuery(query: String) {
        _vocabSearchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun toggleBookmark(wordId: String) {
        repository.toggleBookmarkWord(wordId)
    }

    fun setDailyGoal(goalXp: Int) {
        repository.setDailyGoal(goalXp)
    }

    fun toggleTheme(isDark: Boolean?) {
        repository.toggleDarkMode(isDark)
    }

    fun toggleSound() {
        repository.toggleSound()
    }

    fun toggleHaptics() {
        repository.toggleHaptics()
    }

    fun getStatistics(): LearningStatistics {
        return repository.getStatistics()
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
        speechRecognizer.destroy()
    }
}
