package mx.veltrontech.print.escpos

import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.imagen.LogoRepository

class ServicioImpresion(
    private val motor: MotorEscPos,
    private val printerConnection: PrinterConnection,
    private val logoRepository: LogoRepository,
) {
    suspend fun imprimir(trabajo: Trabajo): ResultadoEnvio {
        val logo = logoRepository.cargarMonocromo()
        return printerConnection.enviar(motor.generar(trabajo, logo))
    }

    suspend fun imprimirLogoPrueba(): ResultadoEnvio {
        val logo = logoRepository.cargarMonocromo() ?: return ResultadoEnvio.Exito
        val trabajo = Trabajo(
            v = 1,
            tipo = "prueba",
            bloques = listOf(Bloque.Logo(), Bloque.Corte()),
        )
        return printerConnection.enviar(motor.generar(trabajo, logo))
    }
}
