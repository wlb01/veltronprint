package mx.veltrontech.print.codigos

import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import mx.veltrontech.print.contrato.TamanoQr
import mx.veltrontech.print.imagen.ImagenMonocromo

object GeneradorQr {
    private const val MARGEN = 4

    fun generarQr(contenido: String, tamano: TamanoQr): ImagenMonocromo {
        val hints = HashMap<EncodeHintType, Any>()
        if (!contenido.all { it.code < 128 }) {
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        }

        val matrix = try {
            Encoder.encode(contenido, ErrorCorrectionLevel.M, hints).matrix
        } catch (e: WriterException) {
            throw CodigoInvalidoException("No se pudo generar el QR", e)
        }

        val modulos = matrix.width
        val totalModulos = modulos + MARGEN * 2

        val solicitado = when (tamano) {
            TamanoQr.CHICO -> 4
            TamanoQr.MEDIANO -> 6
            TamanoQr.GRANDE -> 8
        }
        val n = if (totalModulos * solicitado > 576) 576 / totalModulos else solicitado

        if (n < 2) throw CodigoInvalidoException("El contenido del QR es demasiado largo para imprimir")

        val lado = totalModulos * n
        val negros = BooleanArray(lado * lado)

        for (my in 0 until modulos) {
            for (mx in 0 until modulos) {
                if (matrix.get(mx, my).toInt() == 1) {
                    val px = (MARGEN + mx) * n
                    val py = (MARGEN + my) * n
                    for (dy in 0 until n) {
                        for (dx in 0 until n) {
                            negros[(py + dy) * lado + (px + dx)] = true
                        }
                    }
                }
            }
        }

        return ImagenMonocromo(lado, lado, negros)
    }
}
