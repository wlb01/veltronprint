package mx.veltrontech.print.contrato

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class DecodificadorEnlaceTest {

    private fun codificar(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray(Charsets.UTF_8))

    @Test fun nullEsSinDatos() {
        assertEquals(ResultadoDecodificacion.SinDatos, DecodificadorEnlace.decodificar(null))
    }

    @Test fun vacioesSinDatos() {
        assertEquals(ResultadoDecodificacion.SinDatos, DecodificadorEnlace.decodificar(""))
    }

    @Test fun soloEspaciosSinDatos() {
        assertEquals(ResultadoDecodificacion.SinDatos, DecodificadorEnlace.decodificar("   "))
    }

    @Test fun base64InvalidoEsDanado() {
        assertEquals(ResultadoDecodificacion.Danado, DecodificadorEnlace.decodificar("%%%"))
    }

    @Test fun decodificaSinRelleno() {
        val json = "{\"v\":1}"
        assertEquals(ResultadoDecodificacion.Ok(json), DecodificadorEnlace.decodificar(codificar(json)))
    }

    @Test fun decodificaConRelleno() {
        val json = "{\"v\":1}"
        val dConPadding = Base64.getUrlEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))
        assertEquals(ResultadoDecodificacion.Ok(json), DecodificadorEnlace.decodificar(dConPadding))
    }

    @Test fun aceptaGuionesYSubrayados() {
        // 0xFBEFBE → "----", 0xFFFFFF → "____" en base64 URL-safe; verifica que - y _ se aceptan
        val result = DecodificadorEnlace.decodificar("----____")
        assertTrue("Esperaba Ok, got $result", result is ResultadoDecodificacion.Ok)
    }

    @Test fun acentosUtf8IdaYVuelta() {
        val original = "Hola México: Ñoño café"
        assertEquals(ResultadoDecodificacion.Ok(original), DecodificadorEnlace.decodificar(codificar(original)))
    }
}
