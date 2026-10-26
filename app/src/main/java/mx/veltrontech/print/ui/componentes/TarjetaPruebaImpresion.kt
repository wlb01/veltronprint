package mx.veltrontech.print.ui.componentes

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

@Composable
fun TarjetaPruebaImpresion(
    impresora: ImpresoraGuardada?,
    imprimiendo: Boolean,
    onImprimir: () -> Unit,
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
                    text = stringResource(R.string.prueba_titulo),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.prueba_descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onImprimir,
                enabled = impresora != null && !imprimiendo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (imprimiendo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.prueba_boton_imprimiendo))
                } else {
                    Text(stringResource(R.string.prueba_boton_imprimir))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Sin impresora")
@Composable
private fun PruebaSinImpresoraPreview() {
    VeltronPrintTheme {
        TarjetaPruebaImpresion(null, false, {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Lista para imprimir")
@Composable
private fun PruebaListaPreview() {
    VeltronPrintTheme {
        TarjetaPruebaImpresion(
            ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            false, {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Imprimiendo…")
@Composable
private fun PruebaImprimiendoPreview() {
    VeltronPrintTheme {
        TarjetaPruebaImpresion(
            ImpresoraGuardada("POS-8390L", "86:67:7A:12:34:56"),
            true, {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
