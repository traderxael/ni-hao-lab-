package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.R

object AppIcons {
    val volumeUp: Painter @Composable get() = painterResource(R.drawable.ic_volume_up)
    val volumeMute: Painter @Composable get() = painterResource(R.drawable.ic_volume_mute)
    val lightMode: Painter @Composable get() = painterResource(R.drawable.ic_light_mode)
    val darkMode: Painter @Composable get() = painterResource(R.drawable.ic_dark_mode)
    val mic: Painter @Composable get() = painterResource(R.drawable.ic_mic)
    val micOff: Painter @Composable get() = painterResource(R.drawable.ic_mic_off)
    val lightbulb: Painter @Composable get() = painterResource(R.drawable.ic_lightbulb)
    val route: Painter @Composable get() = painterResource(R.drawable.ic_route)
    val book: Painter @Composable get() = painterResource(R.drawable.ic_book)
    val barChart: Painter @Composable get() = painterResource(R.drawable.ic_bar_chart)
}
