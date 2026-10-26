package mx.veltrontech.print.escpos

import mx.veltrontech.print.contrato.ModoCorte
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ComandosEscPosTest {

    @Test fun inicializar() =
        assertArrayEquals(byteArrayOf(0x1B, 0x40), ComandosEscPos.INICIALIZAR)

    @Test fun tablaCaracteresCp850() =
        assertArrayEquals(byteArrayOf(0x1B, 0x74, 0x02), ComandosEscPos.tablaCaracteres(2))

    @Test fun tablaCaracteresWin1252() =
        assertArrayEquals(byteArrayOf(0x1B, 0x74, 0x10), ComandosEscPos.tablaCaracteres(16))

    @Test fun alinearIzquierda() =
        assertArrayEquals(byteArrayOf(0x1B, 0x61, 0x00), ComandosEscPos.alinear(0))

    @Test fun alinearCentro() =
        assertArrayEquals(byteArrayOf(0x1B, 0x61, 0x01), ComandosEscPos.alinear(1))

    @Test fun alinearDerecha() =
        assertArrayEquals(byteArrayOf(0x1B, 0x61, 0x02), ComandosEscPos.alinear(2))

    @Test fun negritaOn() =
        assertArrayEquals(byteArrayOf(0x1B, 0x45, 0x01), ComandosEscPos.negrita(true))

    @Test fun negritaOff() =
        assertArrayEquals(byteArrayOf(0x1B, 0x45, 0x00), ComandosEscPos.negrita(false))

    @Test fun subrayado() =
        assertArrayEquals(byteArrayOf(0x1B, 0x2D, 0x01), ComandosEscPos.subrayado(true))

    @Test fun tamano1() =
        assertArrayEquals(byteArrayOf(0x1D, 0x21, 0x00), ComandosEscPos.tamano(1))

    @Test fun tamano2() =
        assertArrayEquals(byteArrayOf(0x1D, 0x21, 0x11), ComandosEscPos.tamano(2))

    @Test fun tamano3() =
        assertArrayEquals(byteArrayOf(0x1D, 0x21, 0x22), ComandosEscPos.tamano(3))

    @Test fun fuenteNormal() =
        assertArrayEquals(byteArrayOf(0x1B, 0x4D, 0x00), ComandosEscPos.fuente(0))

    @Test fun fuenteCompacta() =
        assertArrayEquals(byteArrayOf(0x1B, 0x4D, 0x01), ComandosEscPos.fuente(1))

    @Test fun saltoLinea() =
        assertArrayEquals(byteArrayOf(0x0A), ComandosEscPos.SALTO_LINEA)

    @Test fun avanzarLineas() =
        assertArrayEquals(byteArrayOf(0x1B, 0x64, 0x03), ComandosEscPos.avanzarLineas(3))

    @Test fun cortarTotal() =
        assertArrayEquals(byteArrayOf(0x1D, 0x56, 0x41, 0x03), ComandosEscPos.cortar(ModoCorte.TOTAL))

    @Test fun cortarParcial() =
        assertArrayEquals(byteArrayOf(0x1D, 0x56, 0x42, 0x03), ComandosEscPos.cortar(ModoCorte.PARCIAL))

    @Test fun abrirCajon() =
        assertArrayEquals(
            byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()),
            ComandosEscPos.ABRIR_CAJON,
        )
}
