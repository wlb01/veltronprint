package mx.veltrontech.print.escpos

object AjusteLinea {

    /**
     * Divide [texto] en líneas de máximo [columnas] caracteres, respetando '\n'.
     * Corta por palabra; una palabra más larga que [columnas] se parte a la mitad.
     * Elimina espacios al inicio y al final de cada línea resultante.
     */
    fun cortarPorPalabra(texto: String, columnas: Int): List<String> {
        val resultado = mutableListOf<String>()
        for (linea in texto.split('\n')) {
            if (linea.length <= columnas) {
                resultado.add(linea.trim())
                continue
            }
            val lineaActual = StringBuilder()
            for (palabra in linea.split(' ')) {
                if (palabra.isEmpty()) continue
                if (lineaActual.isEmpty()) {
                    agregarPalabra(palabra, columnas, resultado, lineaActual)
                } else if (lineaActual.length + 1 + palabra.length <= columnas) {
                    lineaActual.append(' ').append(palabra)
                } else {
                    resultado.add(lineaActual.toString().trim())
                    lineaActual.clear()
                    agregarPalabra(palabra, columnas, resultado, lineaActual)
                }
            }
            if (lineaActual.isNotEmpty()) resultado.add(lineaActual.toString().trim())
        }
        return resultado
    }

    /**
     * Compone una línea "par": [izquierda] pegada al margen izquierdo y
     * [derecha] pegada al margen derecho, rellenando con espacios.
     * Si no caben, [izquierda] se corta por palabras y [derecha] va en la última línea.
     * Si [derecha] sola no cabe con un espacio, [izquierda] va en su(s) propia(s) línea(s).
     */
    fun componerPar(izquierda: String, derecha: String, columnas: Int): List<String> {
        if (derecha.length + 1 > columnas) {
            val lineas = cortarPorPalabra(izquierda, columnas).toMutableList()
            lineas.add(derecha.padStart(columnas))
            return lineas
        }
        if (izquierda.length + 1 + derecha.length <= columnas) {
            val espacios = columnas - izquierda.length - derecha.length
            return listOf(izquierda + " ".repeat(espacios) + derecha)
        }
        val anchoIzq = columnas - derecha.length - 1
        val lineasIzq = cortarPorPalabra(izquierda, anchoIzq)
        val resultado = mutableListOf<String>()
        for (i in 0 until lineasIzq.size - 1) resultado.add(lineasIzq[i])
        val ultimaIzq = lineasIzq.lastOrNull() ?: ""
        val espacios = columnas - ultimaIzq.length - derecha.length
        resultado.add(ultimaIzq + " ".repeat(espacios) + derecha)
        return resultado
    }

    private fun agregarPalabra(
        palabra: String,
        columnas: Int,
        resultado: MutableList<String>,
        lineaActual: StringBuilder,
    ) {
        var restante = palabra
        while (restante.length > columnas) {
            resultado.add(restante.substring(0, columnas))
            restante = restante.substring(columnas)
        }
        lineaActual.append(restante)
    }
}
