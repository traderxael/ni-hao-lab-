package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.components.AppIcons
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun PinyinScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val pool = remember { store.getFilteredPool() }
    var currentWord by remember { mutableStateOf(pool.randomOrNull() ?: VocabData.VOCAB.first()) }
    var inputText by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var hintText by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }

    val vowels = listOf(
        "ā", "á", "ǎ", "à",
        "ē", "é", "ě", "è",
        "ī", "í", "ǐ", "ì",
        "ō", "ó", "ǒ", "ò",
        "ū", "ú", "ǔ", "ù",
        "ǖ", "ǘ", "ǚ", "ǜ"
    )

    fun nextQuestion() {
        val next = pool.random()
        currentWord = next
        inputText = ""
        isAnswered = false
        isCorrect = false
        hintText = null
    }

    LaunchedEffect(Unit) {
        nextQuestion()
    }

    fun cleanPinyin(s: String): String {
        return s.trim().lowercase().replace(" ", "")
    }

    fun checkAnswer() {
        if (inputText.isBlank()) return
        val target = cleanPinyin(currentWord.pinyin)
        val user = cleanPinyin(inputText)
        val ok = (user == target)
        isAnswered = true
        isCorrect = ok
        store.trackWord(currentWord.hanzi, ok)
        ttsManager.speak(currentWord.hanzi)

        if (ok) {
            score += 10
            store.addXP(8)
            store.addCoins(1)
        }
        store.trackGame("pinyin", ok, score)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("pinyin_screen")
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

        // Question Card
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
                    text = "Escribe el Pinyin con tonos",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = currentWord.hanzi,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelloRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentWord.es,
                    fontSize = 15.sp,
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

                if (hintText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pista: $hintText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE17055)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field
        OutlinedTextField(
            value = inputText,
            onValueChange = { if (!isAnswered) inputText = it },
            placeholder = { Text("Ej: ${currentWord.pinyin.take(2)}...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pinyin_input_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        val firstPart = currentWord.pinyin.split(" ").firstOrNull() ?: currentWord.pinyin.take(2)
                        hintText = "Empieza por: $firstPart"
                    }
                ) {
                    Icon(
                        painter = AppIcons.lightbulb,
                        contentDescription = "Pista",
                        tint = Color(0xFFF5A623),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tone accents shortcut bar
        Text(
            text = "Acceso rápido a vocales con tono:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            vowels.forEach { v ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isAnswered) { inputText += v }
                ) {
                    Text(
                        text = v,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Result Banner
        if (isAnswered) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isCorrect) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isCorrect) "✅ ¡Correcto!" else "❌ Respuesta correcta: ${currentWord.pinyin}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isCorrect) SuccessGreen else ErrorRed
                    )
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                if (isAnswered) nextQuestion() else checkAnswer()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAnswered) MaterialTheme.colorScheme.onSurface else SelloRed
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("pinyin_submit_button")
        ) {
            Text(
                text = if (isAnswered) "Siguiente →" else "Comprobar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
