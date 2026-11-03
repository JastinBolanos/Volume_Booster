package com.volume.domain

/**
 * Pure Kotlin domain constants for hardware protection and hearing safety.
 * Zero Android framework dependencies.
 */
object AudioSafetyConstants {
    // Strict visual limit (0% to 20%) to prevent audio distortion and speaker damage
    const val MIN_BOOST_PERCENT = 0
    const val MAX_SAFE_BOOST_PERCENT = 20

    // Physical maximum threshold applied to the audio engine (1000 mB = +10.0 dB)
    const val MAX_SAFE_GAIN_MB = 1000

    // Notification step increment
    const val BOOST_STEP_PERCENT = 5
}
