package com.example.game

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var bgmPlayer: MediaPlayer? = null
    private var isBgmPlaying = false
    private var vibrator: Vibrator? = null

    private var isSfxEnabled = true
    private var isMusicEnabled = true
    private var musicVolume = 0.5f
    private var isVibrationEnabled = true

    fun init(context: Context) {
        if (toneGenerator == null) {
            // Volume 70 out of 100
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        }
        
        if (vibrator == null) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (bgmPlayer == null) {
            try {
                // To the User: Please make sure your audio file is placed in /app/src/main/res/raw/bgm.mp3
                val resId = context.resources.getIdentifier("bgm", "raw", context.packageName)
                if (resId != 0) {
                    bgmPlayer = MediaPlayer.create(context, resId)?.apply {
                        isLooping = true
                        setVolume(musicVolume, musicVolume)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateSettings(music: Boolean, volume: Float, sfx: Boolean, vibration: Boolean) {
        isMusicEnabled = music
        musicVolume = volume
        isSfxEnabled = sfx
        isVibrationEnabled = vibration

        bgmPlayer?.setVolume(volume, volume)

        if (!music && isBgmPlaying) {
            bgmPlayer?.pause()
            isBgmPlaying = false
        } else if (music && bgmPlayer != null && !bgmPlayer!!.isPlaying) {
            bgmPlayer?.start()
            isBgmPlaying = true
        }
    }

    fun vibrate(durationMs: Long = 50) {
        if (!isVibrationEnabled) return
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBgm() {
        if (!isMusicEnabled) return
        bgmPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                isBgmPlaying = true
            }
        }
    }

    fun pauseBgm() {
        bgmPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isBgmPlaying = false
            }
        }
    }

    fun resumeBgm() {
        if (!isMusicEnabled) return
        bgmPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                isBgmPlaying = true
            }
        }
    }

    fun playHintSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playMergeSound() {
        if (!isSfxEnabled) return
        try {
            // A short satisfying beep
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playWinSound() {
        if (!isSfxEnabled) return
        try {
            CoroutineScope(Dispatchers.Default).launch {
                // A victory sequence
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
                delay(130)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 180)
                delay(200)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playThreeStarWinSound() {
        if (!isSfxEnabled) return
        try {
            CoroutineScope(Dispatchers.Default).launch {
                // A stronger celebratory sequence
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
                delay(320)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 400)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playStarRevealSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playGameOverSound() {
        if (!isSfxEnabled) return
        try {
            CoroutineScope(Dispatchers.Default).launch {
                // A descending "failure" sequence
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
                delay(250)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 400)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        bgmPlayer?.release()
        bgmPlayer = null
    }
}
