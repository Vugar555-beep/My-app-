package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.max

sealed class SpeechState {
    object Idle : SpeechState()
    data class Listening(val rmsDb: Float = 0f) : SpeechState()
    object Processing : SpeechState()
    data class Result(
        val spokenText: String,
        val targetText: String,
        val accuracyScore: Int, // 0..100
        val isPassed: Boolean,
        val wordBreakdown: List<Pair<String, Boolean>>,
        val feedbackComment: String
    ) : SpeechState()
    data class Error(val message: String, val canRetry: Boolean = true) : SpeechState()
}

class KartuliSpeechRecognizer(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private var targetPhrase: String = ""

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@KartuliSpeechRecognizer)
            }
        }
    }

    fun startListening(target: String) {
        targetPhrase = target
        if (speechRecognizer == null) {
            initRecognizer()
        }

        if (speechRecognizer == null) {
            _speechState.value = SpeechState.Error("Голосовой ввод недоступен на этом устройстве.")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ka-GE")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ka", "ka-GE", "ru-RU"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            _speechState.value = SpeechState.Listening(0f)
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechState.value = SpeechState.Error("Ошибка запуска микрофона: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        _speechState.value = SpeechState.Idle
    }

    // Evaluate Georgian pronunciation and word matching
    private fun evaluatePronunciation(spoken: String, target: String): SpeechState.Result {
        val cleanSpoken = spoken.trim().replace(Regex("[.,!?:;\"'«»]"), "").lowercase()
        val cleanTarget = target.trim().replace(Regex("[.,!?:;\"'«»]"), "").lowercase()

        val targetWords = cleanTarget.split(Regex("\\s+")).filter { it.isNotBlank() }
        val spokenWords = cleanSpoken.split(Regex("\\s+")).filter { it.isNotBlank() }

        val wordBreakdown = targetWords.map { targetWord ->
            // Check direct match or close Levenshtein
            val matched = spokenWords.any { spokenWord ->
                spokenWord == targetWord || calculateSimilarity(spokenWord, targetWord) >= 0.70
            }
            targetWord to matched
        }

        val totalScore = if (targetWords.isNotEmpty()) {
            val matchedCount = wordBreakdown.count { it.second }
            val wordRatio = matchedCount.toFloat() / targetWords.size
            val fullStringSim = calculateSimilarity(cleanSpoken, cleanTarget)
            val combined = (wordRatio * 0.6f + fullStringSim * 0.4f) * 100f
            combined.toInt().coerceIn(0, 100)
        } else {
            100
        }

        val isPassed = totalScore >= 60

        val comment = when {
            totalScore >= 85 -> "Блестяще! Произношение как у коренного жителя Тбилиси!"
            totalScore >= 65 -> "Очень хорошо! Грузинские интонации переданы верно."
            totalScore >= 40 -> "Неплохо, но попробуйте четче выделить согласные звуки."
            else -> "Сложное слово! Послушайте Сосо в замедленном режиме и повторите."
        }

        return SpeechState.Result(
            spokenText = spoken,
            targetText = target,
            accuracyScore = totalScore,
            isPassed = isPassed,
            wordBreakdown = wordBreakdown,
            feedbackComment = comment
        )
    }

    private fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f
        val distance = levenshteinDistance(s1, s2)
        val maxLen = max(s1.length, s2.length)
        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _speechState.value = SpeechState.Listening(0f)
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (_speechState.value is SpeechState.Listening) {
            _speechState.value = SpeechState.Listening(rmsdB.coerceAtLeast(0f))
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognized = matches?.firstOrNull() ?: ""
        if (recognized.isNotBlank()) {
            _speechState.value = evaluatePronunciation(recognized, targetPhrase)
        } else {
            _speechState.value = SpeechState.Error("Речь не распознана. Попробуйте сказать громче и ближе к микрофону.")
        }
    }

    override fun onError(error: Int) {
        val msg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "Слово не распознано. Нажмите микрофон и повторите еще раз."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Не услышали ваш голос. Попробуйте снова!"
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Сетевая ошибка распознавания. Проверьте подключение или повторите попытку."
            SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудиоустройства."
            else -> "Не удалось распознать фразу (код $error). Попробуйте снова."
        }
        _speechState.value = SpeechState.Error(msg)
    }

    override fun onBeginningOfSpeech() {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        _speechState.value = SpeechState.Processing
    }
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
