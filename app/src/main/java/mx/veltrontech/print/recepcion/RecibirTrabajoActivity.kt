package mx.veltrontech.print.recepcion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import mx.veltrontech.print.MainActivity
import mx.veltrontech.print.VeltronPrintApp
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

// Activity separada de MainActivity:
// - taskAffinity="" en el manifest: vive en su propia tarea; no altera la pila de MainActivity.
// - finishAndRemoveTask(): cierra la pantalla y la quita de Recientes; el foco vuelve a Chrome.
// - savedInstanceState == null: procesa el enlace solo en la primera creación; girar la tablet
//   recrea la Activity pero savedInstanceState no es null, así que no vuelve a imprimir.
class RecibirTrabajoActivity : ComponentActivity() {

    private val viewModel: RecibirTrabajoViewModel by viewModels {
        val container = (application as VeltronPrintApp).container
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                RecibirTrabajoViewModel(container.servicioImpresion) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            viewModel.procesar(intent?.data?.getQueryParameter("d"))
        }

        setContent {
            VeltronPrintTheme {
                RecibirTrabajoScreen(
                    viewModel = viewModel,
                    onTerminado = { finishAndRemoveTask() },
                    onVincularImpresora = {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                putExtra("abrir", "elegir_impresora")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        finishAndRemoveTask()
                    },
                    onCerrar = { finishAndRemoveTask() },
                )
            }
        }
    }
}
