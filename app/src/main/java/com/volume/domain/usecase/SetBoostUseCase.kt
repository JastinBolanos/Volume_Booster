package com.volume.domain.usecase

import com.volume.domain.AudioSafetyConstants
import com.volume.domain.repository.AudioRepository

class SetBoostUseCase(
    private val repository: AudioRepository
) {
    operator fun invoke(percent: Int) {
        // Enforce strict domain safety invariants (0% to 20%)
        val safePercent = percent.coerceIn(
            AudioSafetyConstants.MIN_BOOST_PERCENT,
            AudioSafetyConstants.MAX_SAFE_BOOST_PERCENT
        )
        repository.setBoost(safePercent)
    }
}
