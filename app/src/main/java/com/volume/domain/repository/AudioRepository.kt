package com.volume.domain.repository

import com.volume.domain.model.AudioState
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain interface defining audio state access and modification contracts.
 * Strictly decoupled from Android framework implementation details.
 */
interface AudioRepository {
    val audioState: StateFlow<AudioState>

    fun setSystemVolume(percent: Int)
    fun setBoost(percent: Int)
    fun toggleMute()
    fun startService()
    fun stopService()
    fun getCurrentSystemVolume(): Int
}
