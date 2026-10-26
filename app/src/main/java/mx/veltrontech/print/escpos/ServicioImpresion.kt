package mx.veltrontech.print.escpos

import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.bluetooth.ResultadoEnvio
import mx.veltrontech.print.contrato.Trabajo

class ServicioImpresion(
    private val motor: MotorEscPos,
    private val printerConnection: PrinterConnection,
) {
    suspend fun imprimir(trabajo: Trabajo): ResultadoEnvio =
        printerConnection.enviar(motor.generar(trabajo))
}
