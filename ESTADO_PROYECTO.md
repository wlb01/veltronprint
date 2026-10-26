# Estado del Proyecto — Veltron Print

Última actualización: 2026-10-26

## Estado general

| Módulo | Estado | Notas |
|--------|--------|-------|
| 001 Conexión Bluetooth | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 002 Motor ESC/POS | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 003 Imágenes y Logo | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 004 QR y códigos de barras | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 005 Receptor de enlaces | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 006–009 | Sin iniciar | — |

## Módulo 005 — Receptor de enlaces

**Completado (código):**
- `kotlinx.serialization` añadido (plugin + `kotlinx-serialization-json 1.8.1`)
- `contrato/ResultadosContrato`: sealed interfaces `ResultadoDecodificacion` y `ResultadoValidacion`
- `contrato/DecodificadorEnlace`: Base64 URL-safe con `java.util.Base64.getUrlDecoder()` (API 26+); acepta con y sin relleno
- `contrato/ValidadorTrabajo`: recorre el `JsonObject` a mano; tipos estrictos (no string para números ni booleanos); valida `v` primero para dar `VersionDesconocida` aunque lo demás esté mal; claves desconocidas ignoradas; detalle exacto con número de bloque
- `recepcion/RecibirTrabajoViewModel`: `StateFlow<RecepcionUiState>` con estados `Imprimiendo`, `Impreso` y `Error`; guarda el `Trabajo` validado para "Reintentar"; cierre automático 2 s tras éxito vía `Channel`
- `recepcion/RecibirTrabajoScreen`: tres estados con iconos (Print, CheckCircle, Warning); ancho máximo 480 dp; funciona en horizontal y vertical
- `recepcion/RecibirTrabajoActivity`: Activity separada; `savedInstanceState == null` evita reprocesar al rotar; `finishAndRemoveTask()` al cerrar; "Vincular impresora" abre `MainActivity` con extra `abrir = "elegir_impresora"`
- `AndroidManifest.xml`: intent-filter movido de `MainActivity` a `RecibirTrabajoActivity` con `taskAffinity=""` y `excludeFromRecents="true"`
- `MainActivity` + `VeltronNavHost`: aceptan extra `abrir = "elegir_impresora"` como `startDestination`
- Tests: `DecodificadorEnlaceTest` (8 tests), `ValidadorTrabajoTest` (26 tests incluye ticket oficial completo)

**QA aprobado** — 2026-10-26. Commit `feat: receptor de enlaces veltronprint://` hecho.

## Módulo 004 — QR y códigos de barras

**Completado (código):**
- `codigos/CodigoInvalidoException`: excepción de dominio para contenido inválido
- `codigos/GeneradorQr`: ZXing `Encoder.encode()` directo, nivel M, margen 4 módulos, 4/6/8 px por módulo según `TamanoQr`; reduce el factor si no cabe en 576 px
- `codigos/GeneradorCodigoBarras`: `Code128Writer`, 1–40 chars ASCII imprimible, margen 10 unidades, módulo de 2 px (1 si no cabe), alto 80 px
- `escpos/ResultadoImpresion`: sealed interface con `Exito`, `ErrorConexion(ResultadoEnvio)`, `ErrorContenido(mensajeRes: Int)`; reemplaza `ResultadoEnvio` como tipo de retorno de `ServicioImpresion`
- `MotorEscPos`: bloques `Qr` y `CodigoBarras` generan `ImagenMonocromo` y la pasan a `RasterEscPos.generar()`
- `ServicioImpresion`: atrapa `CodigoInvalidoException` y devuelve `ErrorContenido`
- `InicioViewModel` / `AjustesViewModel`: actualizados para `ResultadoImpresion`; snackbar lleva `Int` (resource ID)
- `TicketPrueba`: sección "PRUEBA DE CÓDIGOS" con los tres tamaños de QR y un código de barras
- `PrinterConnection`: envío en bloques de **512 bytes con 50 ms de pausa** entre bloques — necesario porque la Ofichido tiene un búfer Bluetooth pequeño que se desborda con imágenes grandes enviadas de golpe
- Tests: `GeneradorQrTest` (7 tests, ida y vuelta con ZXing), `GeneradorCodigoBarrasTest` (5 tests), `MotorEscPosTest` (3 tests nuevos)

**QA aprobado** — 2026-10-26. Commit `feat: QR y códigos de barras` hecho.

**Decisión de hardware documentada:**
- La Ofichido no acepta varios `GS v 0` consecutivos para la misma imagen (solo imprime el último). `RasterEscPos.generar()` emite un único `GS v 0`; el control de caudal queda en `PrinterConnection`.

## Módulo 003 — Imágenes y Logo

**Completado (código):**
- `ImagenMonocromo`: modelo puro sin dependencias Android
- `ConversorImagen`: `aGrises`, `umbral`, tramado Floyd-Steinberg, `calcularTamano` — funciones puras testeables con JUnit
- `OpcionesLogo` / `TamanoLogo` / `ModoConversion` / `LogoGuardado` / `EdicionLogo`
- `ProcesadorImagen`: único archivo que usa Bitmap; decodifica con `ImageDecoder` (API 28+) y `BitmapFactory` (API 26-27), escala, procesa; convierte `ImagenMonocromo` ↔ `ImageBitmap`
- `RasterEscPos`: posicionar en lienzo 576 px, empaquetar 8 bits/byte MSB, generar franjas de 128 filas con GS v 0
- `LogoRepository`: guarda logo en `filesDir/logo/` con archivos temporales → rename; DataStore `"logo"` para opciones
- `ComandosEscPos.imagenRaster`: GS v 0 con encabezado xL xH yL yH
- `MotorEscPos.generar(trabajo, logo?)`: bloque Logo → `RasterEscPos.generar` si hay logo, vacío si no
- `ServicioImpresion`: carga logo del repositorio; `imprimirLogoPrueba()` imprime solo logo + corte
- `AjustesViewModel`: state flow con `guardado`, `edicion`, `vistaPreviaGuardada`; debounce 150 ms en claridad; recicla bitmaps
- `VistaPreviaPapel`: imagen en blanco y negro sobre fondo blanco con `FilterQuality.None`; fracción de ancho correcta
- `SeccionLogo`: controles (modo, claridad, tamaño), botones (elegir, guardar, imprimir prueba, quitar)
- `AjustesScreen`: layout adaptable (>600 dp → Row preview+controls; ≤600 → Column); diálogos "Cambios sin guardar" y "Quitar logo"; `BackHandler`
- Ruta `ajustes` en NavHost; engrane en TopAppBar de Inicio
- Tests: `ConversorImagenTest` (7 tests), `RasterEscPosTest` (5 tests), `MotorEscPosTest` (11 tests)

**QA aprobado** — 2026-10-25. Commit `feat: logo e impresión de imágenes` hecho.

**Pendiente de hardware futuro:**
- Honor X8: verificación visual pendiente (no disponible en la sesión de QA).

## Módulo 002 — Motor ESC/POS

**Completado (código):**
- Modelo del contrato: `Trabajo`, `Bloque` (sealed interface con 9 subtipos), enums de dominio
- `ComandosEscPos`: todos los bytes ESC/POS en un solo lugar, comentados
- `TablaCaracteres`: CP850 y WIN1252 con mapas propios testables
- `Codificador`: ASCII, mapa de tabla, NFD sin diacríticos, emojis eliminados
- `AjusteLinea`: `cortarPorPalabra` y `componerPar` como funciones puras
- `MotorEscPos`: generación de bytes por bloque, reset de estilos, copias
- `ServicioImpresion`: punto de entrada único para 005, 006 y 008
- `TicketPrueba`: ticket oficial completo con sección "PRUEBA DE ACENTOS"
- Interfaz: tarjeta "Prueba de impresión", snackbar, dos tarjetas lado a lado en horizontal
- Tests JUnit: `ComandosEscPosTest`, `CodificadorTest`, `AjusteLineaTest`, `MotorEscPosTest`

**QA aprobado** — 2026-10-25. Commit `feat: motor ESC/POS e impresión de prueba` hecho.

**Pendiente de hardware futuro:**
- El bloque `cajon` abre el cajón de dinero (cuando Daniel lo tenga; cubierto por JUnit).

## Módulo 001 — Conexión Bluetooth

**Completado (código):**
- Infraestructura base: tema Veltron, tipografía Montserrat (fuente variable), colores exactos
- Navegación: NavHost con rutas `inicio`, `elegir_impresora` y `ajustes`
- DataStore: guardado y lectura de impresora vinculada (nombre + MAC)
- Bluetooth: permisos, lista de emparejados, conexión RFCOMM con fallback por reflexión
- PrinterConnection: mutex, watchdog 10 s, delay 300 ms tras escritura, cierre en `finally`
- Pantallas: Inicio (tarjeta con 4 estados + engrane de Ajustes), ElegirImpresora (lista adaptable)
- Tests JUnit: PermisosBluetoothTest, OrdenarDispositivosTest

**QA aprobado** — 2026-10-25. Commit `feat: conexión Bluetooth con la impresora` hecho.

## Decisiones técnicas

- Montserrat se usa como fuente variable (`montserrat_variable.ttf`, peso 100–900).
- `LocalLifecycleOwner` se obtiene via `LocalContext.current as ComponentActivity`.
- El plugin `kotlin-android` NO se declara explícitamente: AGP 9.4.1 lo aplica automáticamente.
- `TablaCaracteres`: mapa propio (no `Charset.forName("IBM850")`) — testable con JUnit.
- `BloquePruebaTabla` vive en `contrato/`; se elimina en el módulo 008.
- `RasterEscPos.posicionar`: coloca la imagen en datos (no usa `ESC a`) para máxima compatibilidad.
- `LogoRepository` usa DataStore `"logo"` separado del `"ajustes"` de `PreferenciasRepository`.
- `debounce(150)` en claridad requiere `@OptIn(FlowPreview::class)`.
- `installDebug` (no `assembleDebug`) es el comando correcto para instalar en el dispositivo conectado.
- `RasterEscPos.generar()` emite un único `GS v 0` (no franjas): la Ofichido ignora varios `GS v 0` consecutivos para la misma imagen.
- `PrinterConnection` envía en bloques de 512 bytes con 50 ms de pausa: la Ofichido tiene un búfer Bluetooth pequeño que se desborda con payloads grandes enviados de golpe.

## Próximos pasos

1. Iniciar módulo 006 (cola de impresión y reintento automático).
