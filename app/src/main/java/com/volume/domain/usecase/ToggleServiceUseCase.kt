package com.volume.domain.usecase

import com.volume.domain.repository.AudioRepository

class ToggleServiceUseCase(
    private val repository: AudioRepository
) {
    fun start() {
        repository.startService()
    }

    fun stop() {
        repository.stopService()
    }
}
