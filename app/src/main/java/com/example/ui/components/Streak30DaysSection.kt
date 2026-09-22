package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayStreakInfo
import com.example.data.StoreRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen

@Composable
fun Streak30DaysSection(
    store: StoreRepository,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = true
) {
    val history = remember(store.state.value, store.streakDays.size) {
        store.getLast30DaysStreakHistory()
    }
    val activeDays = history.count { it.isActive }
    val consistencyPct = (activeDays * 100) / 30
    val totalXpIn30Days = history.sumOf { it.xpEarned }
    val currentStreak = store.state.value.racha.coerceAtLeast(1)

    var selectedDay by remember {
        mutableStateOf(history.lastOrNull { it.isToday } ?: history.lastOrNull())
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_30_days_section"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldYellow.copy(alpha = 0.3f), SelloRed.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔥", fontSize = 22.sp)
                    }

                    Column {
                        Text(
                            text = "Historial de Rachas (30 Días)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tu constancia y hábito diario",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldYellow.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, GoldYellow.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "$consistencyPct% constancia",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric KPI Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_metrics_summary"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Racha actual
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Racha",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentStreak d 🔥",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SelloRed
                        )
                    }
                }

                // Metric 2: Días activos
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Días activos",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$activeDays / 30",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen
                        )
                    }
                }

                // Metric 3: XP 30 días
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "XP 30 días",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+$totalXpIn30Days ⚡",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (activeDays / 30f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (consistencyPct >= 70) SuccessGreen else if (consistencyPct >= 40) GoldYellow else SelloRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Grid of 30 Days (6 columns x 5 rows)
            Text(
                text = "Registro día a día (toca un día para ver detalles):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6-column matrix for 30 days
            val columns = 6
            val rows = 5
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0 until columns) {
                            val index = r * columns + c
                            if (index < history.size) {
                                val day = history[index]
                                val isSelected = selectedDay?.dateStr == day.dateStr

                                DayCellItem(
                                    day = day,
                                    isSelected = isSelected,
                                    onClick = { selectedDay = day },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Selected Day Details Card
            selectedDay?.let { day ->
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (day.isActive) GoldYellow.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_day_detail")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (day.isActive) "🔥" else "❄️",
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = "${day.fullDayName}, ${day.dayOfMonth} (${day.dateStr})" + if (day.isToday) " · ¡HOY!" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (day.isToday) SelloRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (day.isActive) {
                                        "Racha activa · +${day.xpEarned} XP ganados"
                                    } else {
                                        "Sin práctica registrada ese día"
                                    },
                                    fontSize = 12.sp,
                                    color = if (day.isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (day.isActive) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Completado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Consistency tip banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentBlue.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Consejo de racha",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "La consistencia diaria fija el pinyin y los 4 tonos en la memoria a largo plazo. ¡Incluso 5 minutos diarios marcan la diferencia!",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCellItem(
    day: DayStreakInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBrush = Brush.verticalGradient(
        listOf(
            GoldYellow.copy(alpha = 0.95f),
            SelloRed.copy(alpha = 0.90f)
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_today")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_today_scale"
    )

    val cellModifier = modifier
        .aspectRatio(1f)
        .then(if (day.isToday) Modifier.scale(pulseScale) else Modifier)
        .clip(RoundedCornerShape(12.dp))
        .background(
            if (day.isActive) {
                activeBrush
            } else {
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }
        )
        .border(
            width = if (day.isToday) 2.dp else if (isSelected) 1.5.dp else 0.5.dp,
            color = when {
                day.isToday -> AccentBlue
                isSelected -> MaterialTheme.colorScheme.primary
                day.isActive -> GoldYellow.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            },
            shape = RoundedCornerShape(12.dp)
        )
        .clickable { onClick() }
        .testTag("streak_day_cell_${day.dateStr}")

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day of week letter
            Text(
                text = day.dayOfWeek,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (day.isActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Day of month number
            Text(
                text = "${day.dayOfMonth}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (day.isActive) Color.White else MaterialTheme.colorScheme.onSurface
            )

            // Mini flame or dot
            if (day.isActive) {
                Text(
                    text = "🔥",
                    fontSize = 8.sp,
                    lineHeight = 9.sp
                )
            } else if (day.isToday) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                )
            }
        }
    }
}
