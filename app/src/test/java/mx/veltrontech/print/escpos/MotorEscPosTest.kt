package mx.veltrontech.print.escpos

import mx.veltrontech.print.codigos.CodigoInvalidoException
import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.ModoCorte
import mx.veltrontech.print.contrato.TamanoQr
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.imagen.ImagenMonocromo
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorEscPosTest {

    private val motor = MotorEscPos(TablaCaracteres.CP850, ModoCorte.PARCIAL)

    private fun trabajo(vararg bloques: Bloque, copias: Int = 1) =
        Trabajo(v = 1, tipo = "test", copias = copias, bloques = bloques.toList())

    @Test fun encabezadoDeUnTrabajo() {
        val bytes = motor.generar(trabajo(Bloque.Espacio(1)))
        // Debe empezar con INICIALIZAR + tabla CP850
        val encabezado = ComandosEscPos.INICIALIZAR + ComandosEscPos.tablaCaracteres(TablaCaracteres.CP850.numero)
        val inicio = bytes.take(encabezado.size).toByteArray()
        assertArrayEquals(encabezado, inicio)
    }

    @Test fun separadorProduce48CaracteresYSalto() {
        val bytes = motor.generar(trabajo(Bloque.Separador('-')))
        val guiones = ByteArray(48) { '-'.code.toByte() }
        val salto = byteArrayOf(0x0A)
        // Los bytes del separador deben estar dentro del resultado
        val resultado = bytes.toList()
        val buscado = (guiones + salto).toList()
        val idx = resultado.indices.firstOrNull { i ->
            resultado.subList(i, (i + buscado.size).coerceAtMost(resultado.size)) == buscado
        }
        assertTrue("No encontró 48 guiones + salto de línea", idx != null)
    }

    @Test fun copiasDuplicaLaSecuencia() {
        val bytes1 = motor.generar(trabajo(Bloque.Separador(), copias = 1))
        val bytes2 = motor.generar(trabajo(Bloque.Separador(), copias = 2))
        // copias=2 debe tener exactamente el doble de bytes que copias=1
        assertEquals(bytes1.size * 2, bytes2.size)
        // Y la primera mitad de copias=2 debe ser igual a copias=1
        assertArrayEquals(bytes1, bytes2.take(bytes1.size).toByteArray())
    }

    @Test fun corteSinModoUsaParcial() {
        val bytes = motor.generar(trabajo(Bloque.Corte(null)))
        val corteParcial = ComandosEscPos.cortar(ModoCorte.PARCIAL)
        assertTrue(bytes.toList().containsAll(corteParcial.toList()))
    }

    @Test fun corteExplicito() {
        val bytes = motor.generar(trabajo(Bloque.Corte(ModoCorte.TOTAL)))
        val corteTotal = ComandosEscPos.cortar(ModoCorte.TOTAL)
        assertTrue(bytes.toList().containsAll(corteTotal.toList()))
    }

    @Test fun cajonProduceAbrirCajon() {
        val bytes = motor.generar(trabajo(Bloque.Cajon))
        assertTrue(bytes.toList().containsAll(ComandosEscPos.ABRIR_CAJON.toList()))
    }

    @Test fun resetEstilosDespuesDeTexto() {
        val bytes = motor.generar(trabajo(Bloque.Texto("X", negrita = true)))
        // Debe incluir negrita(false) después del bloque
        val negritaOff = ComandosEscPos.negrita(false)
        assertTrue(bytes.toList().containsAll(negritaOff.toList()))
    }

    @Test fun textoCortoProduce1Linea() {
        val bytes = motor.generar(trabajo(Bloque.Texto("Hola")))
        // "Hola" + SALTO_LINEA deben estar presentes
        val esperado = Codificador.codificar("Hola", TablaCaracteres.CP850) + byteArrayOf(0x0A)
        assertTrue(bytes.toList().containsAll(esperado.toList()))
    }

    @Test fun trabajoVacioSoloCabecera() {
        val bytes = motor.generar(trabajo())
        val esperado = ComandosEscPos.INICIALIZAR + ComandosEscPos.tablaCaracteres(TablaCaracteres.CP850.numero)
        assertArrayEquals(esperado, bytes)
    }

    // Logo con imagen → contiene el encabezado GS v 0
    @Test fun logoConImagenGeneraRaster() {
        val negros = BooleanArray(576 * 10) { false }
        val logo = ImagenMonocromo(576, 10, negros)
        val bytes = motor.generar(trabajo(Bloque.Logo()), logo)
        // El comando GS v 0 debe aparecer en los bytes
        var found = false
        for (i in 0 until bytes.size - 3) {
            if (bytes[i] == 0x1D.toByte() && bytes[i + 1] == 0x76.toByte()) { found = true; break }
        }
        assertTrue("Esperado comando GS v 0 (0x1D 0x76)", found)
    }

    @Test fun qrGeneraRaster() {
        val bytes = motor.generar(trabajo(Bloque.Qr("ABC", TamanoQr.CHICO)))
        var found = false
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == 0x1D.toByte() && bytes[i + 1] == 0x76.toByte()) { found = true; break }
        }
        assertTrue("Bloque QR debe generar comandos GS v 0", found)
    }

    @Test fun codigoBarrasConTextoGeneraImagenYTexto() {
        val bytes = motor.generar(trabajo(Bloque.CodigoBarras("ABC", mostrarTexto = true)))
        var foundGsV0 = false
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == 0x1D.toByte() && bytes[i + 1] == 0x76.toByte()) { foundGsV0 = true; break }
        }
        assertTrue("Código de barras debe generar GS v 0", foundGsV0)
        val abcBytes = Codificador.codificar("ABC", TablaCaracteres.CP850)
        assertTrue("Debe incluir el valor en texto", bytes.toList().containsAll(abcBytes.toList()))
    }

    @Test(expected = CodigoInvalidoException::class)
    fun codigoBarrasInvalidoLanzaExcepcion() {
        motor.generar(trabajo(Bloque.CodigoBarras("A".repeat(41))))
    }

    // Logo sin imagen → no genera bytes de imagen
    @Test fun logoSinImagenNoProduce() {
        val sinLogo = motor.generar(trabajo(Bloque.Logo()), null)
        val conLogo = motor.generar(trabajo(Bloque.Espacio(1)), null)
        // Sin logo, solo hay cabecera + reset (sin datos de imagen, tamaño similar al bloque vacío)
        // El trabajo con Logo sin imagen debería tener menos bytes que uno con una imagen real
        // Verificación simple: no aparece 0x1D 0x76 en la salida
        var found = false
        for (i in 0 until sinLogo.size - 1) {
            if (sinLogo[i] == 0x1D.toByte() && sinLogo[i + 1] == 0x76.toByte()) { found = true; break }
        }
        assertTrue("Logo sin imagen no debe generar GS v 0", !found)
    }
}
