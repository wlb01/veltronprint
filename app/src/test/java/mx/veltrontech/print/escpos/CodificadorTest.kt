package mx.veltrontech.print.escpos

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CodificadorTest {

    // --- CP850 ---

    @Test fun acentoMinusculasCp850() {
        val bytes = Codificador.codificar("áéíóú", TablaCaracteres.CP850)
        assertArrayEquals(
            byteArrayOf(0xA0.toByte(), 0x82.toByte(), 0xA1.toByte(), 0xA2.toByte(), 0xA3.toByte()),
            bytes,
        )
    }

    @Test fun acentoMayusculasCp850() {
        val bytes = Codificador.codificar("ÁÉÍÓÚ", TablaCaracteres.CP850)
        assertArrayEquals(
            byteArrayOf(0xB5.toByte(), 0x90.toByte(), 0xD6.toByte(), 0xE0.toByte(), 0xE9.toByte()),
            bytes,
        )
    }

    @Test fun ennesCp850() {
        val bytes = Codificador.codificar("ñÑ", TablaCaracteres.CP850)
        assertArrayEquals(byteArrayOf(0xA4.toByte(), 0xA5.toByte()), bytes)
    }

    @Test fun gradosCp850() {
        val bytes = Codificador.codificar("°", TablaCaracteres.CP850)
        assertArrayEquals(byteArrayOf(0xF8.toByte()), bytes)
    }

    // --- WIN1252 ---

    @Test fun acentoMinusculasWin1252() {
        val bytes = Codificador.codificar("áéíóú", TablaCaracteres.WIN1252)
        assertArrayEquals(
            byteArrayOf(0xE1.toByte(), 0xE9.toByte(), 0xED.toByte(), 0xF3.toByte(), 0xFA.toByte()),
            bytes,
        )
    }

    @Test fun ennesWin1252() {
        val bytes = Codificador.codificar("ñÑ", TablaCaracteres.WIN1252)
        assertArrayEquals(byteArrayOf(0xF1.toByte(), 0xD1.toByte()), bytes)
    }

    @Test fun gradosWin1252() {
        val bytes = Codificador.codificar("°", TablaCaracteres.WIN1252)
        assertArrayEquals(byteArrayOf(0xB0.toByte()), bytes)
    }

    // --- Casos generales ---

    @Test fun asciiIntacto() {
        val bytes = Codificador.codificar("Hello", TablaCaracteres.CP850)
        assertArrayEquals("Hello".map { it.code.toByte() }.toByteArray(), bytes)
    }

    @Test fun accentoSinMapaNfd() {
        // à no está en el mapa; NFD → a + combinando → queda 'a'
        val bytes = Codificador.codificar("à", TablaCaracteres.CP850)
        assertArrayEquals(byteArrayOf('a'.code.toByte()), bytes)
    }

    @Test fun euroSignSeVuelveInterrogacion() {
        // € no tiene equivalente ASCII ni está en el mapa
        val bytes = Codificador.codificar("€", TablaCaracteres.CP850)
        assertArrayEquals(byteArrayOf('?'.code.toByte()), bytes)
    }

    @Test fun emojiEliminado() {
        // 😀 está en plano suplementario → se elimina
        val bytes = Codificador.codificar("A😀B", TablaCaracteres.CP850)
        assertArrayEquals(byteArrayOf('A'.code.toByte(), 'B'.code.toByte()), bytes)
    }

    @Test fun cadenaVacia() {
        assertEquals(0, Codificador.codificar("", TablaCaracteres.CP850).size)
    }
}
