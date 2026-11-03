package com.volume.domain.model

data class AudioState(
    val volumePercent: Int = 80,
    val boostPercent: Int = 0,
    val isServiceRunning: Boolean = false,
    val isMuted: Boolean = false,
    val currentGainMb: Int = 0
) {
    val gainDbText: String
        get() {
            val db = currentGainMb / 100.0
            return String.format("+%.1f dB", db)
        }
}
