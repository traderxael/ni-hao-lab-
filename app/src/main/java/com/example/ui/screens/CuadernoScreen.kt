package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.StoreRepository
import com.example.data.VocabData
import com.example.ui.components.AppIcons
import com.example.ui.components.PracticaRapidaModal
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun CuadernoScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("todas") } // todas, dominadas, debiles, nuevas
    var expandedWord by remember { mutableStateOf<String?>(null) }
    var showPracticaRapida by remember { mutableStateOf(false) }

    val debilWords = remember(store.wordProgressMap.size, store.wordProgressMap.values.map { it.box to it.fail }) {
        store.getDebilWords()
    }

    val allWords = VocabData.VOCAB

    val filteredList = remember(searchQuery, selectedFilter, store.wordProgressMap.size) {
        allWords.filter { w ->
            val matchesSearch = searchQuery.isBlank() ||
                    w.hanzi.contains(searchQuery, ignoreCase = true) ||
                    w.pinyin.contains(searchQuery, ignoreCase = true) ||
                    w.es.contains(searchQuery, ignoreCase = true)

            val status = store.wordStatus(w.hanzi)
            val matchesFilter = when (selectedFilter) {
                "dominadas" -> status == "dominada"
                "debiles" -> status == "debil"
                "nuevas" -> status == "nueva"
                "aprendiendo" -> status == "aprendiendo"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("cuaderno_screen")
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
                text = "📔 Mi Cuaderno",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${filteredList.size} palabras",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por hànzì, pīnyīn o español...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cuaderno_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        val filterOptions = listOf(
            "todas" to "Todas",
            "dominadas" to "Dominadas (Cajas 4-5)",
            "aprendiendo" to "Aprendiendo",
            "debiles" to "A repasar / Débiles",
            "nuevas" to "Nuevas"
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filterOptions) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 11.sp) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Banner de Práctica Rápida
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, if (debilWords.isNotEmpty()) GoldYellow else MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPracticaRapida = true }
                .testTag("practica_rapida_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldYellow.copy(alpha = 0.35f), SelloRed.copy(alpha = 0.25f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 22.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Práctica Rápida",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
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
                            text = if (debilWords.isNotEmpty()) {
                                "${debilWords.size} palabras débiles a reforzar"
                            } else {
                                "Mini-cuestionario de tus palabras débiles"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showPracticaRapida = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SelloRed),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Iniciar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Words List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList) { word ->
                val progress = store.cajaEntry(word.hanzi)
                val status = store.wordStatus(word.hanzi)
                val isExpanded = expandedWord == word.hanzi

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expandedWord = if (isExpanded) null else word.hanzi }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = word.hanzi,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelloRed
                                )
                                Column {
                                    Text(
                                        text = word.pinyin,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentBlue
                                    )
                                    Text(
                                        text = word.es,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (status) {
                                        "dominada" -> SuccessGreen.copy(alpha = 0.15f)
                                        "debil" -> Color(0xFFE5484D).copy(alpha = 0.15f)
                                        "aprendiendo" -> GoldYellow.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = if (status == "debil") "⚠️ Débil" else "Caja ${progress.box}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (status) {
                                            "dominada" -> SuccessGreen
                                            "debil" -> Color(0xFFE5484D)
                                            "aprendiendo" -> GoldYellow
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { ttsManager.speak(word.hanzi) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(AppIcons.volumeUp, contentDescription = "Audio", tint = AccentBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Expanded Info
                        if (isExpanded) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (word.rad != null) {
                                    Text("Radical: ${word.rad} — ${word.mnemo ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (word.fr != null) {
                                    Text("Ejemplo: ${word.fr.hz} (${word.fr.py})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(word.fr.es, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Aciertos: ${progress.ok} · Fallos: ${progress.fail}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    OutlinedButton(
                                        onClick = { store.toggleMarcarDebil(word.hanzi) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (status == "debil") Color(0xFFE5484D) else AccentBlue
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("toggle_debil_${word.hanzi}")
                                    ) {
                                        Text(
                                            text = if (status == "debil") "Quitar de débiles" else "Marcar débil ⚠️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPracticaRapida) {
        PracticaRapidaModal(
            store = store,
            ttsManager = ttsManager,
            onDismiss = { showPracticaRapida = false }
        )
    }
}
