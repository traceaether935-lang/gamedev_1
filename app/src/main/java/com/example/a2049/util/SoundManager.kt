package com.example.a2049.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

class SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playMoveSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to play move sound", e)
        }
    }

    fun playMergeSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 50)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to play merge sound", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
