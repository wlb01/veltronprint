package mx.veltrontech.print.recepcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.contrato.DecodificadorEnlace
import mx.veltrontech.print.contrato.ResultadoDecodificacion
import mx.veltrontech.print.contrato.ResultadoValidacion
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.contrato.ValidadorTrabajo
import mx.veltrontech.print.escpos.ResultadoImpresion
import mx.veltrontech.print.escpos.ServicioImpresion

sealed interface RecepcionUiState {
    data class Imprimiendo(val subtitulo: String) : RecepcionUiState
    data object Impreso : RecepcionUiState
    data class Error(
        val mensajeRes: Int,
        val argumento: Any? = null,
        val detalle: String? = null,
        val puedeReintentar: Boolean = false,
        val puedeVincular: Boolean = false,
    ) : RecepcionUiState
}

class RecibirTrabajoViewModel(
    private val servicioImpresion: ServicioImpresion,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecepcionUiState>(RecepcionUiState.Imprimiendo(""))
    val uiState: StateFlow<RecepcionUiState> = _uiState.asStateFlow()

    private val _eventoTerminado = Channel<Unit>(Channel.CONFLATED)
    val eventoTerminado = _eventoTerminado.receiveAsFlow()

    private var trabajoGuardado: Trabajo? = null

    fun procesar(d: String?) {
        viewModelScope.launch {
            when (val dec = DecodificadorEnlace.decodificar(d)) {
                ResultadoDecodificacion.SinDatos ->
                    _uiState.value = RecepcionUiState.Error(R.string.error_rec_sin_datos)
                ResultadoDecodificacion.Danado ->
                    _uiState.value = RecepcionUiState.Error(R.string.error_rec_danado)
                is ResultadoDecodificacion.Ok -> when (val val_ = ValidadorTrabajo.validar(dec.json)) {
                    ResultadoValidacion.Danado ->
                        _uiState.value = RecepcionUiState.Error(R.string.error_rec_danado)
                    is ResultadoValidacion.VersionDesconocida ->
                        _uiState.value = RecepcionUiState.Error(
                            mensajeRes = R.string.error_rec_version,
                            argumento = val_.v,
                        )
                    is ResultadoValidacion.Invalido ->
                        _uiState.value = RecepcionUiState.Error(
                            mensajeRes = R.string.error_rec_formato,
                            detalle = val_.detalle,
                        )
                    is ResultadoValidacion.Valido -> imprimir(val_.trabajo)
                }
            }
        }
    }

    fun reintentar() {
        val trabajo = trabajoGuardado ?: return
        viewModelScope.launch { imprimir(trabajo) }
    }

    private suspend fun imprimir(trabajo: Trabajo) {
        trabajoGuardado = trabajo
        _uiState.value = RecepcionUiState.Imprimiendo(construirSubtitulo(trabajo))
        when (val r = servicioImpresion.imprimir(trabajo)) {
            ResultadoImpresion.Exito -> {
                _uiState.value = RecepcionUiState.Impreso
                delay(2_000)
                _eventoTerminado.send(Unit)
            }
            is ResultadoImpresion.ErrorContenido ->
                _uiState.value = RecepcionUiState.Error(mensajeRes = r.mensajeRes)
            is ResultadoImpresion.ErrorConexion -> {
                val (msg, reintentar, vincular) = mapearErrorConexion(r.resultado)
                _uiState.value = RecepcionUiState.Error(
                    mensajeRes = msg,
                    puedeReintentar = reintentar,
                    puedeVincular = vincular,
                )
            }
        }
    }

    private fun construirSubtitulo(trabajo: Trabajo): String {
        val tipo = trabajo.tipo.replaceFirstChar { it.uppercase() }
        return if (trabajo.folio != null) "$tipo · ${trabajo.folio}" else tipo
    }

    private data class InfoError(val mensajeRes: Int, val reintentar: Boolean, val vincular: Boolean)

    private fun mapearErrorConexion(r: ResultadoEnvio): InfoError {
        if (r == ResultadoEnvio.SinImpresora) return InfoError(R.string.error_rec_sin_impresora, false, true)
        val msg = when (r) {
            ResultadoEnvio.SinBluetooth -> R.string.error_sin_bluetooth
            ResultadoEnvio.BluetoothApagado -> R.string.error_bluetooth_apagado
            ResultadoEnvio.SinPermiso -> R.string.error_permiso_negado
            ResultadoEnvio.NoEmparejada -> R.string.error_ya_no_emparejada
            ResultadoEnvio.NoResponde -> R.string.error_no_responde
            else -> R.string.error_conexion_perdida
        }
        return InfoError(msg, true, false)
    }
}
