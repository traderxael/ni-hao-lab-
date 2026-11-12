package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.VocabData
import com.example.data.model.UnitInfo
import com.example.data.model.UnitProgress
import com.example.data.model.Word
import com.example.ui.components.AppIcons
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlin.math.roundToInt

data class UnitPalette(
    val primary: Color,
    val lip: Color,
    val softTint: Color
)

private val UNIT_PALETTES = listOf(
    UnitPalette(SelloRed, Color(0xFFA11E25), Color(0xFFFFECEE)),
    UnitPalette(SuccessGreen, Color(0xFF1F7A4D), Color(0xFFE9F6EF)),
    UnitPalette(AccentBlue, Color(0xFF2156C4), Color(0xFFEAF1FF)),
    UnitPalette(Color(0xFF7C4DFF), Color(0xFF5328CC), Color(0xFFF1ECFF)),
    UnitPalette(Color(0xFFE67E22), Color(0xFFB35B0E), Color(0xFFFFF3E6)),
    UnitPalette(Color(0xFF009688), Color(0xFF00695C), Color(0xFFE6F5F3)),
    UnitPalette(Color(0xFFE91E63), Color(0xFFAD1457), Color(0xFFFCE9F0)),
    UnitPalette(Color(0xFF0EA5E9), Color(0xFF0369A1), Color(0xFFE6F6FD))
)

fun paletteForUnit(index: Int): UnitPalette {
    return UNIT_PALETTES[index % UNIT_PALETTES.size]
}

data class SelectedNodePreview(
    val unit: UnitInfo,
    val unitIndex: Int,
    val level: Int,
    val isDone: Boolean,
    val stars: Int,
    val isLocked: Boolean = false
)

@Composable
fun DuolingoUnitBanner(
    unitIndex: Int,
    unit: UnitInfo,
    palette: UnitPalette,
    progress: UnitProgress?,
    isUnlocked: Boolean,
    wordsCount: Int,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doneLevels = progress?.doneLevel ?: 0
    val totalStars = progress?.stars?.values?.sum() ?: 0
    val bannerColor = if (isUnlocked) palette.primary else MaterialTheme.colorScheme.surfaceVariant
    val lipColor = if (isUnlocked) palette.lip else MaterialTheme.colorScheme.outline
    val contentColor = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .testTag("unit_banner_${unit.id}")
    ) {
        // Bottom 3D lip
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 5.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(lipColor)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = bannerColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUnlocked) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = unit.emoji, fontSize = 26.sp)
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "UNIDAD ${unitIndex + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = contentColor.copy(alpha = 0.85f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (isUnlocked) Color.Black.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = if (isUnlocked) "$doneLevels/3 · ⭐$totalStars/9" else "🔒 Bloqueada",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = unit.nombre,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${unit.desc} · $wordsCount palabras",
                            fontSize = 11.sp,
                            color = contentColor.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Guidebook button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUnlocked) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenGuide() }
                        .testTag("unit_guide_btn_${unit.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = AppIcons.book,
                            contentDescription = "Guía de vocabulario",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Guía",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConnectorBridge(
    isUnlocked: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        } else {
            drawProgress.snapTo(0f)
        }
    }

    val outlineColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            drawLine(
                color = outlineColor.copy(alpha = 0.55f),
                start = Offset(cx, 0f),
                end = Offset(cx, size.height),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
            )

            val p = drawProgress.value
            if (p > 0.01f) {
                val endY = size.height * p
                drawLine(
                    color = accentColor.copy(alpha = 0.25f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, endY),
                    strokeWidth = 14.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(cx, 0f),
                    end = Offset(cx, endY),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round
                )
                if (p < 0.98f) {
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = Offset(cx, endY)
                    )
                }
            }
        }
    }
}

@Composable
fun DuolingoWindingUnitPath(
    unitIndex: Int,
    unit: UnitInfo,
    palette: UnitPalette,
    progress: UnitProgress?,
    isUnitUnlocked: Boolean,
    isChestClaimed: Boolean,
    unitWords: List<Word>,
    ttsManager: TtsManager,
    onSelectLevel: (level: Int, isDone: Boolean, stars: Int, isLocked: Boolean) -> Unit,
    onLockedTap: () -> Unit,
    onClaimChest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doneLevels = progress?.doneLevel ?: 0
    val sign = if (unitIndex % 2 == 0) 1f else -1f
    // 4 stepping stones per unit: Nivel 1, Nivel 2, Nivel 3, Cofre de Unidad
    val offsetFractions = listOf(0f, 0.85f * sign, -0.85f * sign, 0f)

    val nodeSize = 76.dp
    val rowHeight = 128.dp
    val topPadding = 30.dp
    val totalHeight = topPadding + rowHeight * 4 + 12.dp

    val outlineTrackColor = MaterialTheme.colorScheme.outline

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val centerX = widthPx / 2f
        val maxSwingDp = (maxWidth * 0.27f).coerceAtMost(96.dp)
        val maxSwingPx = with(density) { maxSwingDp.toPx() }
        val rowHeightPx = with(density) { rowHeight.toPx() }
        val topPadPx = with(density) { topPadding.toPx() }
        val nodeSizePx = with(density) { nodeSize.toPx() }

        val nodeCenters = remember(widthPx, unitIndex) {
            offsetFractions.mapIndexed { idx, frac ->
                Offset(
                    x = centerX + frac * maxSwingPx,
                    y = topPadPx + (idx + 0.5f) * rowHeightPx
                )
            }
        }

        // Progressive line-drawing animations for each of the 3 inter-node segments
        val segmentProgress = remember(unit.id) {
            List(3) { Animatable(0f) }
        }
        LaunchedEffect(unit.id, isUnitUnlocked, doneLevels) {
            for (i in 0 until 3) {
                val shouldDraw = isUnitUnlocked && doneLevels > i
                if (shouldDraw) {
                    if (segmentProgress[i].value < 1f) {
                        segmentProgress[i].animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 520,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                } else {
                    segmentProgress[i].snapTo(0f)
                }
            }
        }

        val pathFlowTransition = rememberInfiniteTransition(label = "path_flow")
        val flowPhase by pathFlowTransition.animateFloat(
            initialValue = 36f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "flow_phase"
        )

        val pathMeasure = remember { PathMeasure() }
        val partialPath = remember { Path() }

        // 1. Winding S-curve path connecting the 4 nodes with progressive drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until nodeCenters.size - 1) {
                val start = nodeCenters[i]
                val end = nodeCenters[i + 1]
                val midY = (start.y + end.y) / 2f
                val fullPath = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(start.x, midY, end.x, midY, end.x, end.y)
                }

                // Always draw the subtle dashed road underneath
                drawPath(
                    path = fullPath,
                    color = outlineTrackColor.copy(alpha = 0.55f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                    )
                )

                val progress = segmentProgress[i].value
                if (progress > 0.01f) {
                    pathMeasure.setPath(fullPath, false)
                    val totalLen = pathMeasure.length
                    if (totalLen > 0f) {
                        partialPath.reset()
                        val drawDist = (totalLen * progress).coerceIn(0f, totalLen)
                        pathMeasure.getSegment(0f, drawDist, partialPath, true)

                        // Soft outer glow
                        drawPath(
                            path = partialPath,
                            color = palette.primary.copy(alpha = 0.25f),
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Solid progressively-drawn line
                        drawPath(
                            path = partialPath,
                            color = palette.primary,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Subtle flowing highlight dashes showing forward momentum
                        drawPath(
                            path = partialPath,
                            color = Color.White.copy(alpha = 0.35f),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 24f), flowPhase)
                            )
                        )

                        // Bright leading spark at the tip while the line is actively drawing
                        if (progress < 0.98f) {
                            val tipPos = pathMeasure.getPosition(drawDist)
                            drawCircle(
                                color = palette.primary.copy(alpha = 0.45f),
                                radius = 10.dp.toPx(),
                                center = tipPos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = tipPos
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive character / mascot medallions nestled inside the open S-curves
        val wordA = unitWords.getOrNull(0)
        val wordB = unitWords.getOrNull(1)
        if (wordA != null) {
            val sideOffsetX = if (sign > 0) centerX - maxSwingPx * 0.92f else centerX + maxSwingPx * 0.92f
            val medY = nodeCenters[1].y
            PathCharacterMedallion(
                hanzi = wordA.hanzi,
                pinyin = wordA.pinyin,
                meaning = wordA.es,
                accent = palette.primary,
                onClick = { ttsManager.speak(wordA.hanzi) },
                modifier = Modifier.offset {
                    IntOffset(
                        x = (sideOffsetX - 42.dp.toPx()).roundToInt(),
                        y = (medY - 34.dp.toPx()).roundToInt()
                    )
                }
            )
        }
        if (wordB != null) {
            val sideOffsetX = if (sign > 0) centerX + maxSwingPx * 0.92f else centerX - maxSwingPx * 0.92f
            val medY = nodeCenters[2].y
            PathCharacterMedallion(
                hanzi = wordB.hanzi,
                pinyin = wordB.pinyin,
                meaning = wordB.es,
                accent = palette.primary,
                onClick = { ttsManager.speak(wordB.hanzi) },
                modifier = Modifier.offset {
                    IntOffset(
                        x = (sideOffsetX - 42.dp.toPx()).roundToInt(),
                        y = (medY - 34.dp.toPx()).roundToInt()
                    )
                }
            )
        }

        // 3. Render Level 1, 2, 3 Nodes
        (1..3).forEach { lvl ->
            val nodeIdx = lvl - 1
            val center = nodeCenters[nodeIdx]
            val isLvlDone = isUnitUnlocked && doneLevels >= lvl
            val isLvlActive = isUnitUnlocked && doneLevels == lvl - 1
            val stars = progress?.stars?.get(lvl) ?: 0
            val label = when (lvl) {
                1 -> "Nivel 1"
                2 -> "Nivel 2"
                else -> "Nivel 3"
            }

            Box(
                modifier = Modifier
                    .width(124.dp)
                    .offset {
                        IntOffset(
                            x = (center.x - 62.dp.toPx()).roundToInt(),
                            y = (center.y - (nodeSizePx / 2f) - 24.dp.toPx()).roundToInt()
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                DuolingoPathNode(
                    nodeSize = nodeSize,
                    palette = palette,
                    isDone = isLvlDone,
                    isActive = isLvlActive,
                    isLocked = !isLvlDone && !isLvlActive,
                    stars = stars,
                    label = label,
                    calloutText = if (lvl == 1) "EMPEZAR" else "CONTINUAR",
                    isChest = false,
                    isChestClaimed = false,
                    onClick = {
                        val locked = !isLvlDone && !isLvlActive
                        onSelectLevel(lvl, isLvlDone, stars, locked)
                    },
                    modifier = Modifier.testTag("unit_${unit.id}_lvl_$lvl")
                )
            }
        }

        // 4. Render Unit Chest / Checkpoint Node (4th node)
        val chestCenter = nodeCenters[3]
        val isChestReady = isUnitUnlocked && doneLevels >= 3 && !isChestClaimed
        Box(
            modifier = Modifier
                .width(124.dp)
                .offset {
                    IntOffset(
                        x = (chestCenter.x - 62.dp.toPx()).roundToInt(),
                        y = (chestCenter.y - (nodeSizePx / 2f) - 24.dp.toPx()).roundToInt()
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            DuolingoPathNode(
                nodeSize = nodeSize,
                palette = UnitPalette(GoldYellow, Color(0xFFC67E08), Color(0xFFFFF7E6)),
                isDone = isChestClaimed,
                isActive = isChestReady,
                isLocked = !isChestReady && !isChestClaimed,
                stars = if (isChestClaimed) 3 else 0,
                label = if (isChestClaimed) "Cofre abierto" else "Cofre +15🪙",
                calloutText = "¡ABRIR 🎁!",
                isChest = true,
                isChestClaimed = isChestClaimed,
                onClick = {
                    if (isChestReady) {
                        onClaimChest()
                    } else if (!isChestClaimed) {
                        onLockedTap()
                    }
                },
                modifier = Modifier.testTag("unit_${unit.id}_chest")
            )
        }
    }
}

@Composable
private fun PathCharacterMedallion(
    hanzi: String,
    pinyin: String,
    meaning: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .width(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hanzi,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = pinyin,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = meaning,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DuolingoPathNode(
    nodeSize: Dp,
    palette: UnitPalette,
    isDone: Boolean,
    isActive: Boolean,
    isLocked: Boolean,
    stars: Int,
    label: String,
    calloutText: String,
    isChest: Boolean,
    isChestClaimed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && !isLocked) 5.dp else 0.dp,
        label = "node_press"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "callout_bob"
    )
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_pulse"
    )

    val faceColor = when {
        isActive -> palette.primary
        isDone -> if (isChest) GoldYellow else palette.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val lipColor = when {
        isActive -> palette.lip
        isDone -> if (isChest) Color(0xFFC67E08) else palette.lip
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Floating Duolingo callout bubble above the active node
        Box(
            modifier = Modifier.height(26.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (isActive) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = bobOffset.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, palette.primary),
                        shadowElevation = 3.dp
                    ) {
                        Text(
                            text = calloutText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            color = palette.primary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    Canvas(modifier = Modifier.size(width = 10.dp, height = 5.dp)) {
                        val tri = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(tri, color = palette.primary)
                    }
                }
            }
        }

        // Tactile 3D Node Circle
        Box(
            modifier = Modifier.size(nodeSize + 14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated outer ring on active node
            if (isActive) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = (size.minDimension / 2f) * (0.86f + 0.14f * pulseFraction)
                    drawCircle(
                        color = palette.primary.copy(alpha = (1f - pulseFraction) * 0.45f),
                        radius = radius,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = palette.primary.copy(alpha = 0.85f),
                        startAngle = -90f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 3D Base shadow lip
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .offset(y = 6.dp)
                    .clip(CircleShape)
                    .background(lipColor)
            )

            // Pressable top circle
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .offset(y = pressOffset)
                    .clip(CircleShape)
                    .background(faceColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glossy top highlight arc
                if (!isLocked) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.25f),
                            startAngle = -140f,
                            sweepAngle = 80f,
                            useCenter = false,
                            topLeft = Offset(10.dp.toPx(), 8.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 20.dp.toPx(),
                                size.height - 20.dp.toPx()
                            ),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                when {
                    isChest && isChestClaimed -> {
                        Text(text = "👑", fontSize = 28.sp)
                    }
                    isChest -> {
                        Text(
                            text = if (isActive) "🎁" else "🧰",
                            fontSize = 28.sp
                        )
                    }
                    isDone -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "$label completado",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    isActive -> {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Empezar $label",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "$label bloqueado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label + Star Pill under the node
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            border = BorderStroke(
                1.dp,
                if (isActive) palette.primary.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (isDone && !isChest && stars > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(stars) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldYellow,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeStartPreviewSheet(
    preview: SelectedNodePreview,
    words: List<Word>,
    ttsManager: TtsManager,
    onStartLesson: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = paletteForUnit(preview.unitIndex)
    val qCount = VocabData.NIVEL_LEN.getOrElse(preview.level - 1) { 5 }
    val xpReward = 15 + preview.level * 5
    val levelTitle = when (preview.level) {
        1 -> "Exploración"
        2 -> "Práctica guiada"
        else -> "Dominio total"
    }
    val hsk1Count = remember(words) { words.count { it.hsk == 1 } }
    val hsk2Count = remember(words) { words.count { it.hsk == 2 } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = preview.unit.emoji, fontSize = 28.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nivel ${preview.level} · $levelTitle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${preview.unit.nombre} · $qCount preguntas",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (preview.isDone) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "⭐ ${preview.stars}/3",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "+$xpReward XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldYellow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+3 🪙",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                if (hsk1Count > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "HSK 1 · $hsk1Count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
                if (hsk2Count > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldYellow.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "HSK 2 · $hsk2Count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HSK Words Preview Section for this Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 Palabras HSK de esta unidad (${words.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Toca para oír",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .testTag("node_preview_hsk_words_list")
            ) {
                items(words, key = { it.hanzi }) { word ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { ttsManager.speak(word.hanzi) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = word.hanzi,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                                Column {
                                    Text(
                                        text = word.pinyin,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = word.es,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (word.hsk == 1) AccentBlue.copy(alpha = 0.14f) else GoldYellow.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = "HSK ${word.hsk}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (word.hsk == 1) AccentBlue else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Icon(
                                    painter = AppIcons.volumeUp,
                                    contentDescription = "Escuchar ${word.hanzi}",
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (preview.isLocked) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Completa el nivel anterior para desbloquear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Button(
                    onClick = onStartLesson,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_lesson_confirm_button")
                ) {
                    Text(
                        text = if (preview.isDone) "REPASAR (+$xpReward XP)" else "EMPEZAR (+$xpReward XP)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitGuidebookSheet(
    unit: UnitInfo,
    unitIndex: Int,
    words: List<Word>,
    ttsManager: TtsManager,
    onDismiss: () -> Unit
) {
    val palette = paletteForUnit(unitIndex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = unit.emoji, fontSize = 26.sp)
                    }
                }
                Column {
                    Text(
                        text = "Guía · ${unit.nombre}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toca cualquier palabra para escuchar su pronunciación",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 420.dp)
            ) {
                items(words, key = { it.hanzi }) { word ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { ttsManager.speak(word.hanzi) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                    Text(
                                        text = word.hanzi,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Column {
                                        Text(
                                            text = word.pinyin,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = word.es,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { ttsManager.speak(word.hanzi) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = AppIcons.volumeUp,
                                        contentDescription = "Escuchar ${word.hanzi}",
                                        tint = palette.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            word.fr?.let { ex ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💬 ${ex.hz} (${ex.py}) — ${ex.es}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
