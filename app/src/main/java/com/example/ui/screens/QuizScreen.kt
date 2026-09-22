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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.data.model.Word
import com.example.ui.components.AppIcons
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val pool = remember { store.getFilteredPool() }
    var currentWord by remember { mutableStateOf(pool.randomOrNull() ?: VocabData.VOCAB.first()) }
    var questionType by remember { mutableStateOf(0) } // 0: Hanzi -> ES, 1: ES -> Hanzi
    var options by remember { mutableStateOf(emptyList<Word>()) }
    var selectedOption by remember { mutableStateOf<Word?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var streak by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var questionsAnswered by remember { mutableStateOf(0) }

    // Generates a new question
    fun nextQuestion() {
        val next = pool.random()
        currentWord = next
        questionType = (0..1).random()
        val distractors = pool.filter { it.hanzi != next.hanzi }.shuffled().take(3)
        options = (distractors + next).shuffled()
        selectedOption = null
        isAnswered = false
        if (questionType == 0) {
            ttsManager.speak(next.hanzi)
        }
    }

    LaunchedEffect(Unit) {
        nextQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("quiz_screen")
    ) {
        // Top bar
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF5A623).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🔥 x$streak ${if (streak >= 3) "(x2 XP)" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5A623),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (questionType == 0) {
                    Text(
                        text = "¿Qué significa?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentWord.hanzi,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelloRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentWord.pinyin,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { ttsManager.speak(currentWord.hanzi) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            painter = AppIcons.volumeUp,
                            contentDescription = "Escuchar",
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = "¿Cómo se dice en chino?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentWord.es,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            options.forEach { opt ->
                val isSelected = selectedOption == opt
                val isCorrect = opt == currentWord
                val btnColor = when {
                    !isAnswered -> MaterialTheme.colorScheme.surface
                    isCorrect -> SuccessGreen.copy(alpha = 0.15f)
                    isSelected -> ErrorRed.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    isCorrect -> SuccessGreen
                    isSelected -> ErrorRed
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                            questionsAnswered++
                            val correct = (opt == currentWord)
                            store.trackWord(currentWord.hanzi, correct)
                            ttsManager.speak(currentWord.hanzi)

                            if (correct) {
                                streak++
                                val xpGain = if (streak >= 3) 10 else 5
                                score += 10
                                store.addXP(xpGain)
                                store.addCoins(1)
                                if (streak >= 5) store.maybeAchv("quiz5")
                            } else {
                                streak = 0
                            }
                            store.trackGame("quiz", correct, score)
                        }
                        .testTag("quiz_option_${opt.hanzi}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (questionType == 0) opt.es else "${opt.hanzi} (${opt.pinyin})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isAnswered) {
                            if (isCorrect) {
                                Text("✅", fontSize = 16.sp)
                            } else if (isSelected) {
                                Text("❌", fontSize = 16.sp)
                            }
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
                    .testTag("quiz_next_button")
            ) {
                Text("Siguiente →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
