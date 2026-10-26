package mx.veltrontech.print.recepcion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.veltrontech.print.R
import mx.veltrontech.print.ui.theme.RojoError
import mx.veltrontech.print.ui.theme.VerdeExito

@Composable
fun RecibirTrabajoScreen(
    viewModel: RecibirTrabajoViewModel,
    onTerminado: () -> Unit,
    onVincularImpresora: () -> Unit,
    onCerrar: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventoTerminado.collect { onTerminado() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (val s = uiState) {
                    is RecepcionUiState.Imprimiendo -> ContenidoImprimiendo(s.subtitulo)
                    RecepcionUiState.Impreso -> ContenidoImpreso()
                    is RecepcionUiState.Error -> ContenidoError(
                        estado = s,
                        onReintentar = { viewModel.reintentar() },
                        onVincularImpresora = onVincularImpresora,
                        onCerrar = onCerrar,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContenidoImprimiendo(subtitulo: String) {
    Icon(
        imageVector = Icons.Filled.Print,
        contentDescription = null,
        modifier = Modifier.size(72.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.rec_titulo_imprimiendo),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )
    if (subtitulo.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(32.dp))
    CircularProgressIndicator()
}

@Composable
private fun ContenidoImpreso() {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(72.dp),
        tint = VerdeExito,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.rec_titulo_impreso),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.rec_subtitulo_regresando),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ContenidoError(
    estado: RecepcionUiState.Error,
    onReintentar: () -> Unit,
    onVincularImpresora: () -> Unit,
    onCerrar: () -> Unit,
) {
    Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = null,
        modifier = Modifier.size(72.dp),
        tint = RojoError,
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.rec_titulo_error),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(12.dp))
    val mensaje = if (estado.argumento != null)
        stringResource(estado.mensajeRes, estado.argumento)
    else
        stringResource(estado.mensajeRes)
    Text(
        text = mensaje,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
    if (estado.detalle != null) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = estado.detalle,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(32.dp))
    if (estado.puedeReintentar) {
        Button(
            onClick = onReintentar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.rec_boton_reintentar))
        }
        Spacer(Modifier.height(8.dp))
    }
    if (estado.puedeVincular) {
        Button(
            onClick = onVincularImpresora,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.rec_boton_vincular))
        }
        Spacer(Modifier.height(8.dp))
    }
    OutlinedButton(
        onClick = onCerrar,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.rec_boton_cerrar))
    }
}
