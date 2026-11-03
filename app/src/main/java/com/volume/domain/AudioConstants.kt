package com.volume.domain

object AudioConstants {
    // Strict safety limits to protect hearing and speaker hardware
    const val MAX_SAFE_BOOST_PERCENT = 20
    const val MIN_BOOST_PERCENT = 0
    
    // 1000 millibels = 10 dB gain. This is the physical maximum threshold sent to LoudnessEnhancer
    const val MAX_SAFE_GAIN_MB = 1000
    
    // Step size for interactive notification + / - buttons
    const val BOOST_STEP_PERCENT = 5

    // Service Actions
    const val ACTION_START_SERVICE = "com.volume.volumebooster.ACTION_START"
    const val ACTION_STOP_SERVICE = "com.volume.volumebooster.ACTION_STOP"
    const val ACTION_SET_BOOST = "com.volume.volumebooster.ACTION_SET_BOOST"
    const val ACTION_BOOST_INCREASE = "com.volume.volumebooster.ACTION_BOOST_INCREASE"
    const val ACTION_BOOST_DECREASE = "com.volume.volumebooster.ACTION_BOOST_DECREASE"
    const val ACTION_SET_VOLUME = "com.volume.volumebooster.ACTION_SET_VOLUME"

    // Intent Extras
    const val EXTRA_BOOST_PERCENT = "extra_boost_percent"
    const val EXTRA_VOLUME_PERCENT = "extra_volume_percent"

    // Notification Channel
    const val NOTIFICATION_CHANNEL_ID = "volume_booster_engine_channel"
    const val NOTIFICATION_ID = 1001

    // SharedPreferences
    const val PREFS_NAME = "volume_booster_prefs"
    const val KEY_BOOST_PERCENT = "pref_boost_percent"
    const val KEY_SERVICE_ENABLED = "pref_service_enabled"
}
