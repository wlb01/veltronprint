package mx.veltrontech.print.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.veltrontech.print.bluetooth.ImpresoraGuardada
import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.datos.PreferenciasRepository
import java.time.LocalTime

// ViewModel: sobrevive las rotaciones de pantalla. La pantalla solo observa su StateFlow.
// StateFlow: como useState de React, pero la pantalla "suscribe" y se redibuja automáticamente.
// viewModelScope: el alcance de corrutinas del ViewModel; se cancela cuando el VM se destruye.
class InicioViewModel(
    private val preferenciasRepository: PreferenciasRepository,
    private val printerConnection: PrinterConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    init {
        // Observa la impresora guardada; si la MAC cambia, resetea a SinProbar
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
                            error = null
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
}

data class InicioUiState(
    val impresora: ImpresoraGuardada? = null,
    val estado: EstadoImpresora = EstadoImpresora.SinProbar,
    val ultimaPrueba: LocalTime? = null,
    val error: ResultadoEnvio? = null,
)

enum class EstadoImpresora { SinProbar, Probando, Lista, NoResponde }
