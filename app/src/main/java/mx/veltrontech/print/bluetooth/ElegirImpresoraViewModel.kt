package mx.veltrontech.print.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.veltrontech.print.datos.PreferenciasRepository

class ElegirImpresoraViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ElegirUiState())
    val uiState: StateFlow<ElegirUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenciasRepository.impresoraGuardada.collect { impresora ->
                _uiState.update { it.copy(macActual = impresora?.mac) }
            }
        }
        recargar()
    }

    fun recargar() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(cargando = true) }
            val dispositivos = bluetoothRepository.dispositivosEmparejados()
            _uiState.update { it.copy(dispositivos = dispositivos, cargando = false) }
        }
    }

    fun seleccionarImpresora(dispositivo: DispositivoEmparejado) {
        viewModelScope.launch {
            preferenciasRepository.guardarImpresora(
                ImpresoraGuardada(dispositivo.nombre, dispositivo.mac)
            )
        }
    }
}

data class ElegirUiState(
    val dispositivos: List<DispositivoEmparejado> = emptyList(),
    val macActual: String? = null,
    val cargando: Boolean = false,
)
