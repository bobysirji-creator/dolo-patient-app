package com.dolo.patient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

val DoloBlue = Color(0xFF153B5B)
val DoloNavy = Color(0xFF102A43)
val DoloTeal = Color(0xFF007F79)
val DoloMint = Color(0xFF16A39A)
val DoloBackground = Color(0xFFF4F8F8)
val DoloSurfaceAlt = Color(0xFFE8F5F3)
val DoloMuted = Color(0xFF5D6F7E)
val DoloBorder = Color(0xFFD9E5E7)
val DoloCoral = Color(0xFFE65D75)
val DoloSuccess = Color(0xFF18845B)
val DoloWarning = Color(0xFFF2A23A)
val DoloInfo = Color(0xFF2C6CB0)

private val colors = lightColorScheme(
    primary = DoloTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F2EE),
    onPrimaryContainer = Color(0xFF003D3A),
    secondary = DoloBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEAF4),
    onSecondaryContainer = DoloNavy,
    tertiary = DoloCoral,
    background = DoloBackground,
    onBackground = DoloNavy,
    surface = Color.White,
    onSurface = DoloNavy,
    surfaceVariant = DoloSurfaceAlt,
    onSurfaceVariant = DoloMuted,
    outline = DoloBorder,
    outlineVariant = Color(0xFFEDF2F3),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6)
)

private val darkColors = darkColorScheme(
    primary = Color(0xFF63D7CE),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF07544F),
    onPrimaryContainer = Color(0xFFB7F5EF),
    secondary = Color(0xFFA9C9E2),
    onSecondary = Color(0xFF0D344F),
    secondaryContainer = Color(0xFF244A64),
    onSecondaryContainer = Color(0xFFD5E8F7),
    tertiary = Color(0xFFFFB1C0),
    background = Color(0xFF0C1619),
    onBackground = Color(0xFFE3ECEE),
    surface = Color(0xFF132226),
    onSurface = Color(0xFFE3ECEE),
    surfaceVariant = Color(0xFF1C3034),
    onSurfaceVariant = Color(0xFFB8C8CB),
    outline = Color(0xFF536B70),
    outlineVariant = Color(0xFF2B4146),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)
private val type = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 34.sp, letterSpacing = (-0.35).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun DoloTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkColors else colors, typography = type, shapes = shapes, content = content)
}
