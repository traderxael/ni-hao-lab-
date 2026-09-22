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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun EscuchaScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val pool = remember { store.getFilteredPool() }
    var currentWord by remember { mutableStateOf(pool.randomOrNull() ?: VocabData.VOCAB.first()) }
    var options by remember { mutableStateOf(emptyList<Word>()) }
    var selectedOption by remember { mutableStateOf<Word?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }

    fun nextQuestion() {
        val next = pool.random()
        currentWord = next
        val distractors = pool.filter { it.hanzi != next.hanzi }.shuffled().take(3)
        options = (distractors + next).shuffled()
        selectedOption = null
        isAnswered = false
        ttsManager.speak(next.hanzi)
    }

    LaunchedEffect(Unit) {
        nextQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("escucha_screen")
    ) {
        // Top Header
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
                        color = SuccessGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🔥 x$streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Audio Player Card
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
                Text(
                    text = "Escucha atentamente",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { ttsManager.speak(currentWord.hanzi, slow = false) },
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

                OutlinedButton(
                    onClick = { ttsManager.speak(currentWord.hanzi, slow = true) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🐢 Lento (0.5x)", fontSize = 12.sp)
                }

                if (isAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentWord.hanzi,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelloRed
                    )
                    Text(
                        text = currentWord.pinyin,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            val correct = (opt == currentWord)
                            store.trackWord(currentWord.hanzi, correct)

                            if (correct) {
                                streak++
                                score += 10
                                store.addXP(6)
                                store.addCoins(1)
                            } else {
                                streak = 0
                            }
                            store.trackGame("escucha", correct, score)
                        }
                        .testTag("escucha_option_${opt.es}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = opt.es,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                Text("Siguiente →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
