package mx.veltrontech.print.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.veltrontech.print.R
import mx.veltrontech.print.ui.theme.VeltronPrintTheme

// @Composable: función que describe una parte de la UI, como un componente de React.
// Se redibuja automáticamente cuando cambia el estado o los parámetros.
@Composable
fun AvisoPermiso(
    negadoPermanente: Boolean,
    onSolicitarPermiso: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.permiso_descripcion),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = if (negadoPermanente) onAbrirAjustes else onSolicitarPermiso,
            modifier = Modifier.height(48.dp),
        ) {
            Text(
                text = stringResource(
                    if (negadoPermanente) R.string.permiso_boton_ajustes
                    else R.string.permiso_boton_dar
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Permiso — solicitar")
@Composable
private fun AvisoPermisoPreview() {
    VeltronPrintTheme {
        AvisoPermiso(
            negadoPermanente = false,
            onSolicitarPermiso = {},
            onAbrirAjustes = {},
        )
    }
}

@Preview(showBackground = true, name = "Permiso — negado permanente")
@Composable
private fun AvisoPermisoPermanentePreview() {
    VeltronPrintTheme {
        AvisoPermiso(
            negadoPermanente = true,
            onSolicitarPermiso = {},
            onAbrirAjustes = {},
        )
    }
}
