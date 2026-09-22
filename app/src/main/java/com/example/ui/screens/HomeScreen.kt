package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
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
import com.example.data.AppState
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.FilterChipsRow
import com.example.ui.components.StreakCelebrationDialog
import com.example.ui.components.StreakHomeCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

data class GameCardItem(
    val id: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val recordKey: String,
    val accentColor: Color
)

@Composable
fun HomeScreen(
    store: StoreRepository,
    state: AppState,
    ttsManager: TtsManager? = null,
    onNavigate: (String) -> Unit
) {
    val pool = store.getFilteredPool()
    val dueCount = store.repasoDebido()
    var showMetodo by remember { mutableStateOf(false) }
    val currentCatObj = VocabData.CATEGORIAS_TEMATICAS.find { it.id == state.filtroCat }

    val streakCelebration by store.streakCelebrationEvent.collectAsState()
    val confettiTrigger by store.confettiTrigger.collectAsState()

    LaunchedEffect(Unit) {
        store.checkDailyStreak()
    }

    LaunchedEffect(streakCelebration) {
        if (streakCelebration != null && state.sonido) {
            ttsManager?.speak("太棒了！加油！")
        }
    }

    val cards = listOf(
        GameCardItem(
            "ruta",
            "🛤️",
            "Ruta",
            if (state.filtroCat == "todas") "Camino de aprendizaje: 14 temáticas, 3 niveles." else "Lecciones de ${currentCatObj?.name ?: state.filtroCat} (3 niveles).",
            "ruta",
            SelloRed
        ),
        GameCardItem("memorama", "🃏", "Memorama", "Une cada 汉字 con su traducción en español.", "mem", Color(0xFF6C5CE7)),
        GameCardItem("quiz", "✅", "Quiz", "Opción múltiple en ambos sentidos. Racha = doble XP.", "quiz", AccentBlue),
        GameCardItem("escucha", "🔊", "Escucha", "Oye la palabra y elige qué significa.", "escucha", SuccessGreen),
        GameCardItem("pinyin", "⌨️", "Escribe pinyin", "Lee el carácter y teclea su pronunciación.", "pinyin", Color(0xFFE17055)),
        GameCardItem("tonos", "🔔", "Tonos", "Escucha y elige el tono (1º-4º) o pares mínimos.", "tonos", GoldYellow),
        GameCardItem("pronuncia", "🎤", "Pronuncia", "Imita el modelo y entrena tu voz.", "pronuncia", Color(0xFFFD79A8)),
        GameCardItem("escribe", "✍️", "Escribe", "Traza el carácter sobre la guía.", "escribe", Color(0xFF0EA5E9)),
        GameCardItem("tienda", "🪙", "Tienda", "Gasta tus monedas: vidas, racha, pistas.", "tienda", GoldYellow),
        GameCardItem("flash", "📇", "Flashcards", "Repaso de fichas con radical y mnemotecnia.", "flash", Color(0xFFD63031)),
        GameCardItem("cultura", "🏮", "Cultura", "Poemas Tang con audio y fiestas tradicionales.", "cultura", Color(0xFFA41F27)),
        GameCardItem("cuaderno", "📔", "Mi cuaderno", "Tu registro personal: qué llevas aprendido.", "cuaderno", AccentBlue)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Filters
        item(span = { GridItemSpan(maxLineSpan) }) {
            FilterChipsRow(
                selectedHsk = state.filtroHsk,
                selectedCat = state.filtroCat,
                totalWordsCount = pool.size,
                dueCount = dueCount,
                onHskChange = { store.setFiltros(it, state.filtroCat) },
                onCatChange = { store.setFiltros(state.filtroHsk, it) }
            )
        }

        // Thematic Filter Active Banner
        if (state.filtroCat != "todas" && currentCatObj != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentBlue.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().testTag("active_theme_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = currentCatObj.emoji, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Tema activo: ${currentCatObj.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentCatObj.desc} · ${pool.size} palabras",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { onNavigate("ruta") },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_ir_lecciones_tema")
                            ) {
                                Text("Ver lecciones", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { store.setFiltros(state.filtroHsk, "todas") },
                                modifier = Modifier.size(36.dp).testTag("btn_limpiar_filtro_cat")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar filtro",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Streak Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            StreakHomeCard(
                streak = state.racha.coerceAtLeast(1),
                onCelebrate = { store.triggerStreakCelebrationManual() }
            )
        }

        // Daily Session Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("quiz") }
                    .testTag("daily_session_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🎯", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Sesión diaria",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (dueCount > 0) "$dueCount palabras te esperan hoy" else "Repaso al día · ¡Sigue así!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Empezar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Práctica Rápida Shortcut
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.5f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("cuaderno") }
                    .testTag("home_practica_rapida_shortcut")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "⚡", fontSize = 26.sp)
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Práctica Rápida",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = SelloRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "3 preguntas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelloRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Refuerza tus palabras débiles en el cuaderno",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ir a cuaderno",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Metodo Accordion
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMetodo = !showMetodo }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧠 Cómo aprende tu cerebro chino",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (showMetodo) "▲" else "▼",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(visible = showMetodo) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("1. Poco y cada día — el repaso espaciado (cajas Leitner) te trae cada palabra a tiempo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("2. Tonos primero — el tono cambia el significado (mā mamá ≠ mǎ caballo).", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("3. Desmonta los caracteres — cada hànzì tiene radicales con sentido y lógica.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("4. Habla desde el día 1 — con shadowing imitas el modelo y afianzas el acento.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Grid Cards
        items(cards) { card ->
            val recordText = when (card.id) {
                "memorama" -> store.memBestMap[state.memPairs] ?: "—"
                "quiz" -> "${store.gameStatsMap["quiz"]?.best ?: 0} pts"
                "escucha" -> "${store.gameStatsMap["escucha"]?.best ?: 0} pts"
                "pinyin" -> "${store.gameStatsMap["pinyin"]?.best ?: 0} pts"
                "tonos" -> "${store.gameStatsMap["tonos"]?.best ?: 0} pts"
                "pronuncia" -> "${store.gameStatsMap["pronuncia"]?.best ?: 0} pts"
                "escribe" -> "${store.gameStatsMap["escribe"]?.best ?: 0} pts"
                "tienda" -> "${state.coins} monedas"
                "flash" -> "${store.flashVistas} vistas"
                "cultura" -> "${store.poemasLeidos.size}/${VocabData.POEMAS.size} poemas"
                "cuaderno" -> "${store.wordProgressMap.values.count { it.box >= 4 }}/${VocabData.VOCAB.size} dominadas"
                "ruta" -> {
                    val doneUnits = store.caminoMap.values.count { it.doneLevel >= 1 }
                    "Unidad ${doneUnits + 1}/${VocabData.UNIDADES.size}"
                }
                else -> ""
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigate(card.id) }
                    .testTag("card_${card.id}")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = card.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.desc,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = recordText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ir",
                            tint = card.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Confetti Effect
    ConfettiOverlay(trigger = confettiTrigger)

    // Special Daily Streak Celebration Dialog
    streakCelebration?.let { celebrationData ->
        StreakCelebrationDialog(
            data = celebrationData,
            confettiTrigger = confettiTrigger,
            onClaim = { store.claimStreakBonus() },
            onDismiss = { store.dismissStreakCelebration() }
        )
    }
}
}
