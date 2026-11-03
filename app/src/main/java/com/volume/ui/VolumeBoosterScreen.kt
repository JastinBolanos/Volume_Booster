package com.volume.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volume.domain.AudioConstants
import com.volume.domain.model.AudioState
import com.volume.ui.theme.BoosterAmber
import com.volume.ui.theme.BoosterCyan
import com.volume.ui.theme.BoosterRed
import com.volume.ui.theme.CardBorder
import com.volume.ui.theme.DarkSurface
import com.volume.ui.theme.SliderTrackInactive

@Composable
fun VolumeBoosterScreen(
    state: AudioState,
    onVolumeChange: (Int) -> Unit,
    onBoostChange: (Int) -> Unit,
    onStopService: () -> Unit,
    onStartService: () -> Unit,
    onToggleMute: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fondo transparente para ver la pantalla de inicio por detrás
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Tocar el área exterior translúcida minimiza la interfaz manteniendo el servicio activo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )

        // Caja flotante inferior ultra compacta y delgada
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Prevenir clicks no deseados */ },
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de arrastre sutil y muy delgada
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // BOTÓN DE APAGAR O PRENDER ULTRA DELGADO (34dp)
                if (state.isServiceRunning) {
                    Button(
                        onClick = onStopService,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("stop_service_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoosterRed,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Apagar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "APAGAR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onStartService,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("start_service_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoosterCyan,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Prender",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRENDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // LÍNEA DESLIZABLE 1: VOLUMEN (Ultra delgada, sin textos de cabecera)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volumen",
                        tint = BoosterCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleMute() }
                    )
                    ThinSlider(
                        value = state.volumePercent.toFloat(),
                        onValueChange = { onVolumeChange(it.toInt()) },
                        valueRange = 0f..100f,
                        activeColor = BoosterCyan,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("volume_slider")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // LÍNEA DESLIZABLE 2: BOOST (Signo de volumen rojo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Boost Volumen",
                        tint = BoosterRed,
                        modifier = Modifier.size(16.dp)
                    )
                    ThinSlider(
                        value = state.boostPercent.toFloat(),
                        onValueChange = { onBoostChange(it.toInt()) },
                        valueRange = 0f..AudioConstants.MAX_SAFE_BOOST_PERCENT.toFloat(),
                        activeColor = BoosterRed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("boost_slider")
                    )
                }
            }
        }
    }
}

/**
 * Control deslizante sumamente delgado que no ocupa espacio en pantalla
 */
@Composable
private fun ThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    inactiveColor: Color = SliderTrackInactive,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val activeWidth = totalWidth * fraction

        // Línea inactiva ultra delgada (3dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(inactiveColor)
        )

        // Línea activa ultra delgada (3dp)
        Box(
            modifier = Modifier
                .width(activeWidth)
                .height(3.dp)
                .clip(CircleShape)
                .background(activeColor)
        )

        // Tirador / punto sutil (10dp)
        val thumbOffset = (activeWidth - 5.dp).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(10.dp)
                .clip(CircleShape)
                .background(activeColor)
                .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        )
    }
}
