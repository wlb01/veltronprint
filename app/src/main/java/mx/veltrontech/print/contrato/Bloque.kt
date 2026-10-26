package mx.veltrontech.print.contrato

enum class Alineacion { IZQUIERDA, CENTRO, DERECHA }
enum class Fuente { NORMAL, COMPACTA }
enum class ModoCorte { TOTAL, PARCIAL }
enum class TamanoQr { CHICO, MEDIANO, GRANDE }

sealed interface Bloque {
    data class Texto(
        val valor: String,
        val alinear: Alineacion = Alineacion.IZQUIERDA,
        val negrita: Boolean = false,
        val subrayado: Boolean = false,
        val tamano: Int = 1,
        val fuente: Fuente = Fuente.NORMAL,
    ) : Bloque

    data class Par(
        val izquierda: String,
        val derecha: String,
        val negrita: Boolean = false,
        val fuente: Fuente = Fuente.NORMAL,
    ) : Bloque

    data class Separador(val caracter: Char = '-') : Bloque

    data class Espacio(val lineas: Int = 1) : Bloque

    data class Logo(val alinear: Alineacion = Alineacion.CENTRO) : Bloque

    data class Qr(
        val valor: String,
        val tamano: TamanoQr = TamanoQr.MEDIANO,
        val alinear: Alineacion = Alineacion.CENTRO,
    ) : Bloque

    data class CodigoBarras(
        val valor: String,
        val formato: String = "CODE128",
        val mostrarTexto: Boolean = true,
        val alinear: Alineacion = Alineacion.CENTRO,
    ) : Bloque

    data class Corte(val modo: ModoCorte? = null) : Bloque

    data object Cajon : Bloque
}
