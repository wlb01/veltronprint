package mx.veltrontech.print.codigos

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import mx.veltrontech.print.contrato.TamanoQr
import mx.veltrontech.print.imagen.ImagenMonocromo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneradorQrTest {

    private val url = "https://veltrontech.mx/terminos"

    private fun ImagenMonocromo.decodificar(): String {
        val pixels = IntArray(ancho * alto) { i -> if (negros[i]) 0x000000 else 0xFFFFFF }
        val source = RGBLuminanceSource(ancho, alto, pixels)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        return QRCodeReader().decode(bitmap).text
    }

    @Test fun qrChicoIdaVuelta() {
        assertEquals(url, GeneradorQr.generarQr(url, TamanoQr.CHICO).decodificar())
    }

    @Test fun qrMedianoIdaVuelta() {
        assertEquals(url, GeneradorQr.generarQr(url, TamanoQr.MEDIANO).decodificar())
    }

    @Test fun qrGrandeIdaVuelta() {
        assertEquals(url, GeneradorQr.generarQr(url, TamanoQr.GRANDE).decodificar())
    }

    @Test fun qrEsCuadrado() {
        val imagen = GeneradorQr.generarQr(url, TamanoQr.MEDIANO)
        assertEquals(imagen.ancho, imagen.alto)
    }

    @Test fun qrTamanosCrecientes() {
        val chico = GeneradorQr.generarQr(url, TamanoQr.CHICO).ancho
        val mediano = GeneradorQr.generarQr(url, TamanoQr.MEDIANO).ancho
        val grande = GeneradorQr.generarQr(url, TamanoQr.GRANDE).ancho
        assertTrue("chico < mediano", chico < mediano)
        assertTrue("mediano < grande", mediano < grande)
        assertTrue("grande cabe en 576px", grande <= 576)
        // Los tamaños son proporcionales a 4:6:8
        assertEquals(mediano, chico * 6 / 4)
        assertEquals(grande, chico * 8 / 4)
    }

    @Test fun contenidoLargoCabe() {
        // 500 chars ASCII → QR grande pero debe caber en 576 puntos con n ≥ 2
        val imagen = GeneradorQr.generarQr("A".repeat(500), TamanoQr.CHICO)
        assertTrue(imagen.ancho > 0)
        assertTrue(imagen.ancho <= 576)
    }

    @Test fun qrConAcentosIdaVuelta() {
        val texto = "Garantía"
        assertEquals(texto, GeneradorQr.generarQr(texto, TamanoQr.MEDIANO).decodificar())
    }
}
