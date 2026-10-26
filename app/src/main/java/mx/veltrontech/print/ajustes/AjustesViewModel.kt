package mx.veltrontech.print.ajustes

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.datos.PreferenciasRepository
import mx.veltrontech.print.escpos.ServicioImpresion
import mx.veltrontech.print.imagen.ConversorImagen
import mx.veltrontech.print.imagen.EdicionLogo
import mx.veltrontech.print.imagen.ImagenMonocromo
import mx.veltrontech.print.imagen.LogoGuardado
import mx.veltrontech.print.imagen.LogoRepository
import mx.veltrontech.print.imagen.ModoConversion
import mx.veltrontech.print.imagen.OpcionesLogo
import mx.veltrontech.print.imagen.ProcesadorImagen
import mx.veltrontech.print.imagen.TamanoLogo

@OptIn(FlowPreview::class)
class AjustesViewModel(
    private val logoRepository: LogoRepository,
    private val servicioImpresion: ServicioImpresion,
    private val preferenciasRepository: PreferenciasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    private val _eventoSnackbar = Channel<Int>(Channel.BUFFERED)
    val eventoSnackbar = _eventoSnackbar.receiveAsFlow()

    private var bitmapOriginal: Bitmap? = null

    // debounce: descarta movimientos intermedios del slider de claridad;
    // solo reprocesa cuando el usuario deja de mover por 150 ms.
    private val _claridadFlow = MutableStateFlow(OpcionesLogo.PREDETERMINADO.claridad)

    init {
        viewModelScope.launch {
            logoRepository.logo.collect { logoGuardado ->
                if (logoGuardado != null) {
                    val preview = logoRepository.cargarMonocromo()
                    _uiState.update { it.copy(guardado = logoGuardado, vistaPreviaGuardada = preview) }
                } else {
                    _uiState.update { it.copy(guardado = null, vistaPreviaGuardada = null) }
                }
            }
        }
        viewModelScope.launch {
            preferenciasRepository.impresoraGuardada.collect { impresora ->
                _uiState.update { it.copy(hayImpresora = impresora != null) }
            }
        }
        viewModelScope.launch {
            _claridadFlow.debounce(150).collect { nivel ->
                val opciones = _uiState.value.edicion?.opciones ?: return@collect
                recalcularConOpciones(opciones.copy(claridad = nivel))
            }
        }
    }

    fun onImagenElegida(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = true, errorImagen = false) }
            val bitmap = ProcesadorImagen.decodificarBitmap(context, uri)
            if (bitmap == null) {
                _uiState.update { it.copy(procesando = false, errorImagen = true) }
                return@launch
            }
            bitmapOriginal?.recycle()
            bitmapOriginal = bitmap
            val opciones = OpcionesLogo.PREDETERMINADO
            val vistaPrevia = ProcesadorImagen.procesarBitmap(bitmap, opciones)
            _claridadFlow.value = opciones.claridad
            _uiState.update { it.copy(
                procesando = false,
                errorImagen = false,
                edicion = EdicionLogo(opciones, vistaPrevia),
            )}
        }
    }

    fun onModoChanged(modo: ModoConversion) {
        val opciones = _uiState.value.edicion?.opciones ?: return
        viewModelScope.launch { recalcularConOpciones(opciones.copy(modo = modo)) }
    }

    fun onClaridadChanged(nivel: Int) {
        val opciones = _uiState.value.edicion?.opciones ?: return
        _uiState.update { it.copy(edicion = it.edicion?.copy(opciones = opciones.copy(claridad = nivel))) }
        _claridadFlow.value = nivel
    }

    fun onTamanoChanged(tamano: TamanoLogo) {
        val opciones = _uiState.value.edicion?.opciones ?: return
        viewModelScope.launch { recalcularConOpciones(opciones.copy(tamano = tamano)) }
    }

    fun guardar() {
        val edicion = _uiState.value.edicion ?: return
        val bitmap = bitmapOriginal ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(procesando = true) }
            try {
                logoRepository.guardar(bitmap, edicion.opciones, edicion.vistaPrevia)
                _uiState.update { it.copy(procesando = false, edicion = null) }
                _eventoSnackbar.send(R.string.ajustes_logo_guardado)
            } catch (_: Exception) {
                _uiState.update { it.copy(procesando = false) }
                _eventoSnackbar.send(R.string.error_logo_no_guardado)
            }
        }
    }

    fun quitarLogo() {
        viewModelScope.launch {
            logoRepository.quitar()
            bitmapOriginal?.recycle()
            bitmapOriginal = null
            _uiState.update { it.copy(edicion = null) }
        }
    }

    fun descartarCambios() {
        bitmapOriginal?.recycle()
        bitmapOriginal = null
        _uiState.update { it.copy(edicion = null, errorImagen = false) }
    }

    fun imprimirLogoPrueba() {
        if (_uiState.value.imprimiendo) return
        _uiState.update { it.copy(imprimiendo = true) }
        viewModelScope.launch {
            val resultado = servicioImpresion.imprimirLogoPrueba()
            _uiState.update { it.copy(imprimiendo = false) }
            resultado.aStringRes()?.let { _eventoSnackbar.send(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bitmapOriginal?.recycle()
        bitmapOriginal = null
    }

    private suspend fun recalcularConOpciones(nuevasOpciones: OpcionesLogo) {
        val bitmap = bitmapOriginal ?: return
        _uiState.update { it.copy(procesando = true) }
        val nuevaPrevia = ProcesadorImagen.procesarBitmap(bitmap, nuevasOpciones)
        _uiState.update { it.copy(
            procesando = false,
            edicion = it.edicion?.copy(opciones = nuevasOpciones, vistaPrevia = nuevaPrevia),
        )}
    }
}

data class AjustesUiState(
    val guardado: LogoGuardado? = null,
    val vistaPreviaGuardada: ImagenMonocromo? = null,
    val edicion: EdicionLogo? = null,
    val hayImpresora: Boolean = false,
    val procesando: Boolean = false,
    val imprimiendo: Boolean = false,
    val errorImagen: Boolean = false,
) {
    val hayCambios: Boolean get() = edicion != null
    val vistaPrevia: ImagenMonocromo? get() = edicion?.vistaPrevia ?: vistaPreviaGuardada
}

private fun ResultadoEnvio.aStringRes(): Int? = when (this) {
    ResultadoEnvio.Exito -> null
    ResultadoEnvio.SinBluetooth -> R.string.error_sin_bluetooth
    ResultadoEnvio.BluetoothApagado -> R.string.error_bluetooth_apagado
    ResultadoEnvio.SinPermiso, ResultadoEnvio.SinImpresora -> R.string.error_permiso_negado
    ResultadoEnvio.NoEmparejada -> R.string.error_ya_no_emparejada
    ResultadoEnvio.NoResponde -> R.string.error_no_responde
    ResultadoEnvio.ConexionPerdida -> R.string.error_conexion_perdida
}
