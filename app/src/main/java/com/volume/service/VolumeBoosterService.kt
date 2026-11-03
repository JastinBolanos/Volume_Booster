package com.volume.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.volume.MainActivity
import com.volume.R
import com.volume.data.repository.AudioRepositoryImpl
import com.volume.domain.AudioConstants

class VolumeBoosterService : Service() {

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentBoostPercent: Int = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VolumeBoosterService onCreate")
        isServiceRunning = true

        acquireWakeLock()
        initLoudnessEnhancer()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: AudioConstants.ACTION_START_SERVICE
        Log.d(TAG, "onStartCommand action: $action")

        when (action) {
            AudioConstants.ACTION_STOP_SERVICE -> {
                stopBoosterService()
                return START_NOT_STICKY
            }

            AudioConstants.ACTION_BOOST_INCREASE -> {
                val newBoost = (currentBoostPercent + AudioConstants.BOOST_STEP_PERCENT)
                    .coerceIn(AudioConstants.MIN_BOOST_PERCENT, AudioConstants.MAX_SAFE_BOOST_PERCENT)
                applyBoost(newBoost)
            }

            AudioConstants.ACTION_BOOST_DECREASE -> {
                val newBoost = (currentBoostPercent - AudioConstants.BOOST_STEP_PERCENT)
                    .coerceIn(AudioConstants.MIN_BOOST_PERCENT, AudioConstants.MAX_SAFE_BOOST_PERCENT)
                applyBoost(newBoost)
            }

            AudioConstants.ACTION_SET_BOOST -> {
                val requestedBoost = intent?.getIntExtra(
                    AudioConstants.EXTRA_BOOST_PERCENT,
                    currentBoostPercent
                ) ?: currentBoostPercent
                applyBoost(requestedBoost)
            }

            AudioConstants.ACTION_START_SERVICE -> {
                val requestedBoost = intent?.getIntExtra(
                    AudioConstants.EXTRA_BOOST_PERCENT,
                    currentBoostPercent
                ) ?: currentBoostPercent
                applyBoost(requestedBoost)
            }
        }

        val notification = buildCustomNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                AudioConstants.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(AudioConstants.NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "VolumeBooster::ServiceWakeLock"
            )?.apply {
                setReferenceCounted(false)
                try {
                    acquire()
                    Log.d(TAG, "Partial WakeLock successfully acquired")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to acquire WakeLock", e)
                }
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            try {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Partial WakeLock released")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing WakeLock", e)
            }
        }
        wakeLock = null
    }

    private fun initLoudnessEnhancer() {
        if (loudnessEnhancer == null) {
            try {
                // Apply LoudnessEnhancer to audio session 0 (global system mix)
                loudnessEnhancer = LoudnessEnhancer(0).apply {
                    enabled = true
                }
                Log.d(TAG, "LoudnessEnhancer initialized on audio session 0")
            } catch (e: Exception) {
                Log.e(TAG, "LoudnessEnhancer initialization error on session 0", e)
            }
        }
    }

    private fun applyBoost(boostPercent: Int) {
        // Enforce the strict 20% safety ceiling
        val clampedPercent = boostPercent.coerceIn(
            AudioConstants.MIN_BOOST_PERCENT,
            AudioConstants.MAX_SAFE_BOOST_PERCENT
        )
        currentBoostPercent = clampedPercent

        // Map 0%..20% linearly to 0..1000 millibels (0..10dB)
        val targetGainMb = ((clampedPercent.toFloat() / AudioConstants.MAX_SAFE_BOOST_PERCENT) *
                AudioConstants.MAX_SAFE_GAIN_MB).toInt()

        Log.d(TAG, "Applying boost: $clampedPercent% -> $targetGainMb mB")

        try {
            loudnessEnhancer?.let { enhancer ->
                enhancer.setTargetGain(targetGainMb)
                enhancer.enabled = (clampedPercent > 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set LoudnessEnhancer target gain", e)
        }

        // Notify running notification and repository
        updateNotification()
        AudioRepositoryImpl.getInstance(this).syncFromService(true, clampedPercent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AudioConstants.NOTIFICATION_CHANNEL_ID,
                "+ Volume",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active volume amplifier and loudness controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildCustomNotification(): Notification {
        val repo = AudioRepositoryImpl.getInstance(this)
        val currentVol = repo.getCurrentSystemVolume()

        val remoteViews = RemoteViews(packageName, R.layout.notification_volume_booster).apply {
            setTextViewText(R.id.tv_notif_boost_value, "+$currentBoostPercent%")
            setProgressBar(R.id.notif_progress, AudioConstants.MAX_SAFE_BOOST_PERCENT, currentBoostPercent, false)

            // PendingIntent for Decrease (-)
            val minusIntent = Intent(this@VolumeBoosterService, VolumeBoosterService::class.java).apply {
                action = AudioConstants.ACTION_BOOST_DECREASE
            }
            val minusPendingIntent = PendingIntent.getService(
                this@VolumeBoosterService,
                101,
                minusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.btn_notif_boost_minus, minusPendingIntent)

            // PendingIntent for Increase (+)
            val plusIntent = Intent(this@VolumeBoosterService, VolumeBoosterService::class.java).apply {
                action = AudioConstants.ACTION_BOOST_INCREASE
            }
            val plusPendingIntent = PendingIntent.getService(
                this@VolumeBoosterService,
                102,
                plusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.btn_notif_boost_plus, plusPendingIntent)
        }

        // Tap notification to open MainActivity
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AudioConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_boost)
            .setCustomContentView(remoteViews)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(AudioConstants.NOTIFICATION_ID, buildCustomNotification())
    }

    private fun stopBoosterService() {
        Log.d(TAG, "Stopping booster service and releasing audiofx + wakelock")
        isServiceRunning = false

        try {
            loudnessEnhancer?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing LoudnessEnhancer", e)
        }
        loudnessEnhancer = null

        releaseWakeLock()

        AudioRepositoryImpl.getInstance(this).syncFromService(false, 0)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VolumeBoosterService onDestroy")
        stopBoosterService()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "VolumeBoosterService"
        var isServiceRunning: Boolean = false
            private set
    }
}
