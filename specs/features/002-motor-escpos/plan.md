# Plan — Módulo 002: Motor ESC/POS

> El CÓMO técnico. Se escribe después de spec.md y antes de programar.

## Archivos a crear

**Modelo del trabajo** (sin anotaciones de serialización; el módulo 005 las agrega)
- `app/src/main/java/mx/veltrontech/print/contrato/Trabajo.kt` — `data class Trabajo(v: Int, tipo: String, folio: String?, copias: Int = 1, bloques: List<Bloque>)`.
- `app/src/main/java/mx/veltrontech/print/contrato/Bloque.kt` — `sealed interface Bloque` con los 9 tipos del contrato: `Texto`, `Par`, `Separador`, `Espacio`, `Logo`, `Qr`, `CodigoBarras`, `Corte`, `Cajon`. Enums `Alineacion { IZQUIERDA, CENTRO, DERECHA }`, `Fuente { NORMAL, COMPACTA }`, `ModoCorte { TOTAL, PARCIAL }`, `TamanoQr { CHICO, MEDIANO, GRANDE }`. Valores por defecto iguales a los del contrato (corte sin modo = `null`, se resuelve al predeterminado).

**Motor**
- `app/src/main/java/mx/veltrontech/print/escpos/ComandosEscPos.kt` — único archivo con bytes ESC/POS, cada constante o función comentada:

  | Nombre | Bytes | Qué hace |
  |---|---|---|
  | `INICIALIZAR` | `1B 40` | Reinicia la impresora |
  | `tablaCaracteres(n)` | `1B 74 n` | Selecciona tabla (CP850 = 2, Windows-1252 = 16) |
  | `alinear(a)` | `1B 61 n` | 0 izquierda, 1 centro, 2 derecha |
  | `negrita(on)` | `1B 45 n` | 1 activa, 0 desactiva |
  | `subrayado(on)` | `1B 2D n` | 1 activa, 0 desactiva |
  | `tamano(t)` | `1D 21 n` | `n = ((t-1) shl 4) or (t-1)` → 1 = `00`, 2 = `11`, 3 = `22` |
  | `fuente(f)` | `1B 4D n` | 0 normal (A, 12×24), 1 compacta (B, 9×17) |
  | `SALTO_LINEA` | `0A` | Avanza una línea |
  | `avanzarLineas(n)` | `1B 64 n` | Avanza n líneas |
  | `cortar(modo)` | `1D 56 41 03` total · `1D 56 42 03` parcial | Avanza hasta la cuchilla + 3 puntos y corta |
  | `ABRIR_CAJON` | `1B 70 00 19 FA` | Pulso al pin 2 (25 ms encendido, 250 ms apagado) |

- `app/src/main/java/mx/veltrontech/print/escpos/TablaCaracteres.kt` — `enum class TablaCaracteres(val numero: Int)` con `CP850(2)` y `WIN1252(16)`, y el mapa de conversión de cada una:

  | Carácter | CP850 | Windows-1252 |
  |---|---|---|
  | á é í ó ú | `A0 82 A1 A2 A3` | `E1 E9 ED F3 FA` |
  | Á É Í Ó Ú | `B5 90 D6 E0 E9` | `C1 C9 CD D3 DA` |
  | ñ Ñ | `A4 A5` | `F1 D1` |
  | ü Ü | `81 9A` | `FC DC` |
  | ¿ ¡ | `A8 AD` | `BF A1` |
  | ° | `F8` | `B0` |

  > Se usa un mapa propio y no `Charset.forName("IBM850")` porque no todas las versiones de Android traen esa codificación, y el mapa se puede probar con JUnit.

- `app/src/main/java/mx/veltrontech/print/escpos/Codificador.kt` — `fun codificar(texto: String, tabla: TablaCaracteres): ByteArray`: ASCII imprimible tal cual; caracteres del mapa → su byte; emojis y otros pares sustitutos → se eliminan; resto → `Normalizer.normalize(NFD)` sin marcas diacríticas si queda ASCII, si no `?`.
- `app/src/main/java/mx/veltrontech/print/escpos/AjusteLinea.kt` — funciones puras: `cortarPorPalabra(texto, columnas): List<String>` y `componerPar(izquierda, derecha, columnas): List<String>`.
- `app/src/main/java/mx/veltrontech/print/escpos/MotorEscPos.kt` — `class MotorEscPos(tabla: TablaCaracteres, corteDefault: ModoCorte)` con `fun generar(trabajo: Trabajo): ByteArray`. Por cada copia: `INICIALIZAR` + `tablaCaracteres` + bloques; después de cada bloque restablece estilos (alinear izquierda, negrita off, subrayado off, tamaño 1, fuente normal).
- `app/src/main/java/mx/veltrontech/print/escpos/ConfigImpresion.kt` — constantes temporales hasta el módulo 008: `TABLA_PREDETERMINADA = TablaCaracteres.CP850`, `CORTE_PREDETERMINADO = ModoCorte.PARCIAL`, `COLUMNAS_NORMAL = 48`, `COLUMNAS_COMPACTA = 64`.

**Ticket de prueba**
- `app/src/main/java/mx/veltrontech/print/ajustes/TicketPrueba.kt` — `fun ticketPrueba(): Trabajo` con los bloques exactos de `contrato-bloques.md` sección 6 más la sección "PRUEBA DE ACENTOS".
  - La sección de acentos necesita cambiar de tabla dentro de una línea, algo que el contrato no permite. Se resuelve con un bloque interno `BloquePruebaTabla(etiqueta, tabla)` que **solo** existe en código, no en el contrato y nunca se acepta desde un enlace. Se elimina en el módulo 008 si Ajustes ya no lo necesita.

**Servicio de impresión**
- `app/src/main/java/mx/veltrontech/print/escpos/ServicioImpresion.kt` — `suspend fun imprimir(trabajo: Trabajo): ResultadoEnvio` = `MotorEscPos.generar()` + `PrinterConnection.enviar()`. Es el punto de entrada que usarán 005, 006 y 008.

**Pruebas JUnit** (`app/src/test/java/mx/veltrontech/print/escpos/`)
- `ComandosEscPosTest.kt` — bytes exactos de cada comando de la tabla, incluidos `tamano(2) = 1D 21 11` y `ABRIR_CAJON`.
- `CodificadorTest.kt` — cada carácter del mapa en ambas tablas; `à → a`; `€ → ?`; emoji eliminado; ASCII intacto.
- `AjusteLineaTest.kt` — texto corto; corte por palabra; palabra más larga que la línea; `\n`; espacios sobrantes; `par` que cabe; `par` que se corta; `par` con `derecha` demasiado largo.
- `MotorEscPosTest.kt` — un trabajo de 1 bloque produce `INICIALIZAR + tabla + ... + restablecer`; `separador` produce 48 caracteres + salto; `copias = 2` duplica la secuencia; `corte` sin modo usa parcial; `cajon` produce `ABRIR_CAJON`.

## Archivos a modificar

- `AppContainer.kt` — crea `MotorEscPos` y `ServicioImpresion`.
- `inicio/InicioViewModel.kt` — estado `imprimiendo: Boolean`, evento `imprimirPrueba()`; al fallar actualiza el estado de la impresora a `NoResponde` con su error; al tener éxito emite el evento del snackbar.
- `inicio/InicioScreen.kt` — tarjeta "Prueba de impresión", snackbar y diseño de dos tarjetas (lado a lado en horizontal).
- `ui/componentes/` — nuevo `TarjetaPruebaImpresion.kt`.
- `res/values/strings.xml` — textos nuevos del spec.

## Modelo de datos

No hay datos persistentes nuevos. La tabla y el corte predeterminados son constantes en `ConfigImpresion.kt` hasta que el módulo 008 los mueva a DataStore.

## Estado en los ViewModel

- `InicioUiState` agrega `imprimiendo: Boolean`.
- Evento de una sola vez (`Channel` o `SharedFlow`) para el snackbar "Ticket enviado a la impresora.".

## Datos falsos para desarrollo

`TicketPrueba.kt` es el dato de prueba. Para revisar el resultado sin gastar papel, `MotorEscPosTest` puede incluir una función auxiliar que convierta los bytes a texto legible (por ejemplo `[ESC a 1]`) y la imprima en la consola de pruebas.

## Dependencias de este módulo

- Necesita el módulo 001 construido: usa `PrinterConnection.enviar()` y `ResultadoEnvio`.
- Requiere la impresora real y papel para la QA.
