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
import mx.veltrontech.print.R
import mx.veltrontech.print.ajustes.ticketPrueba
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.datos.PreferenciasRepository
import mx.veltrontech.print.escpos.ResultadoImpresion
import mx.veltrontech.print.escpos.ServicioImpresion
import java.time.LocalTime

class InicioViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val printerConnection: PrinterConnection,
    private val servicioImpresion: ServicioImpresion,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    private val _eventoSnackbar = Channel<Int>(Channel.BUFFERED)
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
            val hora = LocalTime.now()
            when (val resultado = servicioImpresion.imprimir(ticketPrueba())) {
                ResultadoImpresion.Exito -> {
                    _eventoSnackbar.send(R.string.prueba_snackbar_exito)
                    _uiState.update { it.copy(imprimiendo = false) }
                }
                is ResultadoImpresion.ErrorContenido -> {
                    _eventoSnackbar.send(resultado.mensajeRes)
                    _uiState.update { it.copy(imprimiendo = false) }
                }
                is ResultadoImpresion.ErrorConexion -> {
                    _uiState.update { state ->
                        state.copy(
                            imprimiendo = false,
                            estado = EstadoImpresora.NoResponde,
                            ultimaPrueba = hora,
                            error = resultado.resultado,
                        )
                    }
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
