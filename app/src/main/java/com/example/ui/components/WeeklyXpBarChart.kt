package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppState
import com.example.data.DayStreakInfo
import com.example.data.StoreRepository
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun WeeklyXpBarChart(
    store: StoreRepository,
    state: AppState,
    modifier: Modifier = Modifier
) {
    val xpTotalSnapshot = store.xpByDay.values.sum()
    val weekDays = remember(state.xp, xpTotalSnapshot, state.racha) {
        store.getLast30DaysStreakHistory().takeLast(7)
    }

    val totalWeekXp = remember(weekDays) { weekDays.sumOf { it.xpEarned } }
    val avgWeekXp = remember(totalWeekXp) { totalWeekXp / 7 }
    val goalDaysCount = remember(weekDays, state.goal) { weekDays.count { it.xpEarned >= state.goal } }

    val rawMax = remember(weekDays, state.goal) {
        maxOf(state.goal, weekDays.maxOfOrNull { it.xpEarned } ?: 0, 50)
    }
    val chartMax = remember(rawMax) {
        val step = 25
        (ceil(rawMax / step.toDouble()).toInt() * step).coerceAtLeast(50)
    }

    var selectedDay by remember(weekDays) {
        mutableStateOf(weekDays.lastOrNull { it.isToday } ?: weekDays.lastOrNull())
    }

    val barAnimations = remember { List(7) { Animatable(0f) } }
    LaunchedEffect(weekDays, chartMax) {
        weekDays.forEachIndexed { idx, day ->
            launch {
                barAnimations[idx].snapTo(0f)
                delay(idx * 55L)
                val target = (day.xpEarned.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)
                barAnimations[idx].animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_xp_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📊", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "XP de la última semana",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Progreso diario · Meta ${state.goal} XP",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "+$totalWeekXp XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Summary Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMiniStat(
                    label = "Promedio",
                    value = "$avgWeekXp XP/día",
                    accent = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryMiniStat(
                    label = "Metas logradas",
                    value = "$goalDaysCount / 7 días",
                    accent = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Area with Y-axis, Goal Reference Line, and 7 Interactive Bars
            val chartHeight = 152.dp
            val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            val goalRatio = (state.goal.toFloat() / chartMax.toFloat()).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis labels
                Column(
                    modifier = Modifier
                        .height(chartHeight)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$chartMax",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${chartMax / 2}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "0",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chart Canvas + Bars Overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight)
                ) {
                    // Horizontal grid lines + Goal dashed line
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val h = size.height
                        val w = size.width

                        listOf(0f, 0.5f, 1f).forEach { frac ->
                            val y = h * frac
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Daily Goal Dashed Threshold Line
                        val goalY = h * (1f - goalRatio)
                        drawLine(
                            color = GoldYellow.copy(alpha = 0.85f),
                            start = Offset(0f, goalY),
                            end = Offset(w, goalY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // 7 Animated Bars
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekDays.forEachIndexed { idx, day ->
                            val animatedFraction = barAnimations[idx].value
                            val isSelected = selectedDay?.dateStr == day.dateStr
                            val metGoal = day.xpEarned >= state.goal

                            val barBrush = when {
                                day.xpEarned == 0 -> Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                                metGoal -> Brush.verticalGradient(
                                    listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.72f))
                                )
                                day.isToday -> Brush.verticalGradient(
                                    listOf(SelloRed, SelloRed.copy(alpha = 0.75f))
                                )
                                else -> Brush.verticalGradient(
                                    listOf(AccentBlue, AccentBlue.copy(alpha = 0.72f))
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedDay = day }
                                    .testTag("weekly_xp_bar_${day.dateStr}"),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Value label above bar if selected or > 0
                                    if (day.xpEarned > 0) {
                                        Text(
                                            text = "${day.xpEarned}",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = when {
                                                metGoal -> SuccessGreen
                                                day.isToday -> SelloRed
                                                else -> AccentBlue
                                            },
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    // Actual bar
                                    val barHeightFrac = if (day.xpEarned > 0) {
                                        animatedFraction.coerceAtLeast(0.06f)
                                    } else {
                                        0.04f
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (isSelected) 0.78f else 0.65f)
                                            .fillMaxHeight(barHeightFrac)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(barBrush)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis Day Labels aligned with bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekDays.forEach { day ->
                    val isSelected = selectedDay?.dateStr == day.dateStr
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedDay = day }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (day.isToday) "Hoy" else day.dayOfWeek,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                day.isToday -> SelloRed
                                isSelected -> AccentBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (day.isToday) SelloRed else AccentBlue)
                            )
                        }
                    }
                }
            }

            // Selected Day Tooltip / Detail Banner
            selectedDay?.let { day ->
                Spacer(modifier = Modifier.height(12.dp))
                DayXpDetailPill(day = day, goal = state.goal)
            }
        }
    }
}

@Composable
private fun SummaryMiniStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun DayXpDetailPill(
    day: DayStreakInfo,
    goal: Int
) {
    val metGoal = day.xpEarned >= goal
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (metGoal) SuccessGreen.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_xp_selected_detail")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${day.fullDayName} ${day.dayOfMonth}" + if (day.isToday) " · Hoy" else "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when {
                        metGoal -> "🎯 ¡Meta diaria cumplida!"
                        day.xpEarned > 0 -> "Faltaron ${(goal - day.xpEarned).coerceAtLeast(1)} XP para la meta"
                        else -> "Sin XP registrado este día"
                    },
                    fontSize = 11.sp,
                    color = if (metGoal) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (metGoal) SuccessGreen.copy(alpha = 0.16f) else AccentBlue.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "${day.xpEarned} XP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (metGoal) SuccessGreen else AccentBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
