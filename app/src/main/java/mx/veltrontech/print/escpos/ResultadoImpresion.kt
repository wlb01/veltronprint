package mx.veltrontech.print.escpos

import mx.veltrontech.print.bluetooth.ResultadoEnvio

sealed interface ResultadoImpresion {
    data object Exito : ResultadoImpresion
    data class ErrorConexion(val resultado: ResultadoEnvio) : ResultadoImpresion
    data class ErrorContenido(val mensajeRes: Int) : ResultadoImpresion
}
