package mx.veltrontech.print

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.veltrontech.print.ui.navegacion.Rutas
import mx.veltrontech.print.ui.navegacion.VeltronNavHost
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VeltronPrintApp).container
        val startDestination = when (intent?.getStringExtra("abrir")) {
            "elegir_impresora" -> Rutas.ELEGIR_IMPRESORA
            else -> Rutas.INICIO
        }
        setContent {
            VeltronPrintTheme {
                VeltronNavHost(container = container, startDestination = startDestination)
            }
        }
    }
}