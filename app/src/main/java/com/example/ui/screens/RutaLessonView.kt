package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.model.UnitInfo
import com.example.data.model.Word
import com.example.ui.components.AppIcons
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun RutaLessonView(
    unit: UnitInfo?,
    level: Int,
    questionIndex: Int,
    totalQuestions: Int,
    hearts: Int,
    comboStreak: Int,
    currentWord: Word,
    currentType: Int,
    options: List<Word>,
    selectedOption: Word?,
    isAnswered: Boolean,
    isGameOver: Boolean,
    isLessonWon: Boolean,
    ttsManager: TtsManager,
    onExitLesson: () -> Unit,
    onSelectOption: (Word) -> Unit,
    onNextQuestion: () -> Unit,
    onRetryLesson: () -> Unit
) {
    BackHandler {
        onExitLesson()
    }

    val targetProgress = if (totalQuestions > 0) {
        val step = if (isAnswered && selectedOption == currentWord) questionIndex + 1 else questionIndex
        (step.toFloat() / totalQuestions).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 320),
        label = "lesson_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("active_lesson_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar: Back, Animated Progress Bar, Combo Pill, Hearts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onExitLesson,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Salir de la lección",
                        modifier = Modifier.size(20.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(CircleShape),
                    color = SuccessGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (comboStreak >= 2) {
                    Surface(
                        shape = CircleShape,
                        color = GoldYellow.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "🔥$comboStreak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(text = "❤️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = hearts.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${unit?.emoji ?: ""} ${unit?.nombre ?: ""} · Nivel $level (${questionIndex + 1}/$totalQuestions)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Question Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (currentType) {
                            0 -> "¿Qué significa este carácter?"
                            1 -> "¿Cómo se dice en chino?"
                            2 -> "Escucha y elige el significado"
                            else -> "Desafío sin pinyin: ¿qué significa?"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    when (currentType) {
                        0 -> {
                            Text(
                                text = currentWord.hanzi,
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelloRed
                            )
                            Text(
                                text = currentWord.pinyin,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalIconButton(
                                onClick = { ttsManager.speak(currentWord.hanzi) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = AppIcons.volumeUp,
                                    contentDescription = "Escuchar pronunciación",
                                    tint = SelloRed
                                )
                            }
                        }
                        1 -> {
                            Text(
                                text = currentWord.es,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                        2 -> {
                            Button(
                                onClick = { ttsManager.speak(currentWord.hanzi) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SelloRed),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    painter = AppIcons.volumeUp,
                                    contentDescription = "Escuchar audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca para volver a escuchar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Text(
                                text = currentWord.hanzi,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalIconButton(
                                onClick = { ttsManager.speak(currentWord.hanzi) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = AppIcons.volumeUp,
                                    contentDescription = "Escuchar pista de audio",
                                    tint = SelloRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Options
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                options.forEach { opt ->
                    val isSelected = selectedOption == opt
                    val isCorrect = opt == currentWord
                    val btnColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surface
                        isCorrect -> SuccessGreen.copy(alpha = 0.16f)
                        isSelected -> ErrorRed.copy(alpha = 0.16f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        isCorrect -> SuccessGreen
                        isSelected -> ErrorRed
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = btnColor,
                        border = BorderStroke(1.8.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isAnswered) { onSelectOption(opt) }
                            .testTag("lesson_option_${opt.hanzi}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentType == 1) "${opt.hanzi}  (${opt.pinyin})" else opt.es,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isAnswered) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correcto",
                                        tint = SuccessGreen
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Incorrecto",
                                        tint = ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Duolingo-style slide-up Feedback Banner at Bottom
        AnimatedVisibility(
            visible = isAnswered && !isGameOver && !isLessonWon,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val wasCorrect = selectedOption == currentWord
            val bannerBg = if (wasCorrect) Color(0xFFE9F6EF) else Color(0xFFFDECEC)
            val accent = if (wasCorrect) SuccessGreen else ErrorRed

            Surface(
                color = bannerBg,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (wasCorrect) "¡Excelente! 太棒了!" else "Casi... respuesta correcta:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accent
                            )
                            Text(
                                text = "${currentWord.hanzi} (${currentWord.pinyin}) — ${currentWord.es}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1C1E21)
                            )
                        }
                        IconButton(
                            onClick = { ttsManager.speak(currentWord.hanzi) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = AppIcons.volumeUp,
                                contentDescription = "Escuchar palabra",
                                tint = accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNextQuestion,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("lesson_next_button")
                    ) {
                        Text(
                            text = "CONTINUAR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Game Over Dialog
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = onExitLesson,
                title = { Text("💔 Te has quedado sin vidas") },
                text = { Text("No te preocupes. Repasa la guía de la unidad o recarga vidas en la tienda.") },
                confirmButton = {
                    Button(
                        onClick = onRetryLesson,
                        colors = ButtonDefaults.buttonColors(containerColor = SelloRed)
                    ) {
                        Text("Reintentar lección")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onExitLesson) {
                        Text("Volver a la ruta")
                    }
                }
            )
        }

        // Lesson Won Dialog
        if (isLessonWon) {
            val starsEarned = when {
                hearts >= 3 -> 3
                hearts == 2 -> 2
                else -> 1
            }
            AlertDialog(
                onDismissRequest = onExitLesson,
                title = { Text("🎉 ¡Lección completada!", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) { i ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < starsEarned) GoldYellow else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Has completado el Nivel $level de ${unit?.nombre}.\n+${15 + level * 5} XP · +3 monedas 🪙",
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onExitLesson,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Volver a la ruta")
                    }
                }
            )
        }
    }
}
