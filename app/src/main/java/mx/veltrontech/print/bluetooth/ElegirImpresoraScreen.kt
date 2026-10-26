package mx.veltrontech.print.bluetooth

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.veltrontech.print.AppContainer
import mx.veltrontech.print.R
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirImpresoraScreen(
    container: AppContainer,
    onRegresar: () -> Unit,
    viewModel: ElegirImpresoraViewModel = viewModel {
        ElegirImpresoraViewModel(container.bluetoothRepository, container.preferenciasRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Recargar la lista al volver de los Ajustes de Bluetooth
    LaunchedEffect(context) {
        (context as? ComponentActivity)?.lifecycle?.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.recargar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.elegir_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Layout adaptable: horizontal en tablet landscape, vertical en teléfono o portrait
            if (maxWidth > 600.dp) {
                Row(modifier = Modifier.fillMaxSize()) {
                    PanelAyuda(
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(24.dp),
                        context = context,
                    )
                    ListaDispositivos(
                        uiState = uiState,
                        onSeleccionar = { dispositivo ->
                            viewModel.seleccionarImpresora(dispositivo)
                            onRegresar()
                        },
                        context = context,
                        modifier = Modifier
                            .weight(0.6f)
                            .padding(24.dp),
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    PanelAyuda(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        context = context,
                    )
                    HorizontalDivider()
                    ListaDispositivos(
                        uiState = uiState,
                        onSeleccionar = { dispositivo ->
                            viewModel.seleccionarImpresora(dispositivo)
                            onRegresar()
                        },
                        context = context,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelAyuda(context: android.content.Context, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.elegir_ayuda),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
            modifier = Modifier.height(48.dp),
        ) {
            Text(stringResource(R.string.elegir_boton_ajustes_bt))
        }
    }
}

@Composable
private fun ListaDispositivos(
    uiState: ElegirUiState,
    onSeleccionar: (DispositivoEmparejado) -> Unit,
    context: android.content.Context,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            uiState.cargando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.dispositivos.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.elegir_sin_dispositivos),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(stringResource(R.string.elegir_boton_ajustes_bt))
                    }
                }
            }
            else -> {
                LazyColumn {
                    items(uiState.dispositivos, key = { it.mac }) { dispositivo ->
                        FilaDispositivo(
                            dispositivo = dispositivo,
                            esActual = dispositivo.mac == uiState.macActual,
                            onClick = { onSeleccionar(dispositivo) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDispositivo(
    dispositivo: DispositivoEmparejado,
    esActual: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dispositivo.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = dispositivo.mac,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (esActual) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.elegir_etiqueta_actual),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true, name = "Elegir — con dispositivos")
@Composable
private fun ElegirConDispositivosPreview() {
    VeltronPrintTheme {
        val fakeState = ElegirUiState(
            dispositivos = listOf(
                DispositivoEmparejado("Honor X8", "AA:BB:CC:DD:EE:01"),
                DispositivoEmparejado("POS-8390L", "86:67:7A:12:34:56"),
                DispositivoEmparejado("Samsung Galaxy", "AA:BB:CC:DD:EE:03"),
            ),
            macActual = "86:67:7A:12:34:56",
        )
        Scaffold { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                ListaDispositivos(fakeState, {}, androidx.compose.ui.platform.LocalContext.current)
            }
        }
    }
}

@Preview(showBackground = true, name = "Elegir — vacía")
@Composable
private fun ElegirVaciaPreview() {
    VeltronPrintTheme {
        Scaffold { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                ListaDispositivos(ElegirUiState(), {}, androidx.compose.ui.platform.LocalContext.current)
            }
        }
    }
}
