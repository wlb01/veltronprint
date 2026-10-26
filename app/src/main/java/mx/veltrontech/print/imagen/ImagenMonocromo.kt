package mx.veltrontech.print.imagen

/**
 * Imagen en blanco y negro pura, sin dependencias de Android.
 * negros[y * ancho + x] = true significa punto negro en (x, y).
 */
data class ImagenMonocromo(val ancho: Int, val alto: Int, val negros: BooleanArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImagenMonocromo) return false
        return ancho == other.ancho && alto == other.alto && negros.contentEquals(other.negros)
    }

    override fun hashCode(): Int {
        var result = ancho
        result = 31 * result + alto
        result = 31 * result + negros.contentHashCode()
        return result
    }
}
