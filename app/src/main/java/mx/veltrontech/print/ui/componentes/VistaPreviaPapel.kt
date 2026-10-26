package mx.veltrontech.print.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.dp
import mx.veltrontech.print.imagen.ImagenMonocromo
import mx.veltrontech.print.imagen.ProcesadorImagen

/**
 * Rectángulo blanco que representa el papel de 576 puntos.
 * Siempre fondo blanco, incluso en modo oscuro (Color.White, no MaterialTheme).
 * FilterQuality.None: sin suavizado, para que los puntos se vean nítidos.
 */
@Composable
fun VistaPreviaPapel(imagen: ImagenMonocromo?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .padding(8.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (imagen != null) {
            val imageBitmap = remember(imagen) { ProcesadorImagen.aImageBitmap(imagen) }
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                filterQuality = FilterQuality.None,
                // Ocupa la fracción correcta del ancho del papel (p. ej. 432/576 = 75%)
                modifier = Modifier.fillMaxWidth(imagen.ancho.toFloat() / 576f),
            )
        }
    }
}
