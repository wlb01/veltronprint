package mx.veltrontech.print.escpos

import mx.veltrontech.print.contrato.ModoCorte

object ComandosEscPos {

    /** Reinicia la impresora a sus valores de fábrica. */
    val INICIALIZAR = byteArrayOf(0x1B, 0x40)

    /** Selecciona la tabla de caracteres: CP850 = 2, Windows-1252 = 16. */
    fun tablaCaracteres(n: Int): ByteArray = byteArrayOf(0x1B, 0x74, n.toByte())

    /** Alineación: 0 izquierda, 1 centro, 2 derecha. */
    fun alinear(a: Int): ByteArray = byteArrayOf(0x1B, 0x61, a.toByte())

    /** Negrita: true activa (1), false desactiva (0). */
    fun negrita(on: Boolean): ByteArray = byteArrayOf(0x1B, 0x45, if (on) 0x01 else 0x00)

    /** Subrayado: true activa (1), false desactiva (0). */
    fun subrayado(on: Boolean): ByteArray = byteArrayOf(0x1B, 0x2D, if (on) 0x01 else 0x00)

    /**
     * Tamaño de carácter: multiplica alto y ancho.
     * n = ((t-1) shl 4) or (t-1) → t=1: 0x00, t=2: 0x11, t=3: 0x22.
     */
    fun tamano(t: Int): ByteArray =
        byteArrayOf(0x1D, 0x21, (((t - 1) shl 4) or (t - 1)).toByte())

    /** Fuente: 0 normal (A, 12×24 puntos), 1 compacta (B, 9×17 puntos). */
    fun fuente(f: Int): ByteArray = byteArrayOf(0x1B, 0x4D, f.toByte())

    /** Avanza una línea. */
    val SALTO_LINEA = byteArrayOf(0x0A)

    /** Avanza n líneas en blanco. */
    fun avanzarLineas(n: Int): ByteArray = byteArrayOf(0x1B, 0x64, n.toByte())

    /**
     * Avanza el papel hasta la cuchilla (+ 3 puntos) y corta.
     * Total: GS V A 03 — Parcial: GS V B 03.
     */
    fun cortar(modo: ModoCorte): ByteArray = when (modo) {
        ModoCorte.TOTAL -> byteArrayOf(0x1D, 0x56, 0x41, 0x03)
        ModoCorte.PARCIAL -> byteArrayOf(0x1D, 0x56, 0x42, 0x03)
    }

    /** Pulso de apertura del cajón de dinero: pin 2, 25 ms on, 250 ms off. */
    val ABRIR_CAJON = byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte())

    /**
     * GS v 0 — imprime una imagen de puntos.
     * Cada byte son 8 puntos horizontales, el bit más alto es el de la izquierda, 1 = negro.
     * anchoBytes: ancho en bytes (puntos / 8); alto: filas de puntos.
     */
    fun imagenRaster(anchoBytes: Int, alto: Int, datos: ByteArray): ByteArray {
        val header = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00,
            (anchoBytes and 0xFF).toByte(),
            ((anchoBytes shr 8) and 0xFF).toByte(),
            (alto and 0xFF).toByte(),
            ((alto shr 8) and 0xFF).toByte(),
        )
        return header + datos
    }
}
