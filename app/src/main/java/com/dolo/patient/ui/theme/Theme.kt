package com.dolo.patient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DoloBlue = Color(0xFF0D2344)
val DoloNavy = Color(0xFF0D2344)
val DoloTeal = Color(0xFF08A89D)
val DoloMint = Color(0xFF25C5B8)
val DoloBackground = Color(0xFFF8FBFD)
val DoloSurfaceAlt = Color(0xFFEAF9F8)
val DoloMuted = Color(0xFF677086)
val DoloBorder = Color(0xFFE5EBF2)

private val colors = lightColorScheme(primary=DoloTeal,secondary=DoloMint,tertiary=Color(0xFF1769D2),background=DoloBackground,surface=Color.White,surfaceVariant=DoloSurfaceAlt,onPrimary=Color.White,onBackground=DoloNavy,onSurface=DoloNavy,outline=DoloBorder,error=Color(0xFFD63C52))
private val type = Typography(
 headlineLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.ExtraBold,fontSize=32.sp,color=DoloNavy),
 headlineMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=26.sp,color=DoloNavy),
 titleLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=22.sp,color=DoloNavy),
 titleMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.SemiBold,fontSize=17.sp,color=DoloNavy),
 bodyLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=16.sp,color=DoloNavy),
 bodyMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=14.sp,color=DoloMuted),
 labelLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=16.sp)
)
@Composable fun DoloTheme(content:@Composable ()->Unit){MaterialTheme(colorScheme=colors,typography=type,content=content)}
