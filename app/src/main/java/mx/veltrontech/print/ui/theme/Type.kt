package mx.veltrontech.print.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mx.veltrontech.print.R

// Montserrat Variable (wght 100–900) — un solo archivo cubre todos los pesos.
// Android usa el eje wght automáticamente al declarar múltiples entradas con el mismo archivo.
private val Montserrat = FontFamily(
    Font(R.font.montserrat_variable, weight = FontWeight.Normal),
    Font(R.font.montserrat_variable, weight = FontWeight.Medium),
    Font(R.font.montserrat_variable, weight = FontWeight.ExtraBold),
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp, lineHeight = 64.sp),
    titleLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    labelLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelSmall = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)