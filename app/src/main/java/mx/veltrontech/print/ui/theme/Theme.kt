package mx.veltrontech.print.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Sin color dinámico: la identidad Veltron es fija
private val LightColorScheme = lightColorScheme(
    primary = AzulVeltron,
    onPrimary = Blanco,
    primaryContainer = AzulClaro,
    onPrimaryContainer = Blanco,
    secondary = AzulClaro,
    onSecondary = Blanco,
    tertiary = DoradoEnergia,
    onTertiary = AzulMarino,
    background = Blanco,
    onBackground = AzulMarino,
    surface = Blanco,
    onSurface = AzulMarino,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8EFF7),
    onSurfaceVariant = AzulMarino,
    error = androidx.compose.ui.graphics.Color(0xFFB00020),
    onError = Blanco,
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulVeltron,
    onPrimary = Blanco,
    primaryContainer = AzulProfundo,
    onPrimaryContainer = Blanco,
    secondary = AzulClaro,
    onSecondary = Blanco,
    tertiary = DoradoEnergia,
    onTertiary = AzulMarino,
    background = AzulMarino,
    onBackground = Blanco,
    surface = AzulProfundo,
    onSurface = Blanco,
    surfaceVariant = AzulProfundo,
    onSurfaceVariant = Blanco,
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onError = AzulMarino,
)

@Composable
fun VeltronPrintTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}