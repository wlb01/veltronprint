package mx.veltrontech.print.escpos

import java.io.ByteArrayOutputStream
import java.text.Normalizer

object Codificador {

    /**
     * Convierte [texto] a bytes para la [tabla] activa:
     * - Caracteres del mapa de la tabla → byte de la tabla.
     * - ASCII imprimible (0x20–0x7E) → byte directo.
     * - Suplementarios (código > 0xFFFF, incluye emoji) → se eliminan.
     * - Resto → NFD sin diacríticos si queda ASCII, de lo contrario '?'.
     */
    fun codificar(texto: String, tabla: TablaCaracteres): ByteArray {
        val out = ByteArrayOutputStream(texto.length)
        texto.codePoints().forEach { cp ->
            when {
                cp > 0xFFFF -> Unit  // suplementarios (emoji, etc.) → eliminar
                else -> {
                    val ch = cp.toChar()
                    val mapeado = tabla.mapa[ch]
                    when {
                        mapeado != null -> out.write(mapeado.toInt() and 0xFF)
                        cp in 0x20..0x7E -> out.write(cp)
                        else -> {
                            val nfd = Normalizer.normalize(ch.toString(), Normalizer.Form.NFD)
                            val base = nfd.firstOrNull { c -> c.code in 0x20..0x7E }
                            out.write(base?.code ?: '?'.code)
                        }
                    }
                }
            }
        }
        return out.toByteArray()
    }
}
