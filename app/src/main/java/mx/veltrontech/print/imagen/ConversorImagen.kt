package mx.veltrontech.print.imagen

object ConversorImagen {

    /** Compone el canal alfa sobre blanco y aplica luminancia. */
    fun aGrises(argb: IntArray): IntArray = IntArray(argb.size) { i ->
        val pixel = argb[i]
        val a = (pixel shr 24) and 0xFF
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        val rc = (r * a + 255 * (255 - a)) / 255
        val gc = (g * a + 255 * (255 - a)) / 255
        val bc = (b * a + 255 * (255 - a)) / 255
        (0.299 * rc + 0.587 * gc + 0.114 * bc).toInt().coerceIn(0, 255)
    }

    /** Negro si gris < nivel. */
    fun umbral(grises: IntArray, ancho: Int, alto: Int, nivel: Int): ImagenMonocromo {
        val negros = BooleanArray(grises.size) { i -> grises[i] < nivel }
        return ImagenMonocromo(ancho, alto, negros)
    }

    /** Difusión de error Floyd-Steinberg (7/16, 3/16, 5/16, 1/16), umbral 128. */
    fun tramado(grises: IntArray, ancho: Int, alto: Int): ImagenMonocromo {
        val buf = FloatArray(grises.size) { grises[it].toFloat() }
        val negros = BooleanArray(grises.size)
        for (y in 0 until alto) {
            for (x in 0 until ancho) {
                val idx = y * ancho + x
                val old = buf[idx]
                val new = if (old < 128f) 0f else 255f
                negros[idx] = new == 0f
                val err = old - new
                if (x + 1 < ancho)  buf[idx + 1]             += err * 7f / 16f
                if (y + 1 < alto) {
                    if (x > 0)       buf[idx + ancho - 1]     += err * 3f / 16f
                                     buf[idx + ancho]          += err * 5f / 16f
                    if (x + 1 < ancho) buf[idx + ancho + 1]   += err * 1f / 16f
                }
            }
        }
        return ImagenMonocromo(ancho, alto, negros)
    }

    /**
     * Escala la imagen al anchoDestino conservando proporción.
     * Si el alto resultante supera altoMaximo, escala por alto en su lugar.
     */
    fun calcularTamano(anchoOriginal: Int, altoOriginal: Int, anchoDestino: Int, altoMaximo: Int = 600): Pair<Int, Int> {
        val altoEscalado = (altoOriginal * anchoDestino.toDouble() / anchoOriginal).toInt()
        if (altoEscalado <= altoMaximo) return Pair(anchoDestino, altoEscalado)
        val anchoPorAlto = (anchoOriginal * altoMaximo.toDouble() / altoOriginal).toInt()
        return Pair(anchoPorAlto, altoMaximo)
    }
}
