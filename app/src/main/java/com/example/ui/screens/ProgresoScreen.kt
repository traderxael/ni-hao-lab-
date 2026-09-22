package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppState
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.ui.components.Streak30DaysSection
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun ProgresoScreen(
    store: StoreRepository,
    state: AppState,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    val dominadas = store.wordProgressMap.values.count { it.box >= 4 }
    val aprendiendo = store.wordProgressMap.values.count { it.box in 2..3 }
    val debiles = store.wordProgressMap.values.count { it.fail > 0 && it.box <= 2 }
    val totalWords = VocabData.VOCAB.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("progreso_screen")
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

            Text(
                text = "📊 Estadísticas y Logros",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Resetear datos", tint = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${state.xp} XP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SelloRed)
                            Text("Nivel ${(state.xp / 100) + 1}", fontSize = 11.sp, color = AccentBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Racha actual", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${state.racha} días 🔥", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF5A623))
                            Text("Meta: ${state.goal} XP/día", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 30-Day Streak Consistency Section
            item {
                Streak30DaysSection(store = store)
            }

            // Leitner Boxes Distribution
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧠 Estado del Vocabulario",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🏆 Dominadas: $dominadas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                            Text("🌱 Aprendiendo: $aprendiendo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentBlue)
                            Text("⚠️ Débiles: $debiles", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (dominadas.toFloat() / totalWords).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = SuccessGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$dominadas de $totalWords palabras dominadas (${(dominadas * 100 / totalWords)}%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Game Records
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎮 Récords de Minijuegos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val recs = listOf(
                            "Quiz" to "${store.gameStatsMap["quiz"]?.best ?: 0} pts (jugados: ${store.gameStatsMap["quiz"]?.played ?: 0})",
                            "Memorama" to "${store.memWins} victorias · Mejor: ${store.memBestMap[state.memPairs] ?: "—"}",
                            "Escucha" to "${store.gameStatsMap["escucha"]?.best ?: 0} pts",
                            "Pinyin" to "${store.gameStatsMap["pinyin"]?.best ?: 0} pts",
                            "Tonos" to "${store.gameStatsMap["tonos"]?.best ?: 0} pts",
                            "Pronuncia" to "${store.gameStatsMap["pronuncia"]?.best ?: 0} pts",
                            "Escribe" to "${store.gameStatsMap["escribe"]?.best ?: 0} pts"
                        )
                        recs.forEach { (game, stat) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(game, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(stat, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Achievements Gallery
            item {
                Text(
                    text = "🏆 Galería de Logros (${store.achievementsMap.size}/${VocabData.LOGROS.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(VocabData.LOGROS) { achv ->
                val isUnlocked = store.achievementsMap.containsKey(achv.id)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shadowElevation = if (isUnlocked) 1.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = achv.emoji,
                            fontSize = 32.sp,
                            color = if (isUnlocked) Color.Unspecified else Color.Gray
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = achv.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = achv.desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isUnlocked) {
                            Surface(
                                shape = CircleShape,
                                color = SuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Desbloqueado ✅",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reset Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("⚠️ ¿Resetear progreso?") },
                text = { Text("¿Deseas reiniciar tu progreso? Se restablecerán tus palabras, estadísticas y logros.") },
                confirmButton = {
                    Button(
                        onClick = {
                            store.resetAll()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Sí, resetear todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
