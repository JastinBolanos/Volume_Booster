package com.volume.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.volume.domain.AudioConstants
import com.volume.domain.AudioSafetyConstants
import com.volume.domain.model.AudioState
import com.volume.domain.repository.AudioRepository
import com.volume.service.VolumeBoosterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudioRepositoryImpl(
    private val context: Context
) : AudioRepository {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs: SharedPreferences = context.getSharedPreferences(
        AudioConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _audioState = MutableStateFlow(
        AudioState(
            volumePercent = getCurrentSystemVolume(),
            boostPercent = prefs.getInt(AudioConstants.KEY_BOOST_PERCENT, 0),
            isServiceRunning = VolumeBoosterService.isServiceRunning,
            isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0,
            currentGainMb = calculateGainMb(prefs.getInt(AudioConstants.KEY_BOOST_PERCENT, 0))
        )
    )
    override val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val currentVol = getCurrentSystemVolume()
            val isMute = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
            _audioState.update { it.copy(volumePercent = currentVol, isMuted = isMute) }
        }
    }

    init {
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeObserver
            )
        } catch (_: Exception) { }
    }

    override fun getCurrentSystemVolume(): Int {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVol <= 0) return 0
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((currentVol.toFloat() / maxVol) * 100).toInt().coerceIn(0, 100)
    }

    override fun setSystemVolume(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = ((clamped / 100f) * maxVol).toInt().coerceIn(0, maxVol)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
        } catch (_: Exception) { }
        _audioState.update { it.copy(volumePercent = clamped, isMuted = targetIndex == 0) }
    }

    override fun toggleMute() {
        val currentlyMuted = _audioState.value.isMuted
        if (currentlyMuted) {
            setSystemVolume(50)
        } else {
            setSystemVolume(0)
        }
    }

    override fun setBoost(percent: Int) {
        val clamped = percent.coerceIn(
            AudioSafetyConstants.MIN_BOOST_PERCENT,
            AudioSafetyConstants.MAX_SAFE_BOOST_PERCENT
        )
        val gainMb = calculateGainMb(clamped)
        prefs.edit().putInt(AudioConstants.KEY_BOOST_PERCENT, clamped).apply()
        _audioState.update { it.copy(boostPercent = clamped, currentGainMb = gainMb) }

        val intent = Intent(context, VolumeBoosterService::class.java).apply {
            action = AudioConstants.ACTION_SET_BOOST
            putExtra(AudioConstants.EXTRA_BOOST_PERCENT, clamped)
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (_: Exception) { }
    }

    override fun startService() {
        val currentBoost = _audioState.value.boostPercent
        val intent = Intent(context, VolumeBoosterService::class.java).apply {
            action = AudioConstants.ACTION_START_SERVICE
            putExtra(AudioConstants.EXTRA_BOOST_PERCENT, currentBoost)
        }
        try {
            ContextCompat.startForegroundService(context, intent)
            _audioState.update { it.copy(isServiceRunning = true) }
        } catch (_: Exception) { }
    }

    override fun stopService() {
        val intent = Intent(context, VolumeBoosterService::class.java).apply {
            action = AudioConstants.ACTION_STOP_SERVICE
        }
        try {
            context.startService(intent)
            _audioState.update { it.copy(isServiceRunning = false, boostPercent = 0, currentGainMb = 0) }
            prefs.edit().putInt(AudioConstants.KEY_BOOST_PERCENT, 0).apply()
        } catch (_: Exception) { }
    }

    fun syncFromService(running: Boolean, boost: Int) {
        val gainMb = calculateGainMb(boost)
        _audioState.update {
            it.copy(
                isServiceRunning = running,
                boostPercent = boost,
                currentGainMb = gainMb,
                volumePercent = getCurrentSystemVolume(),
                isMuted = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
            )
        }
    }

    private fun calculateGainMb(boostPercent: Int): Int {
        val clamped = boostPercent.coerceIn(0, AudioSafetyConstants.MAX_SAFE_BOOST_PERCENT)
        return ((clamped.toFloat() / AudioSafetyConstants.MAX_SAFE_BOOST_PERCENT) *
                AudioSafetyConstants.MAX_SAFE_GAIN_MB).toInt()
    }

    companion object {
        @Volatile
        private var instance: AudioRepositoryImpl? = null

        fun getInstance(context: Context): AudioRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: AudioRepositoryImpl(context.applicationContext).also { instance = it }
            }
        }
    }
}
