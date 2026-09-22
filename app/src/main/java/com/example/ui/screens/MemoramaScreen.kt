package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

data class MemoryCard(
    val id: Int,
    val pairId: String,
    val text: String,
    val subText: String? = null,
    val isChinese: Boolean
)

@Composable
fun MemoramaScreen(
    store: StoreRepository,
    ttsManager: TtsManager,
    onBack: () -> Unit
) {
    var pairCount by remember { mutableStateOf(6) } // 6 or 8
    var cards by remember { mutableStateOf(listOf<MemoryCard>()) }
    var flippedIndices by remember { mutableStateOf(setOf<Int>()) }
    var matchedPairIds by remember { mutableStateOf(setOf<String>()) }
    var attempts by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isGameWon by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

    fun setupGame(nPairs: Int) {
        pairCount = nPairs
        val words = store.getFilteredPool().shuffled().take(nPairs)
        val cardList = mutableListOf<MemoryCard>()
        var idCounter = 0
        words.forEach { w ->
            cardList.add(MemoryCard(idCounter++, w.hanzi, w.hanzi, w.pinyin, isChinese = true))
            cardList.add(MemoryCard(idCounter++, w.hanzi, w.es, null, isChinese = false))
        }
        cards = cardList.shuffled()
        flippedIndices = emptySet()
        matchedPairIds = emptySet()
        attempts = 0
        seconds = 0
        isGameWon = false
        isBusy = false
        isTimerRunning = true
    }

    LaunchedEffect(pairCount) {
        setupGame(pairCount)
    }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            seconds++
        }
    }

    fun onCardClick(index: Int) {
        if (isBusy || flippedIndices.contains(index) || matchedPairIds.contains(cards[index].pairId)) return

        val newFlipped = flippedIndices + index
        flippedIndices = newFlipped

        if (cards[index].isChinese) {
            ttsManager.speak(cards[index].pairId)
        }

        if (newFlipped.size == 2) {
            attempts++
            val firstCard = cards[newFlipped.first()]
            val secondCard = cards[newFlipped.last()]

            if (firstCard.pairId == secondCard.pairId) {
                // Match!
                matchedPairIds = matchedPairIds + firstCard.pairId
                flippedIndices = emptySet()
                store.trackWord(firstCard.pairId, true)

                if (matchedPairIds.size == pairCount) {
                    // Win!
                    isTimerRunning = false
                    isGameWon = true
                    store.recordMemoramaWin(pairCount, attempts, seconds)
                    store.addXP(20)
                    store.addCoins(3)
                }
            } else {
                // No match, delay and flip back
                isBusy = true
            }
        }
    }

    LaunchedEffect(isBusy) {
        if (isBusy) {
            delay(900)
            flippedIndices = emptySet()
            isBusy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("memorama_screen")
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = pairCount == 6,
                    onClick = { setupGame(6) },
                    label = { Text("6 parejas", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = pairCount == 8,
                    onClick = { setupGame(8) },
                    label = { Text("8 parejas", fontSize = 11.sp) }
                )
                IconButton(
                    onClick = { setupGame(pairCount) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "⏱️ ${seconds}s · $attempts intentos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "🎯 ${matchedPairIds.size}/$pairCount parejas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(cards) { idx, card ->
                val isFlipped = flippedIndices.contains(idx)
                val isMatched = matchedPairIds.contains(card.pairId)
                val isRevealed = isFlipped || isMatched

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        isMatched -> SuccessGreen.copy(alpha = 0.15f)
                        isRevealed -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = when {
                            isMatched -> SuccessGreen
                            isRevealed -> SelloRed
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isRevealed && !isBusy) { onCardClick(idx) }
                        .testTag("mem_card_$idx")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        if (isRevealed) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = card.text,
                                    fontSize = if (card.isChinese) 22.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (card.isChinese) SelloRed else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (card.subText != null) {
                                    Text(
                                        text = card.subText,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "🎴",
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }

        // Victory Dialog
        if (isGameWon) {
            AlertDialog(
                onDismissRequest = { isGameWon = false },
                title = { Text("🎉 ¡Completado!", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "¡Has encontrado las $pairCount parejas en $attempts intentos y ${seconds}s!\n\n+20 XP · +3 monedas 🪙"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { setupGame(pairCount) },
                        colors = ButtonDefaults.buttonColors(containerColor = SelloRed)
                    ) {
                        Text("Jugar otra vez")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text("Volver al menú")
                    }
                }
            )
        }
    }
}
