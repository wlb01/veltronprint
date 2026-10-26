package mx.veltrontech.print.imagen

enum class ModoConversion { UMBRAL, TRAMADO }

enum class TamanoLogo(val ancho: Int) {
    CHICO(288),
    MEDIANO(432),
    COMPLETO(576),
}

data class OpcionesLogo(
    val modo: ModoConversion = ModoConversion.UMBRAL,
    val claridad: Int = 128,
    val tamano: TamanoLogo = TamanoLogo.MEDIANO,
) {
    companion object {
        val PREDETERMINADO = OpcionesLogo()
    }
}

data class LogoGuardado(val opciones: OpcionesLogo)

data class EdicionLogo(val opciones: OpcionesLogo, val vistaPrevia: ImagenMonocromo)
