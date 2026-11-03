package com.volume.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.volume.domain.model.AudioState
import com.volume.domain.usecase.GetAudioStateUseCase
import com.volume.domain.usecase.SetBoostUseCase
import com.volume.domain.usecase.SetVolumeUseCase
import com.volume.domain.usecase.ToggleMuteUseCase
import com.volume.domain.usecase.ToggleServiceUseCase
import kotlinx.coroutines.flow.StateFlow

/**
 * Presentation ViewModel strictly isolated from the data layer.
 * Communicates exclusively with Domain UseCases.
 */
class VolumeBoosterViewModel(
    getAudioStateUseCase: GetAudioStateUseCase,
    private val setVolumeUseCase: SetVolumeUseCase,
    private val setBoostUseCase: SetBoostUseCase,
    private val toggleServiceUseCase: ToggleServiceUseCase,
    private val toggleMuteUseCase: ToggleMuteUseCase
) : ViewModel() {

    val uiState: StateFlow<AudioState> = getAudioStateUseCase()

    fun onVolumeChange(percent: Int) {
        setVolumeUseCase(percent)
    }

    fun onBoostChange(percent: Int) {
        setBoostUseCase(percent)
    }

    fun startService() {
        toggleServiceUseCase.start()
    }

    fun stopService() {
        toggleServiceUseCase.stop()
    }

    fun toggleMute() {
        toggleMuteUseCase()
    }

    class Factory(
        private val getAudioStateUseCase: GetAudioStateUseCase,
        private val setVolumeUseCase: SetVolumeUseCase,
        private val setBoostUseCase: SetBoostUseCase,
        private val toggleServiceUseCase: ToggleServiceUseCase,
        private val toggleMuteUseCase: ToggleMuteUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VolumeBoosterViewModel::class.java)) {
                return VolumeBoosterViewModel(
                    getAudioStateUseCase = getAudioStateUseCase,
                    setVolumeUseCase = setVolumeUseCase,
                    setBoostUseCase = setBoostUseCase,
                    toggleServiceUseCase = toggleServiceUseCase,
                    toggleMuteUseCase = toggleMuteUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
