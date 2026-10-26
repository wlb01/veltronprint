package mx.veltrontech.print.bluetooth

// sealed interface: como un enum mejorado donde cada variante puede tener sus propios datos.
// El compilador sabe que estas son TODAS las variantes posibles (exhaustivo en when()).
sealed interface ResultadoEnvio {
    data object Exito : ResultadoEnvio
    data object SinBluetooth : ResultadoEnvio
    data object BluetoothApagado : ResultadoEnvio
    data object SinPermiso : ResultadoEnvio
    data object SinImpresora : ResultadoEnvio   // ninguna impresora guardada
    data object NoEmparejada : ResultadoEnvio   // la guardada fue desemparejada
    data object NoResponde : ResultadoEnvio     // timeout de 10 s
    data object ConexionPerdida : ResultadoEnvio // se cayó durante el envío
}
