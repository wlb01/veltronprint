# Tech Stack — Veltron Print

> Parte 2 de la Spec de Sistema. Cada decisión tecnológica tiene su justificación.

## Stack elegido

| Capa | Tecnología | Justificación |
|------|-----------|----------------|
| Lenguaje | Kotlin | Lenguaje oficial de Android; acceso directo a las APIs de Bluetooth clásico sin librerías puente frágiles |
| IDE | Android Studio | Herramienta oficial; Claude Code corre en su terminal integrada con el mismo flujo SDD de siempre |
| Interfaz | Jetpack Compose + Material 3 | Interfaces declarativas parecidas a React (componentes y estado), lo que facilita la curva de aprendizaje |
| Arquitectura | MVVM: una sola Activity, `ViewModel` + `StateFlow` | Patrón estándar de Android; separa pantalla, estado y lógica como en Registro (página / hook / service) |
| Navegación | Navigation Compose | Librería oficial para moverse entre pantallas en Compose, equivalente a React Router |
| JSON del contrato | kotlinx.serialization | Convierte el JSON de los trabajos en clases Kotlin y, a diferencia de `org.json`, funciona en las pruebas JUnit sin emulador |
| Asincronía | Kotlin Coroutines | Las operaciones Bluetooth bloquean; las corrutinas las sacan del hilo principal sin complicar el código |
| Bluetooth | `BluetoothSocket` RFCOMM con UUID SPP `00001101-0000-1000-8000-00805F9B34FB` | La impresora es Bluetooth clásico sin BLE; SPP es su perfil de puerto serie |
| ESC/POS | Codificador propio | Se usan pocos comandos (`ESC @`, `ESC a`, `ESC E`, `ESC -`, `GS !`, `ESC M`, `ESC t`, `GS v 0`, `GS V`, `ESC p`); hacerlo propio da control total, cero dependencias y es fácil de probar |
| QR y código de barras | ZXing core, impresos como imagen raster | Genera la imagen sin depender de que la impresora genérica soporte bien los comandos nativos de QR |
| Base de datos local | Room (SQLite) | Cola de impresión e historial; consultas por folio y borrado de trabajos de más de 30 días |
| Preferencias | DataStore | Impresora vinculada, tabla de caracteres, densidad, modo de corte y ruta del logo |
| Inyección de dependencias | Manual (una clase contenedor) | Hilt agrega complejidad que no hace falta para una app de este tamaño |
| Pruebas | JUnit | El codificador ESC/POS se prueba comparando arreglos de bytes, sin necesidad de impresora |
| Distribución | APK instalado directamente en la tablet | Uso exclusivo interno; no se publica en Play Store |

## Versiones de Android

| Parámetro | Valor | Justificación |
|---|---|---|
| `minSdk` | 26 (Android 8) | Cubre la Galaxy Tab S10 Lite (Android 16) y el Honor X8 de respaldo (Android 11) |
| `targetSdk` / `compileSdk` | El más reciente estable que ofrezca Android Studio al crear el proyecto | Mantiene la app al día |
| Permisos Bluetooth | `BLUETOOTH_CONNECT` en Android 12+ (sin `BLUETOOTH_SCAN`: la app no busca dispositivos, solo usa los emparejados en Android); `BLUETOOTH` y `BLUETOOTH_ADMIN` (con `maxSdkVersion="30"`) en Android 11 o menor | Android 12 cambió el modelo de permisos; la tablet usa el esquema nuevo y el Honor el anterior, así que la app maneja ambos |
| Orientación y tamaño | Todas las pantallas funcionan en horizontal y vertical, en tablet y en teléfono; no se bloquea la orientación | En Android 16, las tablets ignoran los bloqueos de orientación de las apps que apuntan a la versión más reciente; además el Honor de respaldo es un teléfono |

## Identidad visual (interfaz de la app)

| Elemento | Valor |
|----------|-------|
| Azul Marino | `#01112A` — texto principal, fondo en modo oscuro |
| Azul Profundo | `#08355E` — superficies secundarias en modo oscuro |
| Azul Veltron (primario) | `#0048AE` — botones, acentos, elementos activos |
| Azul Claro | `#3973A1` — detalles secundarios |
| Dorado Energía (acento) | `#FFD51D` — solo acentos puntuales, nunca como fondo |
| Blanco | `#FFFFFF` — fondo en modo claro |
| Tipografía | Montserrat (ExtraBold para títulos, Regular/Medium para cuerpo), incluida en `res/font` |
| Temas | Claro y oscuro; por defecto sigue el tema del sistema |

> La impresión térmica es siempre blanco y negro puro. Estos colores aplican solo a la interfaz.

## Datos fijos de la impresora

| Dato | Valor |
|---|---|
| Ancho imprimible | 576 puntos (72 mm a 203 DPI) |
| Columnas fuente normal (A, 12×24) | 48 |
| Columnas fuente compacta (B, 9×17) | 64 |
| Ancho máximo de imágenes | 576 px, 1 bit (blanco y negro) |

## Convenciones

| Tipo | Convención | Ejemplo |
|------|-----------|---------|
| Paquete de la app | Inverso de dominio | `mx.veltrontech.print` |
| Paquetes internos | minúsculas, por capa o feature | `mx.veltrontech.print.bluetooth` |
| Clases y archivos Kotlin | PascalCase | `PrinterConnection.kt` |
| Funciones `@Composable` | PascalCase | `HistorialScreen()` |
| Funciones y variables | camelCase | `imprimirTrabajo()` |
| Constantes | SCREAMING_SNAKE_CASE | `ANCHO_PUNTOS` |
| Tablas Room | snake_case plural | `trabajos_impresion` |
| Recursos (`res/`) | snake_case | `ic_impresora.xml` |
| Carpetas de features (specs) | número-nombre | `001-conexion-bluetooth/` |
| Textos de la interfaz | En español, en `strings.xml` | `error_impresora_apagada` |

## Instalación del proyecto

1. Android Studio → **New Project** → **Empty Activity** (plantilla de Compose).
2. Name: `Veltron Print` · Package name: `mx.veltrontech.print` · Language: Kotlin · Minimum SDK: API 26 · Build configuration language: Kotlin DSL.
3. Dependencias a agregar en `app/build.gradle.kts` (se definen versiones exactas en el plan del módulo que las use):
   - Room (runtime, ktx y compiler vía KSP) — módulo 006
   - DataStore Preferences — módulo 001
   - ZXing core — módulo 004
   - Lifecycle ViewModel Compose y Navigation Compose — módulo 001
   - kotlinx.serialization (plugin + librería JSON) — módulo 005
4. Fuentes Montserrat en `app/src/main/res/font/`.
