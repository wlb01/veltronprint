package mx.veltrontech.print.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mx.veltrontech.print.datos.PreferenciasRepository
import java.io.IOException
import java.util.UUID

// PrinterConnection: único punto de acceso a la impresora en toda la app.
// Garantiza una sola conexión a la vez gracias al Mutex.
//
// Conceptos Kotlin usados:
//   suspend fun — función que puede pausarse sin bloquear el hilo principal.
//   Dispatchers.IO — grupo de hilos para operaciones lentas (Bluetooth, disco).
//   Mutex — asegura que solo una corrutina acceda al socket al mismo tiempo.
//   coroutineScope — crea un alcance donde los hijos se cancelan si el padre falla.
class PrinterConnection(
    private val context: Context,
    private val preferenciasRepository: PreferenciasRepository,
    private val bluetoothRepository: BluetoothRepository,
) {
    private val mutex = Mutex()

    private companion object {
        val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val CMD_INIT = byteArrayOf(0x1B, 0x40.toByte()) // ESC @ — inicializa sin imprimir
        const val TIMEOUT_MS = 10_000L
        const val DELAY_CIERRE_MS = 300L // esperar que el búfer salga antes de cerrar
    }

    // Prueba la conexión enviando solo la inicialización (no imprime ni avanza papel)
    suspend fun probar(): ResultadoEnvio = enviar(CMD_INIT)

    @SuppressLint("MissingPermission")
    suspend fun enviar(bytes: ByteArray): ResultadoEnvio = withContext(Dispatchers.IO) {
        val btManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = btManager.adapter
            ?: return@withContext ResultadoEnvio.SinBluetooth

        if (!PermisosBluetooth.tienePermisos(context))
            return@withContext ResultadoEnvio.SinPermiso

        if (!adapter.isEnabled)
            return@withContext ResultadoEnvio.BluetoothApagado

        val impresora = preferenciasRepository.impresoraGuardadaUnaVez()
            ?: return@withContext ResultadoEnvio.SinImpresora

        if (!bluetoothRepository.estaEmparejado(impresora.mac))
            return@withContext ResultadoEnvio.NoEmparejada

        mutex.withLock {
            val device = adapter.getRemoteDevice(impresora.mac)
            var socket: BluetoothSocket? = null
            var resultado: ResultadoEnvio = ResultadoEnvio.NoResponde
            try {
                socket = conectarConTimeout(device)
                if (socket != null) {
                    try {
                        socket.outputStream.write(bytes)
                        socket.outputStream.flush()
                        delay(DELAY_CIERRE_MS) // algunas impresoras pierden los últimos bytes si se cierra antes
                        resultado = ResultadoEnvio.Exito
                    } catch (e: IOException) {
                        resultado = ResultadoEnvio.ConexionPerdida
                    }
                }
            } finally {
                runCatching { socket?.close() } // siempre cerrar, haya error o no
            }
            resultado
        }
    }

    // Intenta conectar con UUID SPP estándar; si falla, usa canal 1 por reflexión
    // (muchas impresoras genéricas solo aceptan esta forma alternativa).
    private suspend fun conectarConTimeout(device: BluetoothDevice): BluetoothSocket? =
        withContext(Dispatchers.IO) {
            val socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            if (conectarSocket(socket)) return@withContext socket

            runCatching { socket.close() }

            // Fallback por reflexión: createRfcommSocket(1)
            val socketAlt = try {
                @Suppress("DiscouragedPrivateApi")
                device.javaClass.getMethod("createRfcommSocket", Int::class.java)
                    .invoke(device, 1) as BluetoothSocket
            } catch (e: Exception) {
                return@withContext null
            }

            if (conectarSocket(socketAlt)) socketAlt
            else { runCatching { socketAlt.close() }; null }
        }

    // connect() es bloqueante y no responde a cancelación de corrutinas.
    // La corrutina vigilante cierra el socket al vencer los 10 s, lo que
    // hace que connect() lance IOException y podamos devolver NoResponde.
    private suspend fun conectarSocket(socket: BluetoothSocket): Boolean =
        withContext(Dispatchers.IO) {
            var conectado = false
            try {
                coroutineScope {
                    val vigilante = launch {
                        delay(TIMEOUT_MS)
                        runCatching { socket.close() }
                    }
                    socket.connect() // bloquea el hilo IO hasta que conecta o falla
                    vigilante.cancel()
                    conectado = true
                }
            } catch (e: IOException) {
                /* timeout o fallo de conexión */
            }
            conectado
        }
}
