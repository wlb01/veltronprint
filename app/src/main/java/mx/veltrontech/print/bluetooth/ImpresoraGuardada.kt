package mx.veltrontech.print.bluetooth

// data class: clase cuyo único propósito es contener datos.
// Kotlin genera automáticamente equals(), hashCode() y copy().
data class ImpresoraGuardada(
    val nombre: String,
    val mac: String, // dirección MAC, ej. "86:67:7A:12:34:56"
)
