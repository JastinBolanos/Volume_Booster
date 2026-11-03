package com.volume.di

import android.content.Context
import com.volume.data.repository.AudioRepositoryImpl
import com.volume.domain.repository.AudioRepository
import com.volume.domain.usecase.GetAudioStateUseCase
import com.volume.domain.usecase.SetBoostUseCase
import com.volume.domain.usecase.SetVolumeUseCase
import com.volume.domain.usecase.ToggleMuteUseCase
import com.volume.domain.usecase.ToggleServiceUseCase
import com.volume.ui.VolumeBoosterViewModel

/**
 * Dependency Injection container ensuring Dependency Inversion.
 * Data implementations are injected via Domain interfaces into Domain UseCases.
 */
interface AppContainer {
    val audioRepository: AudioRepository
    val getAudioStateUseCase: GetAudioStateUseCase
    val setVolumeUseCase: SetVolumeUseCase
    val setBoostUseCase: SetBoostUseCase
    val toggleServiceUseCase: ToggleServiceUseCase
    val toggleMuteUseCase: ToggleMuteUseCase
    val viewModelFactory: VolumeBoosterViewModel.Factory
}

class DefaultAppContainer(
    private val appContext: Context
) : AppContainer {

    override val audioRepository: AudioRepository by lazy {
        AudioRepositoryImpl.getInstance(appContext)
    }

    override val getAudioStateUseCase: GetAudioStateUseCase by lazy {
        GetAudioStateUseCase(audioRepository)
    }

    override val setVolumeUseCase: SetVolumeUseCase by lazy {
        SetVolumeUseCase(audioRepository)
    }

    override val setBoostUseCase: SetBoostUseCase by lazy {
        SetBoostUseCase(audioRepository)
    }

    override val toggleServiceUseCase: ToggleServiceUseCase by lazy {
        ToggleServiceUseCase(audioRepository)
    }

    override val toggleMuteUseCase: ToggleMuteUseCase by lazy {
        ToggleMuteUseCase(audioRepository)
    }

    override val viewModelFactory: VolumeBoosterViewModel.Factory by lazy {
        VolumeBoosterViewModel.Factory(
            getAudioStateUseCase = getAudioStateUseCase,
            setVolumeUseCase = setVolumeUseCase,
            setBoostUseCase = setBoostUseCase,
            toggleServiceUseCase = toggleServiceUseCase,
            toggleMuteUseCase = toggleMuteUseCase
        )
    }
}
