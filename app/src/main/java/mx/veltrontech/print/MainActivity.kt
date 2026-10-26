package mx.veltrontech.print

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.veltrontech.print.ui.navegacion.VeltronNavHost
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // edge-to-edge: el contenido llega hasta las barras del sistema
        val container = (application as VeltronPrintApp).container
        setContent {
            VeltronPrintTheme {
                VeltronNavHost(container = container)
            }
        }
    }
}