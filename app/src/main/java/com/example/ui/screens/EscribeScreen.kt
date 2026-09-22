package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

data class DrawPoint(val x: Float, val y: Float)

@Composable
fun EscribeScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val pool = remember { store.getFilteredPool() }
    var currentWord by remember { mutableStateOf(pool.randomOrNull() ?: VocabData.VOCAB.first()) }
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }
    var strokeCoverage by remember { mutableStateOf(0) }
    var isEvaluated by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var qualifiedCount by remember { mutableStateOf(0) }

    fun nextWord() {
        currentWord = pool.random()
        paths.clear()
        currentPath = emptyList()
        strokeCoverage = 0
        isEvaluated = false
        ttsManager.speak(currentWord.hanzi)
    }

    LaunchedEffect(Unit) {
        ttsManager.speak(currentWord.hanzi)
    }

    fun evaluateDrawing() {
        val totalPoints = paths.sumOf { it.size }
        // Simple heuristic for user having drawn substantial strokes
        val coverage = (totalPoints * 2).coerceIn(15, 95)
        strokeCoverage = coverage
        isEvaluated = true
        val passed = coverage >= 60

        if (passed) {
            qualifiedCount++
            score += 10
            store.addXP(8)
            store.addCoins(1)
            if (qualifiedCount >= 3) store.maybeAchv("trazo")
        }
        store.trackGame("escribe", passed, score)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("escribe_screen")
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

        Spacer(modifier = Modifier.height(8.dp))

        // Info Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Traza el carácter",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentWord.pinyin} · ${currentWord.es}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (currentWord.rad != null) {
                    Text(
                        text = "Radical: ${currentWord.rad} (${currentWord.mnemo ?: ""})",
                        fontSize = 11.sp,
                        color = AccentBlue
                    )
                }
            }

            IconButton(
                onClick = { ttsManager.speak(currentWord.hanzi) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    painter = AppIcons.volumeUp,
                    contentDescription = "Audio",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Drawing Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .testTag("drawing_canvas"),
            contentAlignment = Alignment.Center
        ) {
            // Background Ghost Hanzi Guide
            Text(
                text = currentWord.hanzi,
                fontSize = 180.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            // Drawing Canvas with Mi-zi-ge grid guidelines
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath = currentPath + change.position
                            },
                            onDragEnd = {
                                if (currentPath.isNotEmpty()) {
                                    paths.add(currentPath)
                                    currentPath = emptyList()
                                }
                            }
                        )
                    }
            ) {
                // Draw grid lines
                val w = size.width
                val h = size.height
                val gridColor = Color.LightGray.copy(alpha = 0.25f)
                drawLine(gridColor, Offset(w / 2, 0f), Offset(w / 2, h), strokeWidth = 1.5f)
                drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 1.5f)
                drawLine(gridColor, Offset(0f, 0f), Offset(w, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(w, 0f), Offset(0f, h), strokeWidth = 1f)

                // Draw completed paths
                for (p in paths) {
                    if (p.size > 1) {
                        val path = Path().apply {
                            moveTo(p.first().x, p.first().y)
                            for (i in 1 until p.size) {
                                lineTo(p[i].x, p[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = SelloRed,
                            style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                // Draw active path
                if (currentPath.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPath.first().x, currentPath.first().y)
                        for (i in 1 until currentPath.size) {
                            lineTo(currentPath[i].x, currentPath[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = SelloRed,
                        style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas controls: Clear button & Evaluation feedback
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    paths.clear()
                    currentPath = emptyList()
                    isEvaluated = false
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Limpiar lienzo", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpiar", fontSize = 12.sp)
            }

            if (isEvaluated) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (strokeCoverage >= 60) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (strokeCoverage >= 60) "🎉 $strokeCoverage% de cobertura (Aprobado)" else "⚠️ $strokeCoverage% (Completa todos los trazos)",
                        fontWeight = FontWeight.Bold,
                        color = if (strokeCoverage >= 60) SuccessGreen else ErrorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button
        Button(
            onClick = {
                if (isEvaluated) nextWord() else evaluateDrawing()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEvaluated) MaterialTheme.colorScheme.onSurface else SelloRed
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("escribe_submit_button")
        ) {
            Text(
                text = if (isEvaluated) "Siguiente carácter →" else "Comprobar trazos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
