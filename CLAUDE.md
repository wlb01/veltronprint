# CLAUDE.md — Veltron Print

> Este archivo lo lee la IA antes de programar cualquier cosa. Sin este archivo, cada sesión empieza desde cero.
> Antes de construir un módulo, lee también `ESTADO_PROYECTO.md`, los archivos de `specs/constitution/` y los tres archivos de la feature.

## 1. Quién soy

Veltron Print es una app Android nativa de uso exclusivo interno para **Veltron Tech**, taller de reparación de celulares en Mazatlán, Sinaloa. Imprime tickets en la impresora térmica de 80 mm del taller por Bluetooth clásico (SPP) usando comandos ESC/POS, y reemplaza a RawBT.

- **Problema que resuelve:** RawBT imprime con marca de agua sin suscripción y, mandando `.txt`, no se puede centrar bien, cortar el papel ni imprimir imágenes.
- **Cómo funciona:** Veltron Tech Registro (sistema web React + PHP) abre un enlace `veltronprint://` con un JSON de bloques. La app lo valida, lo convierte a ESC/POS y lo imprime. **Registro decide qué se imprime; la app decide cómo.**
- **Desarrollador:** Daniel, programador autodidacta con experiencia en React/PHP, aprendiendo Kotlin con este proyecto. Explica brevemente los conceptos de Kotlin/Android nuevos la primera vez que aparezcan (corrutinas, StateFlow, Composables, permisos, Room).
- **Equipos:** Samsung Galaxy Tab S10 Lite (Android 16), equipo principal de recepción. Honor X8 (Android 11), respaldo.
- **Impresora:** Ofichido POS-8390L — 80 mm, 203 DPI, 576 puntos de ancho imprimible, Bluetooth clásico sin BLE, ESC/POS, corte automático, puerto de cajón de dinero.

## 2. Stack tecnológico

| Capa | Tecnología | Notas |
|------|-----------|-------|
| Lenguaje | Kotlin | — |
| Interfaz | Jetpack Compose + Material 3 | Una sola Activity |
| Arquitectura | MVVM: `ViewModel` + `StateFlow` | La pantalla solo observa estado y envía eventos |
| Navegación | Navigation Compose | — |
| Asincronía | Kotlin Coroutines | Bluetooth y base de datos siempre en `Dispatchers.IO` |
| Bluetooth | `BluetoothSocket` RFCOMM, UUID SPP `00001101-0000-1000-8000-00805F9B34FB` | Sin librerías de terceros |
| ESC/POS | Codificador propio | Sin librerías de terceros |
| QR / código de barras | ZXing core → imagen raster | No usar los comandos nativos de QR de la impresora |
| JSON | kotlinx.serialization | — |
| Base de datos | Room (SQLite) | Cola e historial |
| Preferencias | DataStore Preferences | — |
| Inyección de dependencias | Manual: `AppContainer` | No usar Hilt |
| Pruebas | JUnit | Obligatorias para ESC/POS y validación del contrato |
| SDK | `minSdk 26`, `targetSdk`/`compileSdk` el más reciente estable | — |

**No agregues dependencias que no estén en esta tabla sin preguntarle antes a Daniel.**

Detalle y justificación de cada decisión: `specs/constitution/tech-stack.md`.

## 3. Identidad visual (solo interfaz; la impresión es siempre blanco y negro)

| Elemento | Valor |
|----------|-------|
| Azul Veltron (primario) | `#0048AE` |
| Azul Marino | `#01112A` — texto en modo claro, fondo en modo oscuro |
| Azul Profundo | `#08355E` — superficies en modo oscuro |
| Azul Claro | `#3973A1` — detalles secundarios |
| Dorado Energía (acento) | `#FFD51D` — solo acentos puntuales, nunca como fondo |
| Blanco | `#FFFFFF` — fondo en modo claro |
| Tipografía | Montserrat: ExtraBold para títulos, Regular/Medium para cuerpo (`res/font`) |
| Tema | Claro y oscuro; por defecto sigue el del sistema |

Los colores viven únicamente en `ui/theme/Color.kt` y se usan a través de `MaterialTheme.colorScheme`. Nunca escribas un color hexadecimal dentro de una pantalla.

## 4. Estructura de carpetas

```
veltron-print/
├── CLAUDE.md
├── ESTADO_PROYECTO.md
├── specs/
│   ├── constitution/
│   │   ├── mission.md
│   │   ├── tech-stack.md
│   │   ├── roadmap.md
│   │   └── contrato-bloques.md        ← formato de los trabajos de impresión
│   └── features/
│       └── 001-conexion-bluetooth/
│           ├── spec.md
│           ├── plan.md
│           └── tasks.md
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/mx/veltrontech/print/
│       │   │   ├── MainActivity.kt
│       │   │   ├── VeltronPrintApp.kt     ← clase Application, crea el AppContainer
│       │   │   ├── AppContainer.kt        ← instancia y comparte las dependencias
│       │   │   ├── inicio/                ← pantalla de inicio (001 y siguientes)
│       │   │   ├── bluetooth/             ← 001 conexión con la impresora
│       │   │   ├── escpos/                ← 002 comandos y codificador
│       │   │   ├── imagen/                ← 003 conversión a 1 bit y logo
│       │   │   ├── codigos/               ← 004 QR y código de barras
│       │   │   ├── contrato/              ← 005 modelos del JSON, validación y receptor de enlaces
│       │   │   ├── datos/                 ← base de datos Room y DataStore
│       │   │   ├── cola/                  ← 006 cola y reintento
│       │   │   ├── historial/             ← 007
│       │   │   ├── ajustes/               ← 003 logo; 008 resto de ajustes
│       │   │   ├── manual/                ← 009 tickets manuales
│       │   │   └── ui/
│       │   │       ├── theme/             ← Color.kt, Type.kt, Theme.kt
│       │   │       ├── componentes/       ← Composables reutilizables
│       │   │       └── navegacion/        ← NavHost y rutas
│       │   └── res/
│       │       ├── font/                  ← Montserrat
│       │       ├── values/strings.xml     ← todos los textos de la interfaz
│       │       └── drawable/
│       └── test/java/mx/veltrontech/print/  ← pruebas JUnit, mismo árbol de paquetes
└── gradle/libs.versions.toml               ← versiones de todas las dependencias
```

Cada paquete de feature contiene su lógica y, si tiene pantalla, su `XxxScreen.kt` y `XxxViewModel.kt`.

## 5. Convenciones de nomenclatura

| Tipo | Convención | Ejemplo |
|------|-----------|---------|
| Paquete | Inverso de dominio | `mx.veltrontech.print.bluetooth` |
| Clases y archivos Kotlin | PascalCase | `PrinterConnection.kt` |
| Pantallas | `XxxScreen` + `XxxViewModel` | `HistorialScreen.kt`, `HistorialViewModel.kt` |
| Funciones `@Composable` | PascalCase | `TarjetaTrabajo()` |
| Funciones y variables | camelCase | `imprimirTrabajo()` |
| Constantes | SCREAMING_SNAKE_CASE | `ANCHO_PUNTOS` |
| Tablas Room | snake_case plural | `trabajos_impresion` |
| Recursos (`res/`) | snake_case | `ic_impresora.xml` |
| Claves de `strings.xml` | snake_case por pantalla | `ajustes_titulo_densidad` |
| Carpetas de features | número-nombre | `001-conexion-bluetooth/` |
| Ramas Git | feature/nombre | `feature/conexion-bluetooth` |
| Commits | tipo: descripción | `feat: conexión Bluetooth con la impresora` |

Nombres del dominio (pantallas, variables de negocio, tablas) en español; términos técnicos estándar de Android (`ViewModel`, `Repository`, `Dao`) se conservan en inglés.

## 6. Reglas de negocio globales

- Registro decide **qué** se imprime; la app decide **cómo**. La app nunca agrega, quita ni reordena contenido de un trabajo recibido.
- El único contenido que genera la app por sí misma: la impresión de prueba (008) y los tickets manuales (009).
- El formato de los trabajos está definido **únicamente** en `specs/constitution/contrato-bloques.md`. Si un módulo necesita cambiar el formato, detente y pregúntale a Daniel; el contrato se actualiza primero.
- Un trabajo inválido se rechaza completo; nunca se imprime un ticket a medias.
- Una sola impresora vinculada a la vez.
- Ancho fijo de 80 mm: 576 puntos, 48 columnas en fuente normal y 64 en compacta.
- Un trabajo que no se imprimió nunca se pierde en silencio: queda en la cola hasta imprimirse o hasta que el usuario lo cancele.
- La reimpresión usa el JSON original del trabajo.
- El historial conserva 30 días y luego se borra automáticamente.
- La app funciona sin internet. No pidas el permiso `INTERNET`.
- El logo se guarda en la app; no viaja en los trabajos.

## 7. Reglas de código que siempre aplican

**Bluetooth e hilos**
- Nunca hagas operaciones de Bluetooth ni de base de datos en el hilo principal.
- Antes de cualquier operación Bluetooth, verifica permisos con los dos esquemas: `BLUETOOTH_CONNECT` en Android 12+ (no se usa `BLUETOOTH_SCAN` porque la app no busca dispositivos) y `BLUETOOTH`/`BLUETOOTH_ADMIN` (`maxSdkVersion="30"`) en Android 11 o menor.
- Toda conexión se cierra en `finally` o con `use {}`. Nunca dejes un socket abierto tras un error.
- Los datos se envían en bloques de **512 bytes con 50 ms de pausa** entre bloques (`PrinterConnection`). La Ofichido POS-8390L tiene un búfer de recepción Bluetooth pequeño; enviar todo de golpe provoca que descarte datos a mitad de las imágenes.
- Cualquier error del Bluetooth se traduce a un mensaje en español para el usuario; nunca muestres una excepción cruda.

**ESC/POS**
- Todos los comandos son constantes con nombre en un solo archivo de `escpos/`, con un comentario que diga qué hacen. Nunca escribas bytes sueltos fuera de ese archivo.
- La conversión de acentos y ñ a la tabla de caracteres pasa por una sola función.
- Ninguna imagen se envía a la impresora con más de 576 px de ancho.
- Cada comando y cada tipo de bloque tiene su prueba JUnit que compara el arreglo de bytes esperado.

**Interfaz**
- Cada pantalla funciona en horizontal y en vertical, en la tablet y en el teléfono. Nunca bloquees la orientación. Usa layouts adaptables según el ancho disponible.
- Diseño edge-to-edge: respeta los `WindowInsets` para que nada quede debajo de las barras del sistema.
- Cada pantalla que dependa de datos o de la impresora maneja estos estados: **cargando, error y vacío**, más los estados de la impresora: **desconectada, conectando, lista, imprimiendo y error**.
- Todos los textos visibles van en `strings.xml`, en español. Nada de textos fijos en el código.
- Áreas táctiles de al menos 48 dp.

**General**
- La pantalla no tiene lógica: observa el `StateFlow` del `ViewModel` y le envía eventos.
- Las versiones de las dependencias van en `gradle/libs.versions.toml`.
- Daniel usa Windows con Git Bash en la terminal de Android Studio. Usa `./gradlew` para compilar y probar.
- Antes de dar un módulo por terminado, ejecuta `./gradlew test` y `./gradlew assembleDebug` y confirma que pasan.
- La QA final de cada módulo se hace en un dispositivo físico con la impresora real, porque el emulador no tiene Bluetooth.

**Flujo de trabajo**
- Tú (Claude Code) haces los commits de Git al aprobar cada módulo, con el formato de la sección 5.
- Tú actualizas `ESTADO_PROYECTO.md` al cerrar cada sesión.
- Si algo del spec es ambiguo o contradice este archivo, pregunta antes de programar. No inventes comportamiento.

## 8. Reglas de navegación

- La app tiene una sola Activity; las pantallas se navegan con Navigation Compose.
- Las pantallas principales corresponden a los módulos con interfaz: inicio con el estado de la impresora y la cola, historial, ticket manual y ajustes. El diseño exacto de cada una se define en su spec.
- La Activity declara el filtro de intent para el esquema `veltronprint://imprimir`, de modo que un enlace desde Chrome abre la app directamente. El comportamiento al recibirlo se define en el spec del módulo 005.
- Si la app se abre sin impresora vinculada, lleva al usuario a vincularla antes de cualquier otra acción que necesite imprimir.
- Si faltan permisos de Bluetooth, la app los pide explicando para qué son; si el usuario los niega, muestra cómo activarlos desde los ajustes del sistema.
