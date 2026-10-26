package mx.veltrontech.print.imagen

import mx.veltrontech.print.contrato.Alineacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RasterEscPosTest {

    // empaquetar: fila [1,0,0,0,0,0,0,1] → 0x81 (10000001)
    @Test fun empaquetar1Fila() {
        val negros = booleanArrayOf(true, false, false, false, false, false, false, true)
        val imagen = ImagenMonocromo(8, 1, negros)
        val bytes = RasterEscPos.empaquetar(imagen)
        assertEquals(1, bytes.size)
        assertEquals(0x81.toByte(), bytes[0])
    }

    // empaquetar: ancho 9 → 2 bytes por fila; todo negro → 0xFF, 0x80
    @Test fun empaquetar9Pixels() {
        val negros = BooleanArray(9) { true }
        val imagen = ImagenMonocromo(9, 1, negros)
        val bytes = RasterEscPos.empaquetar(imagen)
        assertEquals(2, bytes.size)
        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0x80.toByte(), bytes[1])
    }

    // posicionar: imagen de 288 puntos centrada en 576 → 144 blancos a cada lado
    @Test fun posicionarCentrado() {
        val ancho = 288
        val negros = BooleanArray(ancho) { true }  // 1 fila toda negra
        val imagen = ImagenMonocromo(ancho, 1, negros)
        val posicionada = RasterEscPos.posicionar(imagen, Alineacion.CENTRO)
        assertEquals(576, posicionada.ancho)
        // Primeros 144 píxeles blancos
        for (i in 0 until 144) assertFalse(posicionada.negros[i])
        // Siguiente 288 negros
        for (i in 144 until 432) assertTrue(posicionada.negros[i])
        // Últimos 144 blancos
        for (i in 432 until 576) assertFalse(posicionada.negros[i])
    }

    // generar: imagen de 300 filas → 3 comandos GS v 0 (128 + 128 + 44)
    @Test fun generar300FilasCrea3Comandos() {
        val negros = BooleanArray(576 * 300) { false }
        val imagen = ImagenMonocromo(576, 300, negros)
        val bytes = RasterEscPos.generar(imagen, Alineacion.IZQUIERDA)
        // Cabecera GS v 0 = 8 bytes. 3 comandos → al menos 3 cabeceras.
        // El patrón 0x1D 0x76 0x30 0x00 aparece 3 veces.
        var count = 0
        var i = 0
        while (i < bytes.size - 3) {
            if (bytes[i] == 0x1D.toByte() && bytes[i + 1] == 0x76.toByte()
                && bytes[i + 2] == 0x30.toByte() && bytes[i + 3] == 0x00.toByte()
            ) {
                count++
                i += 4
            } else i++
        }
        assertEquals(3, count)
    }

    // encabezado GS v 0 para franja 576×128:
    // anchoBytes=72 (0x48), alto=128 (0x80)
    // → 1D 76 30 00 48 00 80 00
    @Test fun cabecera576x128() {
        val negros = BooleanArray(576 * 128) { false }
        val imagen = ImagenMonocromo(576, 128, negros)
        val bytes = RasterEscPos.generar(imagen, Alineacion.IZQUIERDA)
        assertEquals(0x1D.toByte(), bytes[0])
        assertEquals(0x76.toByte(), bytes[1])
        assertEquals(0x30.toByte(), bytes[2])
        assertEquals(0x00.toByte(), bytes[3])
        assertEquals(0x48.toByte(), bytes[4])  // anchoBytes = 72 = 0x48
        assertEquals(0x00.toByte(), bytes[5])
        assertEquals(0x80.toByte(), bytes[6])  // alto = 128 = 0x80
        assertEquals(0x00.toByte(), bytes[7])
    }
}
