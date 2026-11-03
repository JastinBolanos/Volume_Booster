package com.volume

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.volume.domain.AudioSafetyConstants
import com.volume.domain.model.AudioState
import com.volume.domain.repository.AudioRepository
import com.volume.domain.usecase.GetAudioStateUseCase
import com.volume.domain.usecase.SetBoostUseCase
import com.volume.domain.usecase.SetVolumeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("+ Volume", appName)
    }

    @Test
    fun `audio safety constants strict bounds`() {
        assertEquals(20, AudioSafetyConstants.MAX_SAFE_BOOST_PERCENT)
        assertEquals(1000, AudioSafetyConstants.MAX_SAFE_GAIN_MB)
        assertEquals(5, AudioSafetyConstants.BOOST_STEP_PERCENT)
    }

    @Test
    fun `domain usecase enforces strict 20 percent boost invariant`() {
        var recordedBoost = -1
        val fakeRepository = object : AudioRepository {
            override val audioState: StateFlow<AudioState> = MutableStateFlow(AudioState())
            override fun setSystemVolume(percent: Int) {}
            override fun setBoost(percent: Int) { recordedBoost = percent }
            override fun toggleMute() {}
            override fun startService() {}
            override fun stopService() {}
            override fun getCurrentSystemVolume(): Int = 50
        }

        val setBoostUseCase = SetBoostUseCase(fakeRepository)

        // Test over-boost clamp
        setBoostUseCase(50)
        assertEquals(20, recordedBoost)

        // Test negative clamp
        setBoostUseCase(-10)
        assertEquals(0, recordedBoost)

        // Test normal value
        setBoostUseCase(15)
        assertEquals(15, recordedBoost)
    }

    @Test
    fun `audio state db formatting`() {
        val state = AudioState(boostPercent = 20, currentGainMb = 1000)
        assertEquals("+10.0 dB", state.gainDbText)

        val stateHalf = AudioState(boostPercent = 10, currentGainMb = 500)
        assertEquals("+5.0 dB", stateHalf.gainDbText)
    }
}
