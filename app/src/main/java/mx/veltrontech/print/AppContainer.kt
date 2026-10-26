package mx.veltrontech.print

import android.content.Context
import mx.veltrontech.print.bluetooth.BluetoothRepository
import mx.veltrontech.print.bluetooth.PrinterConnection
import mx.veltrontech.print.datos.PreferenciasRepository
import mx.veltrontech.print.escpos.ConfigImpresion
import mx.veltrontech.print.escpos.MotorEscPos
import mx.veltrontech.print.escpos.ServicioImpresion
import mx.veltrontech.print.imagen.LogoRepository

// AppContainer: inyección de dependencias manual.
// Se crea una sola vez en VeltronPrintApp y se comparte con todas las pantallas.
// Equivale a un contexto global en React, pero sin magia: solo son objetos.
class AppContainer(context: Context) {
    val preferenciasRepository = PreferenciasRepository(context)
    val bluetoothRepository = BluetoothRepository(context)
    val printerConnection = PrinterConnection(context, preferenciasRepository, bluetoothRepository)
    val motorEscPos = MotorEscPos(ConfigImpresion.TABLA_PREDETERMINADA, ConfigImpresion.CORTE_PREDETERMINADO)
    val logoRepository = LogoRepository(context)
    val servicioImpresion = ServicioImpresion(motorEscPos, printerConnection, logoRepository)
}
