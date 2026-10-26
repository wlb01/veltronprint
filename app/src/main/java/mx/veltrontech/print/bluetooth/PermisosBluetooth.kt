package mx.veltrontech.print.bluetooth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermisosBluetooth {
    // String constante para poder usarlo en pruebas JUnit sin el emulador
    const val BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

    // Función pura (sin Android): devuelve qué permisos hay que pedir según API.
    // API ≤ 30: los permisos BLUETOOTH y BLUETOOTH_ADMIN se otorgan al instalar → lista vacía.
    // API ≥ 31: hay que pedir BLUETOOTH_CONNECT en tiempo de ejecución.
    // Sin BLUETOOTH_SCAN: la app no busca dispositivos, solo usa los emparejados en Android.
    fun permisosRequeridos(sdkInt: Int): List<String> =
        if (sdkInt >= Build.VERSION_CODES.S) listOf(BLUETOOTH_CONNECT) else emptyList()

    fun tienePermisos(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true // En API ≤ 30 los permisos se dan al instalar
        }
}
