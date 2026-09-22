package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.TtsManager
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.data.model.Word
import com.example.ui.theme.*

private enum class QuestionKind {
    HANZI_TO_ES,
    ES_TO_HANZI,
    HANZI_TO_PINYIN
}

private data class MiniQuizQuestion(
    val targetWord: Word,
    val kind: QuestionKind,
    val options: List<Word>
)

@Composable
fun PracticaRapidaModal(
    store: StoreRepository,
    ttsManager: TtsManager,
    onDismiss: () -> Unit
) {
    var questions by remember { mutableStateOf<List<MiniQuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedWord by remember { mutableStateOf<Word?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var testedWords by remember { mutableStateOf<List<Pair<Word, Boolean>>>(emptyList()) }

    fun generateQuestions() {
        val debiles = store.getDebilWords()
        val allVocab = VocabData.VOCAB

        // Take up to 3 weak words
        val selectedTargets = if (debiles.isNotEmpty()) {
            val shuffledDebiles = debiles.shuffled()
            if (shuffledDebiles.size >= 3) {
                shuffledDebiles.take(3)
            } else {
                // Pad with challenging words if fewer than 3 are currently weak
                val needed = 3 - shuffledDebiles.size
                val fillers = allVocab.filter { w -> !shuffledDebiles.any { it.hanzi == w.hanzi } }
                    .shuffled()
                    .take(needed)
                shuffledDebiles + fillers
            }
        } else {
            emptyList()
        }

        if (selectedTargets.isNotEmpty()) {
            val kinds = listOf(
                QuestionKind.HANZI_TO_ES,
                QuestionKind.ES_TO_HANZI,
                QuestionKind.HANZI_TO_PINYIN
            )

            questions = selectedTargets.mapIndexed { idx, target ->
                val distractors = allVocab
                    .filter { it.hanzi != target.hanzi }
                    .shuffled()
                    .take(3)
                val options = (distractors + target).shuffled()
                val kind = kinds[idx % kinds.size]
                MiniQuizQuestion(targetWord = target, kind = kind, options = options)
            }
        } else {
            questions = emptyList()
        }

        currentIndex = 0
        selectedWord = null
        isAnswered = false
        correctCount = 0
        isFinished = false
        testedWords = emptyList()
    }

    LaunchedEffect(Unit) {
        generateQuestions()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp))
                .testTag("practica_rapida_modal"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.5.dp, GoldYellow.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Práctica Rápida",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "3 preguntas de palabras débiles",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("close_practica_rapida_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (questions.isEmpty()) {
                    // Empty state: No weak words yet
                    EmptyWeakWordsView(
                        onSeedWords = {
                            store.seedDefaultDebilWords()
                            generateQuestions()
                        },
                        onDismiss = onDismiss
                    )
                } else if (!isFinished) {
                    // Quiz in progress
                    val currentQ = questions[currentIndex]

                    // Progress bar & Step counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pregunta ${currentIndex + 1} de 3",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelloRed
                        )
                        Text(
                            text = "Aciertos: $correctCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1) / 3f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = GoldYellow,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question Card
                    QuestionCardView(
                        question = currentQ,
                        ttsManager = ttsManager
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Options Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentQ.options.forEachIndexed { optIndex, optionWord ->
                            val isTarget = optionWord.hanzi == currentQ.targetWord.hanzi
                            val isSelected = selectedWord?.hanzi == optionWord.hanzi

                            val backgroundColor = when {
                                !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                isTarget -> SuccessGreen.copy(alpha = 0.2f)
                                isSelected -> ErrorRed.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }

                            val borderColor = when {
                                !isAnswered -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                isTarget -> SuccessGreen
                                isSelected -> ErrorRed
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = backgroundColor,
                                border = BorderStroke(1.5.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAnswered) {
                                        selectedWord = optionWord
                                        isAnswered = true
                                        val correct = isTarget
                                        if (correct) {
                                            correctCount++
                                            store.trackWord(currentQ.targetWord.hanzi, ok = true)
                                            store.addXP(10)
                                        } else {
                                            store.trackWord(currentQ.targetWord.hanzi, ok = false)
                                        }
                                        testedWords = testedWords + (currentQ.targetWord to correct)
                                        ttsManager.speak(currentQ.targetWord.hanzi)
                                    }
                                    .testTag("quiz_option_$optIndex")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val displayText = when (currentQ.kind) {
                                        QuestionKind.HANZI_TO_ES -> optionWord.es
                                        QuestionKind.ES_TO_HANZI -> "${optionWord.hanzi} (${optionWord.pinyin})"
                                        QuestionKind.HANZI_TO_PINYIN -> optionWord.pinyin
                                    }

                                    Text(
                                        text = displayText,
                                        fontSize = if (currentQ.kind == QuestionKind.ES_TO_HANZI) 16.sp else 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered) {
                                        if (isTarget) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Correcto",
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Incorrecto",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next / Action Button
                    if (isAnswered) {
                        Button(
                            onClick = {
                                if (currentIndex < 2) {
                                    currentIndex++
                                    selectedWord = null
                                    isAnswered = false
                                } else {
                                    isFinished = true
                                    // Award extra completion bonus
                                    store.addXP(20)
                                    store.addCoins(5)
                                    if (correctCount >= 2) {
                                        store.triggerConfetti()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentIndex < 2) SelloRed else SuccessGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("next_question_button")
                        ) {
                            Text(
                                text = if (currentIndex < 2) "Siguiente Pregunta ➔" else "Ver Resultados 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Summary View
                    MiniQuizSummaryView(
                        correctCount = correctCount,
                        testedWords = testedWords,
                        onRetry = { generateQuestions() },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCardView(
    question: MiniQuizQuestion,
    ttsManager: TtsManager
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (question.kind) {
                QuestionKind.HANZI_TO_ES -> {
                    Text(
                        text = "¿Qué significa este carácter?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.targetWord.hanzi,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = SelloRed
                    )
                    Text(
                        text = question.targetWord.pinyin,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AccentBlue
                    )
                }
                QuestionKind.ES_TO_HANZI -> {
                    Text(
                        text = "¿Cuál carácter significa...?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${question.targetWord.es}\"",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                QuestionKind.HANZI_TO_PINYIN -> {
                    Text(
                        text = "¿Cuál es el pīnyīn y tono correcto?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.targetWord.hanzi,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = SelloRed
                    )
                    Text(
                        text = "Significa: ${question.targetWord.es}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio helper button
            IconButton(
                onClick = { ttsManager.speak(question.targetWord.hanzi) },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.15f))
            ) {
                Icon(
                    painter = AppIcons.volumeUp,
                    contentDescription = "Escuchar pronunciación",
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniQuizSummaryView(
    correctCount: Int,
    testedWords: List<Pair<Word, Boolean>>,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val emoji = when (correctCount) {
            3 -> "🏆"
            2 -> "👏"
            1 -> "💪"
            else -> "🌱"
        }

        val title = when (correctCount) {
            3 -> "¡3 de 3! ¡Dominio total!"
            2 -> "¡2 de 3! ¡Gran progreso!"
            1 -> "1 de 3. ¡Sigue repasando!"
            else -> "¡Buen intento! La clave es la repetición."
        }

        Text(text = emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Has fortalecido tus palabras débiles en la memoria",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Rewards badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GoldYellow.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "+${correctCount * 10 + 20} XP ⚡", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SelloRed)
                Text(text = "+5 Monedas 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tested words breakdown
        Text(
            text = "Palabras repasadas:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            testedWords.forEach { (word, isOk) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = word.hanzi, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelloRed)
                            Text(text = word.pinyin, fontSize = 12.sp, color = AccentBlue)
                            Text(text = "· ${word.es}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = if (isOk) "✓ Mejorada" else "✕ Repasar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOk) SuccessGreen else ErrorRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("retry_practica_rapida_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Otra ronda", fontSize = 13.sp)
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelloRed),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("finish_practica_rapida_button")
            ) {
                Text("Listo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun EmptyWeakWordsView(
    onSeedWords: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🌱", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¡Aún no tienes palabras débiles!",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Las palabras se marcan como débiles automáticamente cuando fallas en los juegos o cuando pulsas 'Marcar débil' en el cuaderno.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSeedWords,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("seed_debil_words_button")
        ) {
            Text(
                text = "⚡ Añadir 5 palabras desafiantes ahora",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDismiss) {
            Text("Volver al cuaderno", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
