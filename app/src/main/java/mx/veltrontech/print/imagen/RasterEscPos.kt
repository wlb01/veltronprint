package mx.veltrontech.print.imagen

import mx.veltrontech.print.contrato.Alineacion
import mx.veltrontech.print.escpos.ComandosEscPos
import java.io.ByteArrayOutputStream

object RasterEscPos {

    private const val ANCHO_PAPEL = 576
    private const val ANCHO_BYTES_PAPEL = ANCHO_PAPEL / 8  // 72
    private const val MAX_FILAS_FRANJA = 128

    /**
     * Coloca la imagen en un lienzo de 576 puntos con relleno blanco según la alineación.
     * Se hace en datos, no con ESC a, para máxima compatibilidad con impresoras genéricas.
     */
    fun posicionar(imagen: ImagenMonocromo, alineacion: Alineacion): ImagenMonocromo {
        if (imagen.ancho == ANCHO_PAPEL) return imagen
        val offsetX = when (alineacion) {
            Alineacion.IZQUIERDA -> 0
            Alineacion.CENTRO    -> (ANCHO_PAPEL - imagen.ancho) / 2
            Alineacion.DERECHA   -> ANCHO_PAPEL - imagen.ancho
        }
        val negros = BooleanArray(ANCHO_PAPEL * imagen.alto)
        for (y in 0 until imagen.alto) {
            for (x in 0 until imagen.ancho) {
                if (imagen.negros[y * imagen.ancho + x]) {
                    negros[y * ANCHO_PAPEL + offsetX + x] = true
                }
            }
        }
        return ImagenMonocromo(ANCHO_PAPEL, imagen.alto, negros)
    }

    /** 8 puntos por byte, bit más alto = izquierda, 1 = negro. */
    fun empaquetar(imagen: ImagenMonocromo): ByteArray {
        val anchoBytesLinea = (imagen.ancho + 7) / 8
        val out = ByteArray(anchoBytesLinea * imagen.alto)
        for (y in 0 until imagen.alto) {
            for (byteIdx in 0 until anchoBytesLinea) {
                var byte = 0
                for (bit in 0 until 8) {
                    val x = byteIdx * 8 + bit
                    if (x < imagen.ancho && imagen.negros[y * imagen.ancho + x]) {
                        byte = byte or (0x80 ushr bit)
                    }
                }
                out[y * anchoBytesLinea + byteIdx] = byte.toByte()
            }
        }
        return out
    }

    /** Posiciona, empaqueta y divide en franjas de 128 filas, cada una con su comando GS v 0. */
    fun generar(imagen: ImagenMonocromo, alineacion: Alineacion): ByteArray {
        val posicionada = posicionar(imagen, alineacion)
        val datos = empaquetar(posicionada)
        val out = ByteArrayOutputStream()
        var fila = 0
        while (fila < posicionada.alto) {
            val filasEnFranja = minOf(MAX_FILAS_FRANJA, posicionada.alto - fila)
            val inicio = fila * ANCHO_BYTES_PAPEL
            val franja = datos.sliceArray(inicio until inicio + filasEnFranja * ANCHO_BYTES_PAPEL)
            out.write(ComandosEscPos.imagenRaster(ANCHO_BYTES_PAPEL, filasEnFranja, franja))
            fila += filasEnFranja
        }
        return out.toByteArray()
    }
}
