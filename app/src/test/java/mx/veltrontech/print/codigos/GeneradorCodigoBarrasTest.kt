package mx.veltrontech.print.codigos

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.oned.Code128Reader
import com.google.zxing.oned.Code128Writer
import mx.veltrontech.print.imagen.ImagenMonocromo
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneradorCodigoBarrasTest {

    private fun ImagenMonocromo.decodificar(): String {
        val pixels = IntArray(ancho * alto) { i -> if (negros[i]) 0x000000 else 0xFFFFFF }
        val source = RGBLuminanceSource(ancho, alto, pixels)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        return Code128Reader().decode(bitmap).text
    }

    @Test fun idaVueltaFolio() {
        val contenido = "MZT-OCT26-0042"
        assertEquals(contenido, GeneradorCodigoBarras.generarCode128(contenido).decodificar())
    }

    @Test fun anchoYAlto() {
        val contenido = "MZT-OCT26-0042"
        val hints = mapOf(EncodeHintType.MARGIN to 0)
        val barras = Code128Writer().encode(contenido, BarcodeFormat.CODE_128, 0, 0, hints).width
        val imagen = GeneradorCodigoBarras.generarCode128(contenido)
        assertEquals((barras + 20) * 2, imagen.ancho)
        assertEquals(GeneradorCodigoBarras.ALTO, imagen.alto)
    }

    @Test(expected = CodigoInvalidoException::class)
    fun contenidoDe41CaracteresLanzaExcepcion() {
        GeneradorCodigoBarras.generarCode128("A".repeat(41))
    }

    @Test(expected = CodigoInvalidoException::class)
    fun contenidoConAcentoLanzaExcepcion() {
        GeneradorCodigoBarras.generarCode128("Garantía")
    }

    @Test(expected = CodigoInvalidoException::class)
    fun contenidoVacioLanzaExcepcion() {
        GeneradorCodigoBarras.generarCode128("")
    }
}
