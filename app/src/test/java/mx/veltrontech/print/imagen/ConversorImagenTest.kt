package mx.veltrontech.print.imagen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversorImagenTest {

    // Negro puro (ARGB sin transparencia) → gris 0 → negro con cualquier umbral razonable
    @Test fun negroPuroEsNegro() {
        val argb = intArrayOf(0xFF000000.toInt())
        val grises = ConversorImagen.aGrises(argb)
        assertEquals(0, grises[0])
        assertTrue(ConversorImagen.umbral(grises, 1, 1, 128).negros[0])
    }

    // Blanco puro → gris 255 → blanco
    @Test fun blancoPuroEsBlanco() {
        val argb = intArrayOf(0xFFFFFFFF.toInt())
        val grises = ConversorImagen.aGrises(argb)
        assertEquals(255, grises[0])
        assertFalse(ConversorImagen.umbral(grises, 1, 1, 128).negros[0])
    }

    // Transparente (alfa = 0) → compone sobre blanco → gris 255 → blanco
    @Test fun transparenteEsBlanco() {
        val argb = intArrayOf(0x00000000)
        val grises = ConversorImagen.aGrises(argb)
        assertEquals(255, grises[0])
    }

    // Gris 100 con nivel 128 → negro (100 < 128)
    @Test fun gris100ConNivel128EsNegro() {
        val grises = intArrayOf(100)
        assertTrue(ConversorImagen.umbral(grises, 1, 1, 128).negros[0])
    }

    // Gris 100 con nivel 90 → blanco (100 >= 90)
    @Test fun gris100ConNivel90EsBlanco() {
        val grises = intArrayOf(100)
        assertFalse(ConversorImagen.umbral(grises, 1, 1, 90).negros[0])
    }

    // Tramado de un bloque 50% gris: aproximadamente la mitad de puntos son negros
    @Test fun tramadoGris50PorCientoMitadNegros() {
        val ancho = 20
        val alto = 20
        val grises = IntArray(ancho * alto) { 128 }
        val imagen = ConversorImagen.tramado(grises, ancho, alto)
        val negros = imagen.negros.count { it }
        val total = ancho * alto
        // Con Floyd-Steinberg, en un gris uniforme de 128/255 ≈ 50.2% debería ser ~mitad
        assertTrue("Esperado ~200, obtenido $negros", negros in (total / 3)..(total * 2 / 3))
    }

    // calcularTamano: logo Veltron 611×290 a 432 px de ancho → (432, 205)
    @Test fun calcularTamanoLogoVeltron() {
        assertEquals(Pair(432, 205), ConversorImagen.calcularTamano(611, 290, 432))
    }

    // calcularTamano: imagen muy alta se limita a 600 de alto
    @Test fun calcularTamanoLimitaAlto() {
        // 100×2000 a ancho 576 → 576×11520 → excede 600 → escala por alto
        val (ancho, alto) = ConversorImagen.calcularTamano(100, 2000, 576)
        assertEquals(600, alto)
        assertTrue(ancho <= 576)
    }
}
