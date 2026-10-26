package mx.veltrontech.print.ajustes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.veltrontech.print.R
import mx.veltrontech.print.imagen.ModoConversion
import mx.veltrontech.print.imagen.TamanoLogo

/** Controles y botones del logo. La vista previa la gestiona AjustesScreen para el layout adaptable. */
@Composable
fun SeccionLogo(
    uiState: AjustesUiState,
    viewModel: AjustesViewModel,
    onPedirQuitarLogo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.onImagenElegida(context, uri)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.ajustes_logo_titulo),
            style = MaterialTheme.typography.titleMedium,
        )

        val hayLogo = uiState.guardado != null || uiState.edicion != null

        if (!hayLogo) {
            Text(
                text = stringResource(R.string.ajustes_logo_sin_logo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (uiState.errorImagen) {
            Text(
                text = stringResource(R.string.error_imagen_invalida),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (hayLogo) {
            val edicion = uiState.edicion
            val opciones = edicion?.opciones ?: uiState.guardado?.opciones

            Text(
                text = stringResource(R.string.ajustes_logo_modo),
                style = MaterialTheme.typography.labelMedium,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = opciones?.modo == ModoConversion.UMBRAL || opciones == null,
                    onClick = { viewModel.onModoChanged(ModoConversion.UMBRAL) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    enabled = edicion != null,
                ) { Text(stringResource(R.string.ajustes_logo_modo_umbral)) }
                SegmentedButton(
                    selected = opciones?.modo == ModoConversion.TRAMADO,
                    onClick = { viewModel.onModoChanged(ModoConversion.TRAMADO) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    enabled = edicion != null,
                ) { Text(stringResource(R.string.ajustes_logo_modo_tramado)) }
            }

            if (opciones?.modo != ModoConversion.TRAMADO) {
                Text(
                    text = stringResource(R.string.ajustes_logo_claridad),
                    style = MaterialTheme.typography.labelMedium,
                )
                Slider(
                    value = (opciones?.claridad ?: 128).toFloat(),
                    onValueChange = { viewModel.onClaridadChanged(it.toInt()) },
                    valueRange = 0f..255f,
                    enabled = edicion != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.ajustes_logo_claridad_descripcion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.ajustes_logo_tamano),
                style = MaterialTheme.typography.labelMedium,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val tamanos = listOf(
                    TamanoLogo.CHICO to stringResource(R.string.ajustes_logo_50),
                    TamanoLogo.MEDIANO to stringResource(R.string.ajustes_logo_75),
                    TamanoLogo.COMPLETO to stringResource(R.string.ajustes_logo_100),
                )
                tamanos.forEachIndexed { i, (tamano, label) ->
                    SegmentedButton(
                        selected = opciones?.tamano == tamano,
                        onClick = { viewModel.onTamanoChanged(tamano) },
                        shape = SegmentedButtonDefaults.itemShape(i, tamanos.size),
                        enabled = edicion != null,
                    ) { Text(label) }
                }
            }

            if (uiState.procesando) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(Modifier.height(4.dp))

        OutlinedButton(
            onClick = { selector.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.procesando && !uiState.imprimiendo,
        ) {
            Text(
                if (hayLogo) stringResource(R.string.ajustes_logo_elegir_otra)
                else stringResource(R.string.ajustes_logo_elegir)
            )
        }

        if (hayLogo) {
            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.hayCambios && !uiState.procesando && !uiState.imprimiendo,
            ) { Text(stringResource(R.string.ajustes_logo_guardar)) }

            OutlinedButton(
                onClick = { viewModel.imprimirLogoPrueba() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.guardado != null && !uiState.hayCambios
                        && uiState.hayImpresora && !uiState.imprimiendo && !uiState.procesando,
            ) { Text(stringResource(R.string.ajustes_logo_imprimir_prueba)) }

            OutlinedButton(
                onClick = onPedirQuitarLogo,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.guardado != null && !uiState.imprimiendo,
            ) { Text(stringResource(R.string.ajustes_logo_quitar)) }
        }
    }
}
