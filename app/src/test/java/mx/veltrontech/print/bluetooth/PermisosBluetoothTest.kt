package mx.veltrontech.print.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

class PermisosBluetoothTest {

    @Test
    fun `api 26 retorna lista vacia`() {
        assertEquals(emptyList<String>(), PermisosBluetooth.permisosRequeridos(26))
    }

    @Test
    fun `api 30 retorna lista vacia`() {
        assertEquals(emptyList<String>(), PermisosBluetooth.permisosRequeridos(30))
    }

    @Test
    fun `api 31 retorna BLUETOOTH_CONNECT`() {
        assertEquals(
            listOf(PermisosBluetooth.BLUETOOTH_CONNECT),
            PermisosBluetooth.permisosRequeridos(31)
        )
    }

    @Test
    fun `api 36 retorna BLUETOOTH_CONNECT`() {
        assertEquals(
            listOf(PermisosBluetooth.BLUETOOTH_CONNECT),
            PermisosBluetooth.permisosRequeridos(36)
        )
    }
}
