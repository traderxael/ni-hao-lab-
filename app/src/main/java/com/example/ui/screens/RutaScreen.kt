package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.UnitInfo
import com.example.data.model.Word
import com.example.ui.components.AppIcons
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun RutaScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val appState by store.state.collectAsState()
    val selectedCat = appState.filtroCat
    val currentCatObj = VocabData.CATEGORIAS_TEMATICAS.find { it.id == selectedCat }

    val displayedUnits = if (selectedCat == "todas") {
        VocabData.UNIDADES
    } else {
        val filtered = VocabData.UNIDADES.filter { u ->
            u.id == selectedCat || u.cats.contains(selectedCat)
        }
        if (filtered.isNotEmpty()) filtered else VocabData.UNIDADES
    }

    var activeUnit by remember { mutableStateOf<UnitInfo?>(null) }
    var activeLevel by remember { mutableStateOf(1) } // 1, 2, or 3
    var isInLesson by remember { mutableStateOf(false) }

    // Lesson State
    var hearts by remember { mutableStateOf(3) }
    var questionIndex by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(5) }
    var lessonWords by remember { mutableStateOf(listOf<Word>()) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var currentType by remember { mutableStateOf(0) } // 0: hz->es, 1: es->hz, 2: audio->es
    var options by remember { mutableStateOf(listOf<Word>()) }
    var selectedOption by remember { mutableStateOf<Word?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isLessonWon by remember { mutableStateOf(false) }

    fun loadLessonQuestion(idx: Int) {
        if (idx >= totalQuestions) {
            isLessonWon = true
            val starsEarned = when (hearts) {
                3 -> 3
                2 -> 2
                else -> 1
            }
            activeUnit?.let { u ->
                store.recordUnitLevelCompletion(u.id, activeLevel, starsEarned)
            }
            store.addXP(15 + activeLevel * 5)
            store.addCoins(3)
            return
        }

        questionIndex = idx
        val target = lessonWords[idx]
        currentWord = target
        currentType = (0..2).random()
        val allUnitWords = activeUnit?.let { VocabData.unitWords(it.id) } ?: VocabData.VOCAB
        val distractors = (allUnitWords + VocabData.VOCAB).filter { it.hanzi != target.hanzi }.shuffled().take(3)
        options = (distractors + target).shuffled()
        selectedOption = null
        isAnswered = false

        if (currentType == 2 || currentType == 0) {
            ttsManager.speak(target.hanzi)
        }
    }

    fun startLesson(unit: UnitInfo, level: Int) {
        activeUnit = unit
        activeLevel = level
        val words = VocabData.unitWords(unit.id)
        if (words.isEmpty()) return
        val count = VocabData.NIVEL_LEN.getOrElse(level - 1) { 5 }
        totalQuestions = count
        lessonWords = (1..count).map { words.random() }
        questionIndex = 0
        hearts = 3 + store.state.value.corazonesExtra
        isGameOver = false
        isLessonWon = false
        isInLesson = true
        loadLessonQuestion(0)
    }

    if (isInLesson && currentWord != null) {
        // Active Lesson Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .testTag("active_lesson_screen")
        ) {
            // Header with Exit, Progress bar, and Hearts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { isInLesson = false },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Salir", modifier = Modifier.size(18.dp))
                }

                LinearProgressIndicator(
                    progress = { (questionIndex.toFloat() / totalQuestions).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(CircleShape),
                    color = SuccessGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "❤️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hearts.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        text = when (currentType) {
                            0 -> "¿Qué significa este carácter?"
                            1 -> "¿Cómo se dice en chino?"
                            else -> "Escucha y elige el significado"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentType == 0) {
                        Text(
                            text = currentWord!!.hanzi,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelloRed
                        )
                        Text(
                            text = currentWord!!.pinyin,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (currentType == 1) {
                        Text(
                            text = currentWord!!.es,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Button(
                            onClick = { ttsManager.speak(currentWord!!.hanzi) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = SelloRed),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Icon(AppIcons.volumeUp, contentDescription = "Audio", modifier = Modifier.size(32.dp))
                        }
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
                        !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        isCorrect -> SuccessGreen
                        isSelected -> ErrorRed
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
                                val ok = (opt == currentWord)
                                store.trackWord(currentWord!!.hanzi, ok)
                                ttsManager.speak(currentWord!!.hanzi)

                                if (!ok) {
                                    hearts--
                                    if (hearts <= 0) {
                                        isGameOver = true
                                    }
                                }
                            }
                            .testTag("lesson_option_${opt.hanzi}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentType == 1) "${opt.hanzi} (${opt.pinyin})" else opt.es,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isAnswered) {
                                if (isCorrect) Text("✅")
                                else if (isSelected) Text("❌")
                            }
                        }
                    }
                }
            }

            // Next Question Button
            AnimatedVisibility(visible = isAnswered && !isGameOver && !isLessonWon) {
                Button(
                    onClick = { loadLessonQuestion(questionIndex + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("lesson_next_button")
                ) {
                    Text("Continuar →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Game Over Dialog
            if (isGameOver) {
                AlertDialog(
                    onDismissRequest = { isInLesson = false },
                    title = { Text("💔 Te has quedado sin vidas") },
                    text = { Text("No te preocupes. Descansa un momento o consigue vidas en la tienda.") },
                    confirmButton = {
                        Button(
                            onClick = { startLesson(activeUnit!!, activeLevel) },
                            colors = ButtonDefaults.buttonColors(containerColor = SelloRed)
                        ) {
                            Text("Reintentar lección")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isInLesson = false }) {
                            Text("Volver a la ruta")
                        }
                    }
                )
            }

            // Lesson Won Dialog
            if (isLessonWon) {
                AlertDialog(
                    onDismissRequest = { isInLesson = false },
                    title = { Text("🎉 ¡Lección completada!", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { i ->
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i < hearts) GoldYellow else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Has dominado el Nivel $activeLevel de ${activeUnit?.nombre}.\n+${15 + activeLevel * 5} XP · +3 monedas 🪙")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { isInLesson = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Continuar")
                        }
                    }
                )
            }
        }
    } else {
        // Duolingo-style Path list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .testTag("ruta_screen")
        ) {
            // Header
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
                    text = "🛤️ Ruta de Aprendizaje",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val totalDone = store.caminoMap.values.sumOf { it.doneLevel }
                    Text(
                        text = "$totalDone/${VocabData.UNIDADES.size * 3} niveles",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thematic Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VocabData.CATEGORIAS_TEMATICAS.forEach { cat ->
                    val isSelected = selectedCat == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { store.setFiltros(appState.filtroHsk, cat.id) },
                        label = {
                            Text(
                                text = "${cat.emoji} ${cat.name}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("ruta_chip_cat_${cat.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Units List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayedUnits) { uIdx, unit ->
                    val prog = store.caminoMap[unit.id]
                    val doneLevels = prog?.doneLevel ?: 0
                    val isUnlocked = (selectedCat != "todas") || uIdx == 0 || (store.caminoMap[VocabData.UNIDADES.getOrNull(uIdx - 1)?.id]?.doneLevel ?: 0) >= 1
                    val wordsInUnit = VocabData.unitWords(unit.id).size

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = if (isUnlocked) 2.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = unit.emoji, fontSize = 32.sp)
                                    Column {
                                        Text(
                                            text = unit.nombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${unit.desc} · $wordsInUnit palabras",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (!isUnlocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (isUnlocked) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    (1..3).forEach { lvl ->
                                        val isLvlDone = doneLevels >= lvl
                                        val isLvlActive = doneLevels == lvl - 1
                                        val stars = prog?.stars?.get(lvl) ?: 0

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = when {
                                                isLvlDone -> SuccessGreen.copy(alpha = 0.15f)
                                                isLvlActive -> SelloRed
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable(enabled = isLvlDone || isLvlActive) {
                                                    startLesson(unit, lvl)
                                                }
                                                .testTag("unit_${unit.id}_lvl_$lvl")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Nivel $lvl",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isLvlActive) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (isLvlDone) {
                                                    Row {
                                                        repeat(stars) {
                                                            Text("⭐", fontSize = 10.sp)
                                                        }
                                                    }
                                                } else if (isLvlActive) {
                                                    Text("▶ Jugar", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
