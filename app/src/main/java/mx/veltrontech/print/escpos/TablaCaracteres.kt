package mx.veltrontech.print.escpos

/**
 * Tablas de caracteres soportadas.
 * Se usa un mapa propio (no Charset.forName) porque no todas las versiones
 * de Android incluyen "IBM850" y los mapas son testables con JUnit.
 */
enum class TablaCaracteres(val numero: Int, val mapa: Map<Char, Byte>) {
    CP850(
        2,
        mapOf(
            'á' to 0xA0.toByte(), 'é' to 0x82.toByte(), 'í' to 0xA1.toByte(),
            'ó' to 0xA2.toByte(), 'ú' to 0xA3.toByte(),
            'Á' to 0xB5.toByte(), 'É' to 0x90.toByte(), 'Í' to 0xD6.toByte(),
            'Ó' to 0xE0.toByte(), 'Ú' to 0xE9.toByte(),
            'ñ' to 0xA4.toByte(), 'Ñ' to 0xA5.toByte(),
            'ü' to 0x81.toByte(), 'Ü' to 0x9A.toByte(),
            '¿' to 0xA8.toByte(), '¡' to 0xAD.toByte(),
            '°' to 0xF8.toByte(),
        ),
    ),
    WIN1252(
        16,
        mapOf(
            'á' to 0xE1.toByte(), 'é' to 0xE9.toByte(), 'í' to 0xED.toByte(),
            'ó' to 0xF3.toByte(), 'ú' to 0xFA.toByte(),
            'Á' to 0xC1.toByte(), 'É' to 0xC9.toByte(), 'Í' to 0xCD.toByte(),
            'Ó' to 0xD3.toByte(), 'Ú' to 0xDA.toByte(),
            'ñ' to 0xF1.toByte(), 'Ñ' to 0xD1.toByte(),
            'ü' to 0xFC.toByte(), 'Ü' to 0xDC.toByte(),
            '¿' to 0xBF.toByte(), '¡' to 0xA1.toByte(),
            '°' to 0xB0.toByte(),
        ),
    ),
}
