package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.VocabData
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import kotlinx.coroutines.delay

val TITULOS_NIVEL = listOf(
    "🐣 Pollito",
    "🌱 Brote",
    "🐼 Panda junior",
    "🏮 Farolillo",
    "🐲 Dragón",
    "👑 Maestro",
    "🌟 Leyenda"
)

fun getTituloNivel(nivel: Int): String {
    val idx = ((nivel - 1) / 2).coerceIn(0, TITULOS_NIVEL.size - 1)
    return TITULOS_NIVEL[idx]
}

@Composable
fun AppHeader(
    state: AppState,
    onToggleSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenTienda: () -> Unit,
    onOpenStreak: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nivel = (state.xp / 100) + 1
    val xpInLevel = state.xp % 100
    val xpNeeded = 100 - xpInLevel
    val titulo = getTituloNivel(nivel)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Row: Brand & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SelloRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "中",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "NiHao Lab",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aprende chino · HSK1+2",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        painter = if (state.sonido) AppIcons.volumeUp else AppIcons.volumeMute,
                        contentDescription = "Toggle sound",
                        tint = if (state.sonido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        painter = if (state.isDarkTheme) AppIcons.lightMode else AppIcons.darkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats Bar: Level, XP progress, Streak, Coins
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Level badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(width = 38.dp, height = 38.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = nivel.toString(),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = "LVL",
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.sp,
                        lineHeight = 8.sp
                    )
                }
            }

            // XP Block
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${state.xp} XP · $titulo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$xpNeeded XP al lvl ${nivel + 1}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { (xpInLevel / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Streak badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onOpenStreak() }
                    .testTag("streak_header_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = "🔥", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = state.racha.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Coins badge (clickable to shop)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onOpenTienda() }
                    .testTag("coins_header_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = "🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = state.coins.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedHsk: Int,
    selectedCat: String,
    totalWordsCount: Int,
    dueCount: Int,
    onHskChange: (Int) -> Unit,
    onCatChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hskOptions = listOf("HSK1+2" to 0, "HSK1" to 1, "HSK2" to 2)
    val currentCategory = VocabData.CATEGORIAS_TEMATICAS.find { it.id == selectedCat }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HSK filters
            hskOptions.forEach { (label, value) ->
                FilterChip(
                    selected = selectedHsk == value,
                    onClick = { onHskChange(value) },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Count badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (selectedCat == "todas") "$totalWordsCount palabras" else "$totalWordsCount en ${currentCategory?.name ?: selectedCat}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (dueCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldYellow.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🎯 $dueCount para repasar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Expanded thematic category scroll row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VocabData.CATEGORIAS_TEMATICAS.forEach { cat ->
                val isSelected = selectedCat == cat.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onCatChange(cat.id) },
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
                    modifier = Modifier.testTag("filter_chip_cat_${cat.id}")
                )
            }
        }
    }
}

@Composable
fun ToastNotification(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it }
    ) {
        if (message != null) {
            LaunchedEffect(message) {
                delay(2000)
                onDismiss()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
