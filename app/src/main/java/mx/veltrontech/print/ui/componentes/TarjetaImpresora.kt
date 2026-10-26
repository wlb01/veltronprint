package mx.veltrontech.print.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.inicio.EstadoImpresora
import mx.veltrontech.print.ui.theme.VeltronPrintTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FMT_HORA = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun TarjetaImpresora(
    impresora: ImpresoraGuardada?,
    estado: EstadoImpresora,
    ultimaPrueba: LocalTime?,
    error: ResultadoEnvio?,
    onVincular: () -> Unit,
    onProbar: () -> Unit,
    onCambiar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.inicio_label_impresora),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(16.dp))

            if (impresora == null) {
                ContenidoSinImpresora(onVincular)
            } else {
                ContenidoConImpresora(
                    impresora = impresora,
                    estado = estado,
                    ultimaPrueba = ultimaPrueba,
                    error = error,
                    onProbar = onProbar,
                    onCambiar = onCambiar,
                )
            }
        }
    }
}

@Composable
private fun ContenidoSinImpresora(onVincular: () -> Unit) {
    Text(
        text = stringResource(R.string.inicio_sin_impresora),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = onVincular,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(stringResource(R.string.inicio_boton_vincular))
    }
}

@Composable
private fun ContenidoConImpresora(
    impresora: ImpresoraGuardada,
    estado: EstadoImpresora,
    ultimaPrueba: LocalTime?,
    error: ResultadoEnvio?,
    onProbar: () -> Unit,
    onCambiar: () -> Unit,
) {
    Text(
        text = impresora.nombre,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = impresora.mac,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))

    // Indicador de estado
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (estado == EstadoImpresora.Probando) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = stringResource(
                when (estado) {
                    EstadoImpresora.SinProbar -> R.string.inicio_estado_sin_probar
                    EstadoImpresora.Probando -> R.string.inicio_estado_probando
                    EstadoImpresora.Lista -> R.string.inicio_estado_lista
                    EstadoImpresora.NoResponde -> R.string.inicio_estado_no_responde
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = when (estado) {
                EstadoImpresora.Lista -> Color(0xFF2E7D32)
                EstadoImpresora.NoResponde -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }

    // Hora de la última prueba
    ultimaPrueba?.let { hora ->
        Text(
            text = stringResource(R.string.inicio_ultima_prueba, hora.format(FMT_HORA)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    // Mensaje de error (cuando No responde)
    error?.let { r ->
        Spacer(Modifier.height(8.dp))
        Text(
            text = errorTexto(r),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onProbar,
            enabled = estado != EstadoImpresora.Probando,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
        ) {
            Text(stringResource(R.string.inicio_boton_probar))
        }
        OutlinedButton(
            onClick = onCambiar,
            modifier = Modifier.height(48.dp),
        ) {
            Text(stringResource(R.string.inicio_boton_cambiar))
        }
    }
}

// Mapea ResultadoEnvio a los mensajes exactos del spec §5
@Composable
private fun errorTexto(resultado: ResultadoEnvio): String = when (resultado) {
    ResultadoEnvio.Exito -> ""
    ResultadoEnvio.SinBluetooth -> stringResource(R.string.error_sin_bluetooth)
    ResultadoEnvio.BluetoothApagado -> stringResource(R.string.error_bluetooth_apagado)
    ResultadoEnvio.SinPermiso, ResultadoEnvio.SinImpresora ->
        stringResource(R.string.error_permiso_negado)
    ResultadoEnvio.NoEmparejada -> stringResource(R.string.error_ya_no_emparejada)
    ResultadoEnvio.NoResponde -> stringResource(R.string.error_no_responde)
    ResultadoEnvio.ConexionPerdida -> stringResource(R.string.error_conexion_perdida)
}

// Previews para los 4 estados de la tarjeta
@Preview(showBackground = true, name = "Sin impresora")
@Composable
private fun TarjetaSinImpresoraPreview() {
    VeltronPrintTheme {
        TarjetaImpresora(null, EstadoImpresora.SinProbar, null, null, {}, {}, {},
            modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Sin probar")
@Composable
private fun TarjetaSinProbarPreview() {
    VeltronPrintTheme {
        TarjetaImpresora(ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            EstadoImpresora.SinProbar, null, null, {}, {}, {},
            modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Probando…")
@Composable
private fun TarjetaProbandoPreview() {
    VeltronPrintTheme {
        TarjetaImpresora(ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            EstadoImpresora.Probando, null, null, {}, {}, {},
            modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Lista")
@Composable
private fun TarjetaListaPreview() {
    VeltronPrintTheme {
        TarjetaImpresora(ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            EstadoImpresora.Lista, LocalTime.of(14, 30), null, {}, {}, {},
            modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "No responde")
@Composable
private fun TarjetaNoRespondePreview() {
    VeltronPrintTheme {
        TarjetaImpresora(ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            EstadoImpresora.NoResponde, LocalTime.of(14, 30), ResultadoEnvio.NoResponde, {}, {}, {},
            modifier = Modifier.padding(16.dp))
    }
}
