package com.volume

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.volume.ui.VolumeBoosterScreen
import com.volume.ui.VolumeBoosterViewModel
import com.volume.ui.theme.VolumeBoosterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VolumeBoosterViewModel by viewModels {
        (application as VolumeBoosterApplication).container.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VolumeBoosterTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                // Permission launcher for POST_NOTIFICATIONS on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Handle result */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Pressing Android back button minimizes the overlay to system home screen
                BackHandler {
                    moveTaskToBack(true)
                }

                VolumeBoosterScreen(
                    state = state,
                    onVolumeChange = { viewModel.onVolumeChange(it) },
                    onBoostChange = { viewModel.onBoostChange(it) },
                    onStopService = { viewModel.stopService() },
                    onStartService = { viewModel.startService() },
                    onToggleMute = { viewModel.toggleMute() },
                    onDismiss = {
                        // Minimize overlay and reveal launcher / background application
                        moveTaskToBack(true)
                    }
                )
            }
        }
    }
}
