package mx.veltrontech.print.escpos

import mx.veltrontech.print.R
import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.codigos.CodigoInvalidoException
import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.imagen.LogoRepository

class ServicioImpresion(
    private val motor: MotorEscPos,
    private val printerConnection: PrinterConnection,
    private val logoRepository: LogoRepository,
) {
    suspend fun imprimir(trabajo: Trabajo): ResultadoImpresion {
        val logo = logoRepository.cargarMonocromo()
        val bytes = try {
            motor.generar(trabajo, logo)
        } catch (e: CodigoInvalidoException) {
            return ResultadoImpresion.ErrorContenido(R.string.error_codigo_invalido)
        }
        return printerConnection.enviar(bytes).aResultado()
    }

    suspend fun imprimirLogoPrueba(): ResultadoImpresion {
        val logo = logoRepository.cargarMonocromo() ?: return ResultadoImpresion.Exito
        val trabajo = Trabajo(
            v = 1,
            tipo = "prueba",
            bloques = listOf(Bloque.Logo(), Bloque.Corte()),
        )
        return printerConnection.enviar(motor.generar(trabajo, logo)).aResultado()
    }

    private fun ResultadoEnvio.aResultado(): ResultadoImpresion = when (this) {
        ResultadoEnvio.Exito -> ResultadoImpresion.Exito
        else -> ResultadoImpresion.ErrorConexion(this)
    }
}
