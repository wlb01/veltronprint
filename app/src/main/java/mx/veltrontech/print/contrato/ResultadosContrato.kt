package mx.veltrontech.print.contrato

sealed interface ResultadoDecodificacion {
    data class Ok(val json: String) : ResultadoDecodificacion
    data object SinDatos : ResultadoDecodificacion
    data object Danado : ResultadoDecodificacion
}

sealed interface ResultadoValidacion {
    data class Valido(val trabajo: Trabajo) : ResultadoValidacion
    data object Danado : ResultadoValidacion
    data class VersionDesconocida(val v: Int) : ResultadoValidacion
    data class Invalido(val detalle: String) : ResultadoValidacion
}
