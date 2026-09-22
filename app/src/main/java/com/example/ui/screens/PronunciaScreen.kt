package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
fun PronunciaScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pool = remember { store.getFilteredPool() }
    var currentWord by remember { mutableStateOf(pool.randomOrNull() ?: VocabData.VOCAB.first()) }
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var isEvaluated by remember { mutableStateOf(false) }
    var isPassed by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted
        } else {
            store.toast("Se necesita permiso de micrófono para evaluar")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startListening() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (speechRecognizer == null) {
            // Self-assessment mode if device speech recognition is missing in container
            isListening = false
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val heard = matches?.firstOrNull() ?: ""
                recognizedText = heard
                val ok = heard.contains(currentWord.hanzi) || heard.contains(currentWord.pinyin.replace(" ", ""))
                isPassed = ok
                isEvaluated = true

                if (ok) {
                    correctCount++
                    score += 10
                    store.addXP(8)
                    store.addCoins(1)
                    if (correctCount >= 3) store.maybeAchv("voz")
                }
                store.trackGame("pronuncia", ok, score)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
        isListening = true
    }

    fun nextWord() {
        currentWord = pool.random()
        recognizedText = ""
        isEvaluated = false
        isPassed = false
        ttsManager.speak(currentWord.hanzi)
    }

    LaunchedEffect(Unit) {
        ttsManager.speak(currentWord.hanzi)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("pronuncia_screen")
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

        // Target Card
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
                    text = "Shadowing · Repite en voz alta",
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
                    text = currentWord.pinyin,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentWord.es,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { ttsManager.speak(currentWord.hanzi, slow = false) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(AppIcons.volumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Normal (1x)")
                    }
                    OutlinedButton(
                        onClick = { ttsManager.speak(currentWord.hanzi, slow = true) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🐢 Lento (0.5x)")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Microphone Hero Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (isListening) {
                        speechRecognizer?.stopListening()
                        isListening = false
                    } else {
                        startListening()
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) ErrorRed else AccentBlue
                ),
                modifier = Modifier
                    .size(88.dp)
                    .testTag("pronounce_mic_button")
            ) {
                Icon(
                    painter = if (isListening) AppIcons.micOff else AppIcons.mic,
                    contentDescription = "Grabar voz",
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isListening) "🎙️ Escuchando... Habla ahora" else "Toca el micro para pronunciar",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Self-assessment option
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    // Manual pass verification
                    isEvaluated = true
                    isPassed = true
                    correctCount++
                    score += 10
                    store.addXP(8)
                    store.addCoins(1)
                    if (correctCount >= 3) store.maybeAchv("voz")
                    store.trackGame("pronuncia", true, score)
                }
            ) {
                Text("¿Lo pronunciaste bien? Marcar como correcto ✅", fontSize = 12.sp)
            }

            if (isEvaluated) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPassed) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPassed) "🎉 ¡Muy buena pronunciación! (+8 XP)" else "Intentémoslo de nuevo",
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) SuccessGreen else ErrorRed,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Next Button
        AnimatedVisibility(visible = isEvaluated) {
            Button(
                onClick = { nextWord() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Siguiente palabra →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
