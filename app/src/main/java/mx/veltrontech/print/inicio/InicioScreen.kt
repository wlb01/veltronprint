package mx.veltrontech.print.inicio

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.veltrontech.print.AppContainer
import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.ui.componentes.AvisoPermiso
import mx.veltrontech.print.ui.componentes.TarjetaImpresora
import mx.veltrontech.print.ui.theme.VeltronPrintTheme
import mx.veltrontech.print.bluetooth.PermisosBluetooth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    container: AppContainer,
    onVincularImpresora: () -> Unit,
    viewModel: InicioViewModel = viewModel {
        InicioViewModel(container.preferenciasRepository, container.printerConnection)
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- Manejo de permisos (solo en API 31+) ---
    var tienePermiso by remember { mutableStateOf(PermisosBluetooth.tienePermisos(context)) }
    var negadoPermanente by remember { mutableStateOf(false) }

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        tienePermiso = resultados.values.all { it }
        if (!tienePermiso && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            negadoPermanente = PermisosBluetooth.permisosRequeridos(Build.VERSION.SDK_INT).any { permiso ->
                !(context as Activity).shouldShowRequestPermissionRationale(permiso)
            }
        }
    }

    // Re-verificar permisos al volver de los Ajustes de Android
    LaunchedEffect(context) {
        (context as? ComponentActivity)?.lifecycle?.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            tienePermiso = PermisosBluetooth.tienePermisos(context)
        }
    }

    // --- Launcher para pedir encender Bluetooth ---
    val encenderBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onVincularImpresora()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.inicio_titulo),
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { innerPadding ->
        // Centrar con ancho máximo para que no se estire en la tablet
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !tienePermiso) {
                AvisoPermiso(
                    negadoPermanente = negadoPermanente,
                    onSolicitarPermiso = {
                        permisosLauncher.launch(
                            PermisosBluetooth.permisosRequeridos(Build.VERSION.SDK_INT).toTypedArray()
                        )
                    },
                    onAbrirAjustes = {
                        context.startActivity(
                            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                    modifier = Modifier.widthIn(max = 560.dp),
                )
            } else {
                TarjetaImpresora(
                    impresora = uiState.impresora,
                    estado = uiState.estado,
                    ultimaPrueba = uiState.ultimaPrueba,
                    error = uiState.error,
                    onVincular = {
                        if (android.bluetooth.BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
                            @Suppress("DEPRECATION")
                            encenderBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        } else {
                            onVincularImpresora()
                        }
                    },
                    onProbar = viewModel::probarConexion,
                    onCambiar = onVincularImpresora,
                    modifier = Modifier.widthIn(max = 560.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Inicio — sin impresora")
@Composable
private fun InicioSinImpresoraPreview() {
    VeltronPrintTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Veltron Print") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad).padding(16.dp), Alignment.TopCenter) {
                TarjetaImpresora(null, EstadoImpresora.SinProbar, null, null, {}, {}, {},
                    modifier = Modifier.widthIn(max = 560.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Inicio — con impresora, lista")
@Composable
private fun InicioConImpresoraPreview() {
    VeltronPrintTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Veltron Print") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad).padding(16.dp), Alignment.TopCenter) {
                TarjetaImpresora(
                    impresora = ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
                    estado = EstadoImpresora.Lista,
                    ultimaPrueba = java.time.LocalTime.of(10, 45),
                    error = null,
                    onVincular = {}, onProbar = {}, onCambiar = {},
                    modifier = Modifier.widthIn(max = 560.dp),
                )
            }
        }
    }
}
