package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class KartuliTtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var isGeorgianAvailable = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val kaLocale = Locale("ka", "GE")
            val result = tts?.setLanguage(kaLocale)
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                isGeorgianAvailable = true
            } else {
                // Try language only
                val kaOnly = Locale("ka")
                val langResult = tts?.setLanguage(kaOnly)
                isGeorgianAvailable = langResult == TextToSpeech.LANG_AVAILABLE || langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE
            }
            isInitialized = true
        } else {
            Log.e("KartuliTTS", "Init failed: $status")
        }
    }

    fun speak(text: String, isSlow: Boolean = false) {
        if (!isInitialized || tts == null) return
        try {
            tts?.setSpeechRate(if (isSlow) 0.65f else 0.95f)
            tts?.setPitch(1.0f)
            val params = Bundle()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "kartuli_tts_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("KartuliTTS", "Failed speaking: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Safe cleanup
        }
    }
}
