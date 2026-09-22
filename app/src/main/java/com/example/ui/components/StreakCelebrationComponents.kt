package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.StoreRepository
import com.example.data.StreakCelebrationData
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SelloRed
import com.example.ui.theme.SuccessGreen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFFE53935), // Sello Red
    Color(0xFFFFB300), // Gold Yellow
    Color(0xFF1E88E5), // Vivid Blue
    Color(0xFF43A047), // Jade Green
    Color(0xFF8E24AA), // Imperial Purple
    Color(0xFFFF7043), // Tangerine
    Color(0xFFEC407A), // Peony Pink
    Color(0xFF00ACC1)  // Cyan
)

private data class ConfettiPiece(
    val xRatio: Float,
    val yOffsetPx: Float,
    val vx: Float,
    val vy: Float,
    val gravity: Float,
    val wobbleSpeed: Float,
    val wobblePhase: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val shapeType: Int // 0: rect, 1: circle, 2: star/sparkle, 3: ribbon
)

@Composable
fun ConfettiOverlay(
    trigger: Int,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    var activeTrigger by remember { mutableStateOf(0) }
    val progress = remember { Animatable(0f) }
    val particles = remember(trigger) {
        val rand = Random(trigger.hashCode() + System.currentTimeMillis().toInt())
        List(70) {
            ConfettiPiece(
                xRatio = rand.nextFloat(),
                yOffsetPx = -rand.nextFloat() * 120f,
                vx = (rand.nextFloat() - 0.5f) * 220f,
                vy = rand.nextFloat() * 180f + 120f,
                gravity = rand.nextFloat() * 260f + 320f,
                wobbleSpeed = rand.nextFloat() * 8f + 5f,
                wobblePhase = rand.nextFloat() * 6.28f,
                rotationSpeed = (rand.nextFloat() - 0.5f) * 540f,
                initialRotation = rand.nextFloat() * 360f,
                width = rand.nextFloat() * 14f + 12f,
                height = rand.nextFloat() * 18f + 10f,
                color = CONFETTI_COLORS[rand.nextInt(CONFETTI_COLORS.size)],
                shapeType = rand.nextInt(4)
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            activeTrigger = trigger
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
            )
            onFinished()
        }
    }

    if (trigger > 0 && progress.value in 0.001f..0.999f) {
        val currentProgress = progress.value
        val elapsedSec = currentProgress * 3.2f
        val alpha = if (currentProgress > 0.75f) {
            (1f - (currentProgress - 0.75f) / 0.25f).coerceIn(0f, 1f)
        } else {
            1f
        }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .testTag("confetti_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            particles.forEach { p ->
                val curX = p.xRatio * canvasW + p.vx * elapsedSec + sin(elapsedSec * p.wobbleSpeed + p.wobblePhase) * 28f
                val curY = p.yOffsetPx + p.vy * elapsedSec + 0.5f * p.gravity * elapsedSec * elapsedSec

                if (curY in -60f..(canvasH + 60f) && curX in -60f..(canvasW + 60f)) {
                    val wobbleX = cos(elapsedSec * p.wobbleSpeed + p.wobblePhase)
                    val rotation = p.initialRotation + p.rotationSpeed * elapsedSec
                    val drawColor = p.color.copy(alpha = alpha)

                    rotate(degrees = rotation, pivot = Offset(curX, curY)) {
                        scale(scaleX = wobbleX, scaleY = 1f, pivot = Offset(curX, curY)) {
                            when (p.shapeType) {
                                1 -> { // Circle
                                    drawCircle(
                                        color = drawColor,
                                        radius = p.width * 0.45f,
                                        center = Offset(curX, curY)
                                    )
                                }
                                2 -> { // Star / Diamond
                                    drawSparkle(
                                        center = Offset(curX, curY),
                                        radius = p.width * 0.6f,
                                        color = drawColor
                                    )
                                }
                                3 -> { // Ribbon
                                    drawRoundRect(
                                        color = drawColor,
                                        topLeft = Offset(curX - p.width * 0.3f, curY - p.height * 0.7f),
                                        size = Size(p.width * 0.6f, p.height * 1.4f)
                                    )
                                }
                                else -> { // Rectangle
                                    drawRect(
                                        color = drawColor,
                                        topLeft = Offset(curX - p.width * 0.5f, curY - p.height * 0.5f),
                                        size = Size(p.width, p.height)
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

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
fun AnimatedStreakFlame(
    streak: Int,
    modifier: Modifier = Modifier,
    flameSize: Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val emberRise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ember_rise"
    )

    Box(
        modifier = modifier.size(flameSize * 1.5f),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Background Halo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.minDimension * 0.48f * pulseScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldYellow.copy(alpha = 0.70f * glowAlpha),
                        SelloRed.copy(alpha = 0.35f * glowAlpha),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = centerOffset
            )

            // Little rising embers
            val emberCount = 6
            for (i in 0 until emberCount) {
                val seedOffset = (i * 0.17f + emberRise) % 1f
                val emberX = centerOffset.x + sin((i * 1.2f) + emberRise * 6.28f) * (glowRadius * 0.38f)
                val emberY = centerOffset.y + (glowRadius * 0.2f) - (seedOffset * glowRadius * 0.95f)
                val emberAlpha = (1f - seedOffset).coerceIn(0f, 1f) * glowAlpha

                drawCircle(
                    color = if (i % 2 == 0) GoldYellow.copy(alpha = emberAlpha) else SelloRed.copy(alpha = emberAlpha),
                    radius = 3.5f * (1f - seedOffset * 0.5f),
                    center = Offset(emberX, emberY)
                )
            }
        }

        // Central Animated Flame Emoji
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(flameSize),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔥",
                fontSize = (flameSize.value * 0.72f).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StreakCelebrationDialog(
    data: StreakCelebrationData,
    confettiTrigger: Int,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .testTag("streak_celebration_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Fullscreen Confetti falling during the celebration!
            ConfettiOverlay(trigger = confettiTrigger)

            // Celebration Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clickable(enabled = false) {}
                    .testTag("streak_celebration_card"),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldYellow.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button top right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("btn_close_streak_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Pulsing animated flame hero
                    AnimatedStreakFlame(
                        streak = data.streak,
                        flameSize = 90.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (data.isNewToday) "¡NUEVA RACHA ALCANZADA!" else "¡RACHA DIARIA ACTIVA!",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp,
                        color = SelloRed,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pill with streak count
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GoldYellow.copy(alpha = 0.20f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldYellow)
                    ) {
                        Text(
                            text = "🔥 ${data.streak} ${if (data.streak == 1) "DÍA SEGUIDO" else "DÍAS SEGUIDOS"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val motivationalQuote = when {
                        data.streak == 1 -> "¡Has encendido la chispa! Practicar chino cada día fijará los tonos y caracteres en tu memoria a largo plazo."
                        data.streak < 3 -> "¡Dos días consecutivos! Tu disciplina está comenzando a dar frutos. ¡Sigue con este impulso!"
                        data.streak < 7 -> "¡Constancia de hierro! Tu cerebro está reconociendo radicales y vocabulario con mayor naturalidad."
                        data.streak < 14 -> "¡Más de una semana completa! Eres imparable. El chino mandarín ya forma parte de tu rutina diaria."
                        data.streak < 30 -> "¡Impresionante maestría! Mantienes viva la llama del aprendizaje día tras día."
                        else -> "¡Leyenda viviente de NiHao Lab! Tu perseverancia no tiene límites. ¡Una racha legendaria!"
                    }

                    Text(
                        text = motivationalQuote,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Daily Bonus Reward Banner
                    if (data.isNewToday && (data.bonusCoins > 0 || data.bonusXp > 0)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AccentBlue.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🎁", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Bonificación de racha hoy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "+${data.bonusCoins} Monedas 🪙  ·  +${data.bonusXp} XP ⚡",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = AccentBlue
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary claim/continue button
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_claim_streak_bonus"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SelloRed)
                    ) {
                        Text(
                            text = if (data.isNewToday) "¡Reclamar y a por hoy! 🚀" else "¡Continuar aprendiendo! 🚀",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakHomeCard(
    streak: Int,
    onCelebrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCelebrate() }
            .testTag("streak_home_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldYellow.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                AnimatedStreakFlame(
                    streak = streak,
                    flameSize = 44.dp
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Racha Diaria: $streak ${if (streak == 1) "día" else "días"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "¡Llama activa! Toca para celebrar tu constancia.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FilledTonalButton(
                onClick = onCelebrate,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GoldYellow.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("btn_celebrar_racha")
            ) {
                Text(
                    text = "🎉 Celebrar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
