package mx.veltrontech.print.contrato

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorTrabajoTest {

    private fun leerTicketOficial(): String =
        javaClass.classLoader!!.getResourceAsStream("ticket_oficial.json")!!
            .bufferedReader().readText()

    private fun valido(json: String): ResultadoValidacion.Valido {
        val r = ValidadorTrabajo.validar(json)
        assertTrue("Esperaba Valido, got $r", r is ResultadoValidacion.Valido)
        return r as ResultadoValidacion.Valido
    }

    private fun invalido(json: String, fragmento: String) {
        val r = ValidadorTrabajo.validar(json)
        assertTrue("Esperaba Invalido, got $r", r is ResultadoValidacion.Invalido)
        val detalle = (r as ResultadoValidacion.Invalido).detalle
        assertTrue("Detalle '$detalle' no contiene '$fragmento'", detalle.contains(fragmento))
    }

    // --- Ticket oficial ---

    @Test fun ticketOficial33Bloques() {
        val trabajo = valido(leerTicketOficial()).trabajo
        assertEquals(33, trabajo.bloques.size)
        assertEquals("registro", trabajo.tipo)
        assertEquals("MZT-OCT26-0042", trabajo.folio)
        assertEquals(1, trabajo.copias)
    }

    @Test fun ticketOficialPrimerBloqueEsLogo() {
        assertTrue(valido(leerTicketOficial()).trabajo.bloques[0] is Bloque.Logo)
    }

    @Test fun ticketOficialFolioConNegrita() {
        // bloque 7 (índice 6): par del folio con negrita: true
        val par = valido(leerTicketOficial()).trabajo.bloques[6] as Bloque.Par
        assertEquals("Folio:", par.izquierda)
        assertEquals("MZT-OCT26-0042", par.derecha)
        assertTrue(par.negrita)
    }

    @Test fun ticketOficialQrCorrecto() {
        // bloque 28 (índice 27): qr con la URL de T&C
        val qr = valido(leerTicketOficial()).trabajo.bloques[27] as Bloque.Qr
        assertEquals("https://veltrontech.mx/terminos", qr.valor)
        assertEquals(TamanoQr.MEDIANO, qr.tamano)
    }

    @Test fun ticketOficialCorteAlFinal() {
        assertTrue(valido(leerTicketOficial()).trabajo.bloques.last() is Bloque.Corte)
    }

    // --- JSON roto ---

    @Test fun jsonRotoEsDanado() {
        assertTrue(ValidadorTrabajo.validar("{no es json}") is ResultadoValidacion.Danado)
    }

    // --- v ---

    @Test fun vAusente() = invalido("""{"tipo":"t","bloques":[{"tipo":"corte"}]}""", "falta el campo v")

    @Test fun vComoTexto() = invalido("""{"v":"1","tipo":"t","bloques":[{"tipo":"corte"}]}""", "sin comillas")

    @Test fun vDos_versionDesconocida() {
        val r = ValidadorTrabajo.validar("""{"v":2,"bloques_invalidos":true}""")
        assertTrue(r is ResultadoValidacion.VersionDesconocida)
        assertEquals(2, (r as ResultadoValidacion.VersionDesconocida).v)
    }

    @Test fun vDosConBloquesInvalidos_sigueVersionDesconocida() {
        // v:2 incluso si bloques es inválido → VersionDesconocida (v se valida antes que bloques)
        val r = ValidadorTrabajo.validar("""{"v":2,"tipo":"t","bloques":[{"tipo":"prueba_tabla"}]}""")
        assertTrue(r is ResultadoValidacion.VersionDesconocida)
    }

    // --- tipo ---

    @Test fun tipoAusente() = invalido("""{"v":1,"bloques":[{"tipo":"corte"}]}""", "falta el campo tipo")

    @Test fun tipoVacio() = invalido("""{"v":1,"tipo":"","bloques":[{"tipo":"corte"}]}""", "no puede estar vacío")

    // --- copias ---

    @Test fun copiasFueraDeRango() = invalido(
        """{"v":1,"tipo":"t","copias":5,"bloques":[{"tipo":"corte"}]}""",
        "copias debe estar entre 1 y 3; llegó 5")

    @Test fun copiasComoTexto() = invalido(
        """{"v":1,"tipo":"t","copias":"2","bloques":[{"tipo":"corte"}]}""", "sin comillas")

    // --- bloques ---

    @Test fun bloquesAusentes() = invalido("""{"v":1,"tipo":"t"}""", "falta el campo bloques")

    @Test fun bloquesVacios() = invalido("""{"v":1,"tipo":"t","bloques":[]}""", "no puede estar vacío")

    @Test fun bloqueDesconocido() {
        invalido("""{"v":1,"tipo":"t","bloques":[{"tipo":"imagen"}]}""", "tipo desconocido")
        invalido("""{"v":1,"tipo":"t","bloques":[{"tipo":"imagen"}]}""", "\"imagen\"")
    }

    @Test fun bloqueInterno_pruebaTabla() =
        invalido("""{"v":1,"tipo":"t","bloques":[{"tipo":"prueba_tabla"}]}""", "tipo desconocido")

    // --- campos obligatorios ---

    @Test fun textoSinValor() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"texto"}]}""", "falta el campo valor")

    @Test fun parSinIzquierda() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"par","derecha":"d"}]}""", "falta el campo izquierda")

    @Test fun qrSinValor() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"qr"}]}""", "falta el campo valor")

    @Test fun codigoBarrasSinValor() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"codigo_barras"}]}""", "falta el campo valor")

    // --- valores fuera de rango ---

    @Test fun tamanoFueraDeRango() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"texto","valor":"x","tamano":7}]}""",
        "tamano debe ser 1, 2 o 3; llegó 7")

    @Test fun tamanoComoTexto() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"texto","valor":"x","tamano":"2"}]}""", "sin comillas")

    @Test fun negritaComoTexto() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"par","izquierda":"i","derecha":"d","negrita":"true"}]}""",
        "sin comillas")

    @Test fun separadorConDosCaracteres() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"separador","caracter":"--"}]}""",
        "exactamente un carácter")

    @Test fun codigoBarrasLargo() {
        val valor = "a".repeat(41)
        invalido("""{"v":1,"tipo":"t","bloques":[{"tipo":"codigo_barras","valor":"$valor"}]}""",
            "demasiado largo")
    }

    @Test fun codigoBarrasConAcento() = invalido(
        """{"v":1,"tipo":"t","bloques":[{"tipo":"codigo_barras","valor":"café"}]}""",
        "caracteres no permitidos")

    @Test fun qrDemasiadoLargo() {
        val valor = "a".repeat(501)
        invalido("""{"v":1,"tipo":"t","bloques":[{"tipo":"qr","valor":"$valor"}]}""",
            "demasiado largo")
    }

    // --- campos desconocidos ignorados ---

    @Test fun campoExtraDesconocidoEsValido() {
        val r = ValidadorTrabajo.validar(
            """{"v":1,"tipo":"t","campoExtra":"ignorame","bloques":[{"tipo":"corte"}]}""")
        assertTrue(r is ResultadoValidacion.Valido)
    }
}
