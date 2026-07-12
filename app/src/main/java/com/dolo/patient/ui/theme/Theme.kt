package com.dolo.patient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DoloBlue = Color(0xFF356AE6)
val DoloNavy = Color(0xFF172B4D)
val DoloMint = Color(0xFF4CC9A4)
val DoloBackground = Color(0xFFF7FAFF)
val DoloSurfaceAlt = Color(0xFFEAF1FF)

private val DoloColors = lightColorScheme(
    primary = DoloBlue,
    secondary = DoloMint,
    background = DoloBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = DoloNavy,
    onSurface = DoloNavy,
)

@Composable
fun DoloTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DoloColors, typography = MaterialTheme.typography, content = content)
}

