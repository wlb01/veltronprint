# Plan — Módulo 005: Receptor de enlaces

> El CÓMO técnico. Se escribe después de spec.md y antes de programar.

## Dependencia nueva

- **kotlinx.serialization** (plugin `org.jetbrains.kotlin.plugin.serialization` con la misma versión de Kotlin del proyecto + librería `kotlinx-serialization-json`), aprobada en `tech-stack.md`. Versiones en `libs.versions.toml`.

## Archivos a crear

**Decodificación y validación** (funciones puras, sin Android, probables con JUnit)
- `contrato/DecodificadorEnlace.kt`
  - `fun decodificar(d: String?): ResultadoDecodificacion` → `Ok(json: String)` | `SinDatos` | `Danado`.
  - Base64 URL-safe con `java.util.Base64.getUrlDecoder()` (acepta con y sin relleno; está disponible desde API 26). Después `String(bytes, Charsets.UTF_8)`.
- `contrato/ValidadorTrabajo.kt`
  - `fun validar(json: String): ResultadoValidacion` → `Valido(trabajo: Trabajo)` | `Danado` (JSON mal formado) | `VersionDesconocida(v: Int)` | `Invalido(detalle: String)`.
  - Recorre el JSON como `JsonObject` (`Json.parseToJsonElement`) y valida campo por campo para poder generar el detalle técnico exacto de la spec. No se usa la deserialización automática con `@Serializable`: sus mensajes de error son en inglés y no indican el número de bloque.
  - Tipos estrictos: un número debe ser `JsonPrimitive` no string (`!isString`) convertible a `Int`; un booleano debe ser `true`/`false` sin comillas.
  - Ignora claves desconocidas.
  - Valida `v` primero (para dar "Versión desconocida" aunque lo demás esté mal), luego los campos del trabajo y luego cada bloque en orden; se detiene en el primer error.
  - Construye `Trabajo` y `Bloque` del paquete `contrato/` con los valores por defecto del contrato.
  - Nunca produce `BloquePruebaTabla`.
- `contrato/ResultadosContrato.kt` — las sealed interfaces `ResultadoDecodificacion` y `ResultadoValidacion`.

**Pantalla y Activity**
- `recepcion/RecibirTrabajoActivity.kt` — Activity separada de `MainActivity`:
  - Lee `intent.data`, toma `getQueryParameter("d")` y se lo pasa al ViewModel **solo si `savedInstanceState == null`** (así girar la tablet no reprocesa el enlace).
  - Al recibir el evento "terminado", llama a `finishAndRemoveTask()`, que cierra la pantalla, la quita de recientes y devuelve el foco a Chrome.
  - Tema de la app, edge-to-edge.
- `recepcion/RecibirTrabajoViewModel.kt` — `StateFlow<RecepcionUiState>`: `Imprimiendo(subtitulo)` | `Impreso` | `Error(mensajeRes, argumento?, detalle?, puedeReintentar, puedeVincular)`. Guarda el `Trabajo` ya validado para "Reintentar". Tras `Impreso`, espera 2000 ms y emite el evento "terminado".
- `recepcion/RecibirTrabajoScreen.kt` — Composable con los tres estados.

**Pruebas JUnit** (`app/src/test/java/mx/veltrontech/print/contrato/`)
- `DecodificadorEnlaceTest.kt` — con y sin relleno; caracteres `-` y `_`; acentos UTF-8 de ida y vuelta; `null`, vacío y `%%%` → resultado correcto.
- `ValidadorTrabajoTest.kt`:
  - El JSON del ticket oficial (copiado de `contrato-bloques.md` sección 6 a `app/src/test/resources/ticket_oficial.json`) produce un `Trabajo` con 33 bloques y los valores esperados en al menos: el primer bloque (`Logo`), el folio (`Par` con negrita), el QR y el corte.
  - Un caso por cada regla de rechazo de la spec, verificando el texto exacto del detalle técnico.
  - `v: 2` con bloques inválidos → `VersionDesconocida(2)`.
  - Campo extra desconocido → válido.
  - `"tamano": "2"` y `"negrita": "true"` → inválidos.
  - Un bloque `{"tipo": "prueba_tabla"}` → tipo desconocido.

**Página de pruebas**
- `herramientas/prueba-print.html` (raíz del proyecto, no forma parte del APK) — **ya la escribió Claude en el chat**; solo se verifica que esté en el repositorio.

## Archivos a modificar

- `AndroidManifest.xml` — declarar `RecibirTrabajoActivity`:
  ```xml
  <activity
      android:name=".recepcion.RecibirTrabajoActivity"
      android:exported="true"
      android:taskAffinity=""
      android:excludeFromRecents="true"
      android:launchMode="standard"
      android:theme="@style/Theme.VeltronPrint">
      <intent-filter>
          <action android:name="android.intent.action.VIEW" />
          <category android:name="android.intent.category.DEFAULT" />
          <category android:name="android.intent.category.BROWSABLE" />
          <data android:scheme="veltronprint" android:host="imprimir" />
      </intent-filter>
  </activity>
  ```
  - `taskAffinity=""` la separa de la tarea de `MainActivity`, para que no altere la pantalla donde estaba la app.
  - `launchMode="standard"`: cada enlace abre su propia instancia; el `Mutex` de `PrinterConnection` hace que se impriman en orden.
- `escpos/ServicioImpresion.kt` — sin cambios de firma; `RecibirTrabajoViewModel` usa `imprimir(trabajo)`.
- `escpos/ResultadoImpresion.kt` — sin cambios.
- `MainActivity.kt` + `VeltronNavHost.kt` — aceptar un extra `abrir = "elegir_impresora"` para que "Vincular impresora" abra directo esa pantalla.
- `res/values/strings.xml` — mensajes y textos de la spec. El de versión usa argumento: `Actualiza la app.` con `%1$d`.
- `CLAUDE.md` — agregar `recepcion/` a la estructura de carpetas (← 005 pantalla que recibe enlaces) y `herramientas/` en la raíz (← página de pruebas, fuera del APK).

## Modelo de datos

Sin persistencia en este módulo (la cola y el historial llegan en 006 y 007).

## Mapa de errores a la interfaz

| Origen | Estado de error |
|---|---|
| `SinDatos` | Sin datos · Cerrar |
| `Danado` (decodificación o JSON) | Datos dañados · Cerrar |
| `VersionDesconocida(n)` | Versión desconocida con `{n}` · Cerrar |
| `Invalido(detalle)` | Formato inválido + detalle · Cerrar |
| `ResultadoImpresion.ErrorContenido` | Código inválido · Cerrar |
| `ErrorConexion(SinImpresora)` | Sin impresora · Vincular impresora, Cerrar |
| `ErrorConexion(otro)` | Mensaje del módulo 001 · Reintentar, Cerrar |

## Datos de prueba

- `app/src/test/resources/ticket_oficial.json` — copia exacta del JSON de la sección 6 del contrato.
- `herramientas/prueba-print.html` — página para la QA manual en la tablet (ver tasks.md).

## Dependencias de este módulo

- Necesita 001 a 004 terminados.
- Para la QA: la página de pruebas subida a un sitio HTTPS (cPanel) y abierta en Chrome de la tablet.
