package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.data.model.ToneOption
import com.example.data.model.TonePair
import com.example.ui.components.AppIcons
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun TonosScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(0) } // 0: Pares mínimos, 1: Identificar tono
    var currentPair by remember { mutableStateOf(VocabData.PARES_TONO.random()) }
    var targetOption by remember { mutableStateOf(currentPair.opts.random()) }
    var selectedOption by remember { mutableStateOf<ToneOption?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    fun nextQuestion() {
        val nextPair = VocabData.PARES_TONO.random()
        currentPair = nextPair
        targetOption = nextPair.opts.random()
        selectedOption = null
        isAnswered = false
        ttsManager.speak(targetOption.hz)
    }

    LaunchedEffect(Unit) {
        nextQuestion()
    }

    val toneDescriptions = listOf(
        "1º Tono: Alto y plano (—)",
        "2º Tono: Ascendente (／)",
        "3º Tono: Baja y sube (∨)",
        "4º Tono: Descendente cortante (＼)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("tonos_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "⭐ $score pts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question Hero
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Qué tono estás escuchando?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { ttsManager.speak(targetOption.hz) },
                    colors = ButtonDefaults.buttonColors(containerColor = SelloRed),
                    shape = CircleShape,
                    modifier = Modifier.size(76.dp)
                ) {
                    Icon(
                        painter = AppIcons.volumeUp,
                        contentDescription = "Escuchar",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sílaba: ${currentPair.sil}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isAnswered) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${targetOption.hz} (${targetOption.py}) = ${targetOption.es}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelloRed
                    )
                    Text(
                        text = toneDescriptions[targetOption.t - 1],
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Tone Options
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            currentPair.opts.forEach { opt ->
                val isSelected = selectedOption == opt
                val isCorrect = opt == targetOption
                val btnColor = when {
                    !isAnswered -> MaterialTheme.colorScheme.surface
                    isCorrect -> SuccessGreen.copy(alpha = 0.15f)
                    isSelected -> ErrorRed.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    isCorrect -> SuccessGreen
                    isSelected -> ErrorRed
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = btnColor,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAnswered) {
                            selectedOption = opt
                            isAnswered = true
                            val correct = (opt == targetOption)
                            if (correct) {
                                score += 10
                                store.addXP(6)
                                store.addCoins(1)
                            }
                            store.trackGame("tonos", correct, score)
                        }
                        .testTag("tone_option_${opt.t}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { ttsManager.speak(opt.hz) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    painter = AppIcons.volumeUp,
                                    contentDescription = "Escuchar tono",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${opt.t}º tono: ${opt.py}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${opt.hz} — ${opt.es}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isAnswered) {
                            if (isCorrect) Text("✅", fontSize = 16.sp)
                            else if (isSelected) Text("❌", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Next Button
        AnimatedVisibility(visible = isAnswered) {
            Button(
                onClick = { nextQuestion() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Siguiente tono →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
