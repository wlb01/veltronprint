package mx.veltrontech.print.escpos

import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.ModoCorte
import mx.veltrontech.print.contrato.Trabajo
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
        // Solo INICIALIZAR + tabla, sin bloques ni resets
        val esperado = ComandosEscPos.INICIALIZAR + ComandosEscPos.tablaCaracteres(TablaCaracteres.CP850.numero)
        assertArrayEquals(esperado, bytes)
    }
}
