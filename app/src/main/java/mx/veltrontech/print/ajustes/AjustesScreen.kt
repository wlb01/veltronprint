package mx.veltrontech.print.ajustes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.veltrontech.print.R
import mx.veltrontech.print.ui.componentes.VistaPreviaPapel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    onRegresar: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var mostrarDialogoCambios by remember { mutableStateOf(false) }
    var mostrarDialogoQuitar by remember { mutableStateOf(false) }

    // BackHandler: intercepta el botón regresar cuando hay cambios pendientes.
    // enabled=true → muestra el diálogo; enabled=false → deja pasar el back normalmente.
    BackHandler(enabled = uiState.hayCambios) {
        mostrarDialogoCambios = true
    }

    LaunchedEffect(viewModel) {
        viewModel.eventoSnackbar.collect { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
        }
    }

    if (mostrarDialogoCambios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCambios = false },
            title = { Text(stringResource(R.string.ajustes_dialog_cambios_titulo)) },
            text = { Text(stringResource(R.string.ajustes_dialog_cambios_texto)) },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoCambios = false }) {
                    Text(stringResource(R.string.ajustes_dialog_cambios_seguir))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCambios = false
                    viewModel.descartarCambios()
                    onRegresar()
                }) {
                    Text(stringResource(R.string.ajustes_dialog_cambios_descartar))
                }
            },
        )
    }

    if (mostrarDialogoQuitar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQuitar = false },
            text = { Text(stringResource(R.string.ajustes_dialog_quitar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoQuitar = false
                    viewModel.quitarLogo()
                }) {
                    Text(stringResource(R.string.ajustes_dialog_quitar_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoQuitar = false }) {
                    Text(stringResource(R.string.ajustes_dialog_quitar_cancelar))
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ajustes_titulo)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hayCambios) mostrarDialogoCambios = true else onRegresar()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_regresar),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val esHorizontal = maxWidth > 600.dp
            val hayPrevia = uiState.vistaPrevia != null

            if (esHorizontal && hayPrevia) {
                // Horizontal con logo: vista previa a la izquierda, controles a la derecha
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    VistaPreviaPapel(
                        imagen = uiState.vistaPrevia,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        SeccionLogo(
                            uiState = uiState,
                            viewModel = viewModel,
                            onPedirQuitarLogo = { mostrarDialogoQuitar = true },
                        )
                    }
                }
            } else {
                // Vertical (o sin logo): vista previa arriba si existe, controles debajo
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (hayPrevia) {
                        VistaPreviaPapel(
                            imagen = uiState.vistaPrevia,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        )
                    }
                    SeccionLogo(
                        uiState = uiState,
                        viewModel = viewModel,
                        onPedirQuitarLogo = { mostrarDialogoQuitar = true },
                    )
                }
            }
        }
    }
}
