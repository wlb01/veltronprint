package mx.veltrontech.print.contrato

data class Trabajo(
    val v: Int,
    val tipo: String,
    val folio: String? = null,
    val copias: Int = 1,
    val bloques: List<Bloque>,
)
