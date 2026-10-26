package mx.veltrontech.print.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context

// Funciones puras (sin Context) para poder probarlas con JUnit sin emulador

fun normalizarNombre(nombre: String?): String =
    nombre?.takeIf { it.isNotBlank() } ?: "Dispositivo sin nombre"

fun ordenarDispositivos(dispositivos: List<DispositivoEmparejado>): List<DispositivoEmparejado> =
    dispositivos.sortedBy { it.nombre.lowercase() }

// BluetoothRepository: acceso a los dispositivos emparejados de Android.
// No busca dispositivos; solo lee la lista que ya emparejó el usuario en los ajustes de Android.
class BluetoothRepository(context: Context) {

    private val btManager = context.getSystemService(BluetoothManager::class.java)

    fun disponible(): Boolean = btManager.adapter != null

    fun encendido(): Boolean = btManager.adapter?.isEnabled == true

    // Requiere BLUETOOTH_CONNECT en API 31+; el llamador verifica permisos antes.
    @SuppressLint("MissingPermission")
    fun dispositivosEmparejados(): List<DispositivoEmparejado> {
        val adapter = btManager.adapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        val dispositivos = adapter.bondedDevices?.map { device ->
            DispositivoEmparejado(normalizarNombre(device.name), device.address)
        } ?: emptyList()
        return ordenarDispositivos(dispositivos)
    }

    @SuppressLint("MissingPermission")
    fun estaEmparejado(mac: String): Boolean {
        val adapter = btManager.adapter ?: return false
        return adapter.bondedDevices?.any { it.address == mac } == true
    }
}
