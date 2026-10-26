package mx.veltrontech.print.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

class OrdenarDispositivosTest {

    @Test
    fun `orden alfabetico sin distinguir mayusculas`() {
        val lista = listOf(
            DispositivoEmparejado("Zebra BT", "AA:00"),
            DispositivoEmparejado("apple watch", "AA:01"),
            DispositivoEmparejado("Banana Speaker", "AA:02"),
        )
        assertEquals(
            listOf("apple watch", "Banana Speaker", "Zebra BT"),
            ordenarDispositivos(lista).map { it.nombre }
        )
    }

    @Test
    fun `nombre nulo se convierte en dispositivo sin nombre`() {
        assertEquals("Dispositivo sin nombre", normalizarNombre(null))
    }

    @Test
    fun `nombre vacio se convierte en dispositivo sin nombre`() {
        assertEquals("Dispositivo sin nombre", normalizarNombre(""))
    }

    @Test
    fun `nombre solo espacios se convierte en dispositivo sin nombre`() {
        assertEquals("Dispositivo sin nombre", normalizarNombre("   "))
    }

    @Test
    fun `nombre normal se conserva`() {
        assertEquals("POS-8390L", normalizarNombre("POS-8390L"))
    }
}
