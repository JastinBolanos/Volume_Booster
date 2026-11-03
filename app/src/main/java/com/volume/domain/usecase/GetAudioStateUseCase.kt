package com.volume.domain.usecase

import com.volume.domain.model.AudioState
import com.volume.domain.repository.AudioRepository
import kotlinx.coroutines.flow.StateFlow

class GetAudioStateUseCase(
    private val repository: AudioRepository
) {
    operator fun invoke(): StateFlow<AudioState> = repository.audioState
}
