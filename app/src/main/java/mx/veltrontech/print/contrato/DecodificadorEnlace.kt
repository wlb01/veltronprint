package mx.veltrontech.print.contrato

object DecodificadorEnlace {
    // getUrlDecoder: acepta los chars '-' y '_' del Base64 URL-safe y no requiere relleno '='
    fun decodificar(d: String?): ResultadoDecodificacion {
        if (d.isNullOrBlank()) return ResultadoDecodificacion.SinDatos
        return try {
            val bytes = java.util.Base64.getUrlDecoder().decode(d)
            ResultadoDecodificacion.Ok(String(bytes, Charsets.UTF_8))
        } catch (e: Exception) {
            ResultadoDecodificacion.Danado
        }
    }
}
