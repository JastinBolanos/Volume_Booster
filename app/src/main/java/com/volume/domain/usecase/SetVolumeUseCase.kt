package com.volume.domain.usecase

import com.volume.domain.repository.AudioRepository

class SetVolumeUseCase(
    private val repository: AudioRepository
) {
    operator fun invoke(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        repository.setSystemVolume(clamped)
    }
}
