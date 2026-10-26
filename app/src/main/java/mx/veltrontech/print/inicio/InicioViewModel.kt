package mx.veltrontech.print.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.veltrontech.print.ajustes.ticketPrueba
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.datos.PreferenciasRepository
import mx.veltrontech.print.escpos.ServicioImpresion
import java.time.LocalTime

// Channel: cola de mensajes asíncrona donde cada valor se consume exactamente una vez.
// Ideal para eventos de una sola vez como el snackbar.
// receiveAsFlow() lo convierte en un Flow que la pantalla puede colectar.
class InicioViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val printerConnection: PrinterConnection,
    private val servicioImpresion: ServicioImpresion,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    private val _eventoSnackbar = Channel<Unit>(Channel.BUFFERED)
    val eventoSnackbar = _eventoSnackbar.receiveAsFlow()

    init {
        var macAnterior: String? = null
        viewModelScope.launch {
            preferenciasRepository.impresoraGuardada.collect { impresora ->
                val macCambio = impresora?.mac != macAnterior
                macAnterior = impresora?.mac
                if (macCambio) {
                    _uiState.update {
                        it.copy(
                            impresora = impresora,
                            estado = EstadoImpresora.SinProbar,
                            ultimaPrueba = null,
                            error = null,
                        )
                    }
                } else {
                    _uiState.update { it.copy(impresora = impresora) }
                }
            }
        }
    }

    fun probarConexion() {
        if (_uiState.value.estado == EstadoImpresora.Probando) return
        _uiState.update { it.copy(estado = EstadoImpresora.Probando, error = null) }
        viewModelScope.launch {
            val resultado = printerConnection.probar()
            val hora = LocalTime.now()
            _uiState.update { state ->
                if (resultado is ResultadoEnvio.Exito) {
                    state.copy(estado = EstadoImpresora.Lista, ultimaPrueba = hora, error = null)
                } else {
                    state.copy(estado = EstadoImpresora.NoResponde, ultimaPrueba = hora, error = resultado)
                }
            }
        }
    }

    fun imprimirPrueba() {
        if (_uiState.value.imprimiendo) return
        _uiState.update { it.copy(imprimiendo = true) }
        viewModelScope.launch {
            val resultado = servicioImpresion.imprimir(ticketPrueba())
            val hora = LocalTime.now()
            if (resultado is ResultadoEnvio.Exito) {
                _eventoSnackbar.send(Unit)
                _uiState.update { it.copy(imprimiendo = false) }
            } else {
                _uiState.update { state ->
                    state.copy(
                        imprimiendo = false,
                        estado = EstadoImpresora.NoResponde,
                        ultimaPrueba = hora,
                        error = resultado,
                    )
                }
            }
        }
    }
}

data class InicioUiState(
    val impresora: ImpresoraGuardada? = null,
    val estado: EstadoImpresora = EstadoImpresora.SinProbar,
    val ultimaPrueba: LocalTime? = null,
    val error: ResultadoEnvio? = null,
    val imprimiendo: Boolean = false,
)

enum class EstadoImpresora { SinProbar, Probando, Lista, NoResponde }
