package com.volume.domain.usecase

import com.volume.domain.repository.AudioRepository

class ToggleMuteUseCase(
    private val repository: AudioRepository
) {
    operator fun invoke() {
        repository.toggleMute()
    }
}
