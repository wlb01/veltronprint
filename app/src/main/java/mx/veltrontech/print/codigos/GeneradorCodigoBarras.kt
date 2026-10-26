package mx.veltrontech.print.codigos

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.oned.Code128Writer
import mx.veltrontech.print.imagen.ImagenMonocromo

object GeneradorCodigoBarras {
    private const val MARGEN = 10
    const val ALTO = 80

    fun generarCode128(contenido: String): ImagenMonocromo {
        if (contenido.isEmpty() || contenido.length > 40)
            throw CodigoInvalidoException("El código de barras debe tener entre 1 y 40 caracteres")
        if (!contenido.all { it.code in 32..126 })
            throw CodigoInvalidoException("El código de barras solo acepta caracteres ASCII imprimibles (32–126)")

        // MARGIN=0 para obtener solo el patrón puro; nosotros añadimos MARGEN manualmente
        val hints = mapOf(EncodeHintType.MARGIN to 0)
        val bitMatrix = Code128Writer().encode(contenido, BarcodeFormat.CODE_128, 0, 0, hints)
        val barras = BooleanArray(bitMatrix.width) { x -> bitMatrix.get(x, 0) }

        val totalUnidades = barras.size + MARGEN * 2
        val anchoModulo = if (totalUnidades * 2 > 576) 1 else 2
        val ancho = totalUnidades * anchoModulo
        val negros = BooleanArray(ancho * ALTO)

        val offsetX = MARGEN * anchoModulo
        for (y in 0 until ALTO) {
            var x = offsetX
            for (barra in barras) {
                repeat(anchoModulo) { negros[y * ancho + x++] = barra }
            }
        }

        return ImagenMonocromo(ancho, ALTO, negros)
    }
}
