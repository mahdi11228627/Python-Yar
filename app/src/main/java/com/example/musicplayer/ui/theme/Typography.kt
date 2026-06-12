package com.example.musicplayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.musicplayer.R

// تعریف خانواده فونت وزیرمتن
// Note: You need to add the font files to res/font to use this.
// For now, we use FontFamily.Default to avoid build errors.
val Vazirmatn = FontFamily.Default
/*
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)
*/

// تنظیم تایپوگرافی اختصاصی برای موزیک‌پلیر
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Vazirmatn,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)
