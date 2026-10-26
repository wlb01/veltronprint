package mx.veltrontech.print.escpos

import org.junit.Assert.assertEquals
import org.junit.Test

class AjusteLineaTest {

    // --- cortarPorPalabra ---

    @Test fun textoCorto() {
        assertEquals(listOf("Hola"), AjusteLinea.cortarPorPalabra("Hola", 10))
    }

    @Test fun textoCabeExacto() {
        assertEquals(listOf("1234567890"), AjusteLinea.cortarPorPalabra("1234567890", 10))
    }

    @Test fun corteEntrepalabras() {
        val resultado = AjusteLinea.cortarPorPalabra("uno dos tres", 7)
        assertEquals(listOf("uno dos", "tres"), resultado)
    }

    @Test fun palabraMasLargaQueLinea() {
        val resultado = AjusteLinea.cortarPorPalabra("ABCDEFGH", 4)
        assertEquals(listOf("ABCD", "EFGH"), resultado)
    }

    @Test fun saltoDeLinea() {
        val resultado = AjusteLinea.cortarPorPalabra("uno\ndos", 10)
        assertEquals(listOf("uno", "dos"), resultado)
    }

    @Test fun espaciosSobrantes() {
        val resultado = AjusteLinea.cortarPorPalabra("  hola  mundo  ", 20)
        assertEquals(listOf("hola  mundo"), resultado)
    }

    @Test fun palabraExactaEnBorde() {
        val resultado = AjusteLinea.cortarPorPalabra("uno dos", 3)
        assertEquals(listOf("uno", "dos"), resultado)
    }

    // --- componerPar ---

    @Test fun parQueCapaEnUnaLinea() {
        val resultado = AjusteLinea.componerPar("Clave:", "Valor", 20)
        assertEquals(1, resultado.size)
        assertEquals("Clave:         Valor", resultado[0])
        assertEquals(20, resultado[0].length)
    }

    @Test fun parExacto() {
        // "AB" + espacio + "CD" = 5 chars, col = 5
        val resultado = AjusteLinea.componerPar("AB", "CD", 5)
        assertEquals(listOf("AB CD"), resultado)
        assertEquals(5, resultado[0].length)
    }

    @Test fun parConCorteDeIzquierda() {
        // derecha "XXXX" (4) + espacio = 5; ancho izq = 48 - 4 - 1 = 43
        val izq = "uno dos tres cuatro cinco"  // 25 chars, cabe en 43
        val resultado = AjusteLinea.componerPar(izq, "XXXX", 48)
        assertEquals(1, resultado.size)
        assertEquals(48, resultado[0].length)
        assert(resultado[0].startsWith("uno dos tres cuatro cinco"))
        assert(resultado[0].endsWith("XXXX"))
    }

    @Test fun parConIzquierdaQueSeParte() {
        // columnas = 10; derecha = "ABCD" (4); ancho izq = 5
        // izquierda "una palabra" no cabe en 5, se parte
        val resultado = AjusteLinea.componerPar("una palabra", "ABCD", 10)
        assert(resultado.size > 1)
        assertEquals(10, resultado.last().length)
        assert(resultado.last().endsWith("ABCD"))
    }

    @Test fun parConDerechaDemasiadoLarga() {
        // derecha sola no cabe con un espacio (len 9, col 9 → 9+1 > 9)
        val resultado = AjusteLinea.componerPar("Izq", "123456789", 9)
        assert(resultado.size >= 2)
        assertEquals(9, resultado.last().length)
        assertEquals("123456789", resultado.last().trim())
    }
}
