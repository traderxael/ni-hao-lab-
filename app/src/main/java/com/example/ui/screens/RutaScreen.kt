package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.model.UnitInfo
import com.example.data.model.Word
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun RutaScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    val appState by store.state.collectAsState()
    val caminoRevision by store.caminoRevision.collectAsState()
    val selectedCat = appState.filtroCat
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val displayedUnits = remember(selectedCat) {
        if (selectedCat == "todas") {
            VocabData.UNIDADES
        } else {
            val filtered = VocabData.UNIDADES.filter { u ->
                u.id == selectedCat || u.cats.contains(selectedCat)
            }
            if (filtered.isNotEmpty()) filtered else VocabData.UNIDADES
        }
    }

    var activeUnit by remember { mutableStateOf<UnitInfo?>(null) }
    var activeLevel by remember { mutableStateOf(1) }
    var isInLesson by remember { mutableStateOf(false) }

    // Preview & Guidebook modal states
    var nodePreview by remember { mutableStateOf<SelectedNodePreview?>(null) }
    var guidebookUnit by remember { mutableStateOf<Pair<UnitInfo, Int>?>(null) }

    // Lesson State
    var hearts by remember { mutableStateOf(3) }
    var comboStreak by remember { mutableStateOf(0) }
    var questionIndex by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(5) }
    var lessonWords by remember { mutableStateOf(listOf<Word>()) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var currentType by remember { mutableStateOf(0) }
    var options by remember { mutableStateOf(listOf<Word>()) }
    var selectedOption by remember { mutableStateOf<Word?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isLessonWon by remember { mutableStateOf(false) }

    fun loadLessonQuestion(idx: Int) {
        if (idx >= totalQuestions) {
            isLessonWon = true
            val starsEarned = when {
                hearts >= 3 -> 3
                hearts == 2 -> 2
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
        // Level 1 uses types 0..2; Levels 2 & 3 also introduce Hanzi-only challenge (type 3)
        currentType = if (activeLevel >= 2) (0..3).random() else (0..2).random()
        val allUnitWords = activeUnit?.let { VocabData.unitWords(it.id) } ?: VocabData.VOCAB
        val distractors = (allUnitWords + VocabData.VOCAB)
            .filter { it.hanzi != target.hanzi }
            .distinctBy { it.hanzi }
            .shuffled()
            .take(3)
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
        // Prioritize variety across the unit's words before repeating
        val pool = mutableListOf<Word>()
        while (pool.size < count) {
            pool.addAll(words.shuffled())
        }
        lessonWords = pool.take(count)
        questionIndex = 0
        comboStreak = 0
        hearts = 3 + store.state.value.corazonesExtra
        isGameOver = false
        isLessonWon = false
        isInLesson = true
        loadLessonQuestion(0)
    }

    if (isInLesson && currentWord != null) {
        RutaLessonView(
            unit = activeUnit,
            level = activeLevel,
            questionIndex = questionIndex,
            totalQuestions = totalQuestions,
            hearts = hearts,
            comboStreak = comboStreak,
            currentWord = currentWord!!,
            currentType = currentType,
            options = options,
            selectedOption = selectedOption,
            isAnswered = isAnswered,
            isGameOver = isGameOver,
            isLessonWon = isLessonWon,
            ttsManager = ttsManager,
            onExitLesson = { isInLesson = false },
            onSelectOption = { opt ->
                selectedOption = opt
                isAnswered = true
                val ok = (opt == currentWord)
                store.trackWord(currentWord!!.hanzi, ok)
                ttsManager.speak(currentWord!!.hanzi)

                if (ok) {
                    comboStreak++
                } else {
                    comboStreak = 0
                    hearts--
                    if (hearts <= 0) {
                        isGameOver = true
                    }
                }
            },
            onNextQuestion = { loadLessonQuestion(questionIndex + 1) },
            onRetryLesson = { activeUnit?.let { startLesson(it, activeLevel) } }
        )
    } else {
        // Compute current active unit index for quick jump button
        val totalDoneLevels = remember(caminoRevision) {
            store.caminoMap.values.sumOf { it.doneLevel }
        }
        val maxTotalLevels = VocabData.UNIDADES.size * 3

        val activeDisplayedIndex = remember(displayedUnits, caminoRevision, selectedCat) {
            val idx = displayedUnits.indexOfFirst { unit ->
                val globalIdx = VocabData.UNIDADES.indexOfFirst { it.id == unit.id }.coerceAtLeast(0)
                val unlocked = (selectedCat != "todas") || globalIdx == 0 ||
                    (store.caminoMap[VocabData.UNIDADES.getOrNull(globalIdx - 1)?.id]?.doneLevel ?: 0) >= 1
                val done = store.caminoMap[unit.id]?.doneLevel ?: 0
                val chestReady = done >= 3 && !store.claimedUnitChests.contains(unit.id)
                unlocked && (done < 3 || chestReady)
            }
            if (idx >= 0) idx else 0
        }

        val showJumpToCurrent by remember(activeDisplayedIndex) {
            derivedStateOf {
                val visible = listState.layoutInfo.visibleItemsInfo.map { it.index }
                visible.isNotEmpty() && !visible.contains(activeDisplayedIndex)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("ruta_screen")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header & Path Progress Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🛤️ Ruta de Aprendizaje",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Camino paso a paso · HSK 1+2",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SuccessGreen.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "$totalDoneLevels/$maxTotalLevels",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (totalDoneLevels.toFloat() / maxTotalLevels.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape),
                            color = SuccessGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                    }
                }

                // Winding Duolingo-Style Path
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 14.dp,
                            bottom = 92.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(
                            items = displayedUnits,
                            key = { _, unit -> unit.id }
                        ) { displayIdx, unit ->
                            // Force dependency on caminoRevision so progress updates recompose immediately
                            val rev = caminoRevision
                            val globalIdx = VocabData.UNIDADES.indexOfFirst { it.id == unit.id }.coerceAtLeast(0)
                            val palette = paletteForUnit(globalIdx)
                            val prog = remember(rev, unit.id) { store.caminoMap[unit.id] }
                            val isUnlocked = (selectedCat != "todas") || globalIdx == 0 ||
                                (store.caminoMap[VocabData.UNIDADES.getOrNull(globalIdx - 1)?.id]?.doneLevel ?: 0) >= 1
                            val isChestClaimed = remember(rev, unit.id) { store.claimedUnitChests.contains(unit.id) }
                            val unitWordsList = remember(unit.id) { VocabData.unitWords(unit.id) }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (displayIdx > 0) {
                                    UnitConnectorBridge(
                                        isUnlocked = isUnlocked,
                                        accentColor = paletteForUnit((globalIdx - 1).coerceAtLeast(0)).primary
                                    )
                                }

                                DuolingoUnitBanner(
                                    unitIndex = globalIdx,
                                    unit = unit,
                                    palette = palette,
                                    progress = prog,
                                    isUnlocked = isUnlocked,
                                    wordsCount = unitWordsList.size,
                                    onOpenGuide = { guidebookUnit = unit to globalIdx }
                                )

                                DuolingoWindingUnitPath(
                                    unitIndex = globalIdx,
                                    unit = unit,
                                    palette = palette,
                                    progress = prog,
                                    isUnitUnlocked = isUnlocked,
                                    isChestClaimed = isChestClaimed,
                                    unitWords = unitWordsList,
                                    ttsManager = ttsManager,
                                    onSelectLevel = { lvl, isDone, stars, isLocked ->
                                        nodePreview = SelectedNodePreview(
                                            unit = unit,
                                            unitIndex = globalIdx,
                                            level = lvl,
                                            isDone = isDone,
                                            stars = stars,
                                            isLocked = isLocked
                                        )
                                    },
                                    onLockedTap = {
                                        store.toast("🔒 Completa el nivel anterior para desbloquear")
                                    },
                                    onClaimChest = {
                                        store.claimUnitChest(unit.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Quick-Jump Pill to Current Active Unit
            AnimatedVisibility(
                visible = showJumpToCurrent,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(activeDisplayedIndex)
                        }
                    },
                    containerColor = SelloRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("ruta_jump_current_fab")
                ) {
                    Text(
                        text = "🎯 Ir al actual",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Node Start Preview Sheet (Duolingo-style lesson launch popover with unit HSK words)
            nodePreview?.let { preview ->
                NodeStartPreviewSheet(
                    preview = preview,
                    words = remember(preview.unit.id) { VocabData.unitWords(preview.unit.id) },
                    ttsManager = ttsManager,
                    onStartLesson = {
                        nodePreview = null
                        startLesson(preview.unit, preview.level)
                    },
                    onDismiss = { nodePreview = null }
                )
            }

            // Unit Vocabulary Guidebook Sheet
            guidebookUnit?.let { (unit, globalIdx) ->
                UnitGuidebookSheet(
                    unit = unit,
                    unitIndex = globalIdx,
                    words = VocabData.unitWords(unit.id),
                    ttsManager = ttsManager,
                    onDismiss = { guidebookUnit = null }
                )
            }
        }
    }
}
