package com.example.data.model

data class AlphabetLetter(
    val letter: String,
    val name: String,
    val transcriptionRu: String,
    val transcriptionLat: String,
    val soundDescription: String,
    val isEjectiveOrSpecial: Boolean = false,
    val exampleWordGe: String,
    val exampleWordTranscription: String,
    val exampleWordRu: String,
    val audioQuery: String = letter
)

data class VocabularyWord(
    val id: String,
    val ge: String,
    val transcriptionRu: String,
    val ru: String,
    val category: String,
    val exampleGe: String = "",
    val exampleRu: String = "",
    val phoneticNote: String = "",
    val grammarNote: String = "",
    val masteryLevel: Int = 0 // 0 to 5 for SRS
)

enum class ExerciseType {
    TRANSLATE_TO_RU,
    TRANSLATE_TO_GE,
    WORD_BUBBLES,
    LETTER_RECOGNITION,
    FILL_BLANK,
    MATCH_PAIRS,
    AUDIO_LISTEN,
    DIALOGUE,
    SPEAK_PRACTICE,        // Speaking practice via microphone
    READING_COMPREHENSION  // Reading a paragraph and answering questions
}

data class Exercise(
    val id: String,
    val type: ExerciseType,
    val prompt: String,
    val geText: String = "",
    val ruText: String = "",
    val transcriptionRu: String = "",
    val options: List<String> = emptyList(),
    val correctAnswers: List<String>,
    val bubblePool: List<String> = emptyList(),
    val pairs: Map<String, String> = emptyMap(),
    val explanation: String = "",
    val dialogueSpeaker: String = "",
    val audioText: String = geText,
    val readingPassageGe: String = "",
    val readingPassageRu: String = "",
    val topicTag: String = "Общее"
)

data class Lesson(
    val id: String,
    val moduleId: String,
    val order: Int,
    val title: String,
    val description: String,
    val iconName: String,
    val exercises: List<Exercise>,
    val xpReward: Int = 20,
    val coinReward: Int = 8,
    val isExamOrCheckpoint: Boolean = false
)

data class Module(
    val id: String,
    val order: Int,
    val title: String,
    val subtitle: String,
    val levelTag: String,          // "A1.1 Начинающий", "A1.2 Базовый", "A2 Продолжающий", "B1 Разговорный"
    val description: String,
    val locationName: String,
    val lessons: List<Lesson>,
    val checkpointExam: Lesson? = null
)

data class GrammarRule(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val detailedExplanation: String,
    val examples: List<Pair<String, String>>,
    val keyTables: List<Pair<String, List<Pair<String, String>>>> = emptyList(),
    val proTip: String = "",
    val relatedLessonId: String? = null
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val target: Int,
    val currentProgress: Int,
    val isUnlocked: Boolean = false
)

data class DailyQuest(
    val id: String,
    val title: String,
    val rewardXp: Int,
    val target: Int,
    val current: Int,
    val isCompleted: Boolean = false
)

// Interactive Dialogue Simulation
data class DialogueTurn(
    val id: String,
    val speaker: String,
    val isUserTurn: Boolean,
    val textGe: String,
    val textRu: String,
    val transcriptionRu: String,
    val userOptions: List<String> = emptyList(),
    val correctReplyIndex: Int = 0
)

data class DialogueScenario(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val iconEmoji: String,
    val turns: List<DialogueTurn>
)

// Real Statistics Model
data class LearningStatistics(
    val wordsLearnedCount: Int,
    val lessonsCompletedCount: Int,
    val overallAccuracyRate: Int,
    val totalStudyMinutes: Int,
    val voicePracticesCompleted: Int,
    val strongTopics: List<String>,
    val weakTopics: List<String>,
    val weeklyXp: List<Pair<String, Int>>
)
