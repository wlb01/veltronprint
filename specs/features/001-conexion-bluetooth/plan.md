# Plan — Módulo 001: Conexión Bluetooth

> El CÓMO técnico. Se escribe después de spec.md y antes de programar.

Este es el primer módulo, así que también construye la base de la app: tema, fuentes, contenedor de dependencias y navegación.

## Archivos a crear

**Base de la app**
- `gradle/libs.versions.toml` — versiones de Lifecycle ViewModel Compose, Navigation Compose y DataStore Preferences (agregar a las que crea la plantilla).
- `app/src/main/java/mx/veltrontech/print/VeltronPrintApp.kt` — clase `Application`; crea el `AppContainer` una sola vez.
- `app/src/main/java/mx/veltrontech/print/AppContainer.kt` — instancia `PreferenciasRepository`, `BluetoothRepository` y `PrinterConnection`, y los comparte.
- `app/src/main/java/mx/veltrontech/print/ui/theme/Color.kt` — los 6 colores de Veltron y los esquemas claro y oscuro.
- `app/src/main/java/mx/veltrontech/print/ui/theme/Type.kt` — tipografía Montserrat.
- `app/src/main/java/mx/veltrontech/print/ui/theme/Theme.kt` — `VeltronPrintTheme` (claro/oscuro según el sistema).
- `app/src/main/res/font/montserrat_regular.ttf`, `montserrat_medium.ttf`, `montserrat_extrabold.ttf` — se descargan del repositorio oficial de Google Fonts. Si Claude Code no puede descargarlas, se las pide a Daniel.
- `app/src/main/java/mx/veltrontech/print/ui/navegacion/Rutas.kt` — rutas `inicio`, `elegir_impresora`.
- `app/src/main/java/mx/veltrontech/print/ui/navegacion/VeltronNavHost.kt` — `NavHost` con las dos rutas.
- `app/src/main/java/mx/veltrontech/print/ui/componentes/AvisoPermiso.kt` — Composable del Aviso de permiso.

**Datos**
- `app/src/main/java/mx/veltrontech/print/datos/PreferenciasRepository.kt` — DataStore con las claves `impresora_mac` e `impresora_nombre`; expone `impresoraGuardada: Flow<ImpresoraGuardada?>`, `guardarImpresora()`.

**Bluetooth**
- `app/src/main/java/mx/veltrontech/print/bluetooth/ImpresoraGuardada.kt` — `data class ImpresoraGuardada(val nombre: String, val mac: String)`.
- `app/src/main/java/mx/veltrontech/print/bluetooth/DispositivoEmparejado.kt` — `data class DispositivoEmparejado(val nombre: String, val mac: String)`.
- `app/src/main/java/mx/veltrontech/print/bluetooth/PermisosBluetooth.kt` — `fun permisosRequeridos(sdkInt: Int): List<String>` (vacía en API ≤ 30; `BLUETOOTH_CONNECT` en API ≥ 31) y `fun tienePermisos(context): Boolean`.
- `app/src/main/java/mx/veltrontech/print/bluetooth/BluetoothRepository.kt` — `disponible()`, `encendido()`, `dispositivosEmparejados(): List<DispositivoEmparejado>` (ordenada con `ordenarDispositivos()`), `estaEmparejado(mac)`.
- `app/src/main/java/mx/veltrontech/print/bluetooth/ResultadoEnvio.kt` — `sealed interface ResultadoEnvio`: `Exito`, `SinBluetooth`, `BluetoothApagado`, `SinPermiso`, `SinImpresora`, `NoEmparejada`, `NoResponde`, `ConexionPerdida`.
- `app/src/main/java/mx/veltrontech/print/bluetooth/PrinterConnection.kt` — `suspend fun enviar(bytes: ByteArray): ResultadoEnvio` y `suspend fun probar(): ResultadoEnvio` (envía solo `ESC @` = `0x1B 0x40`). Ver "Detalles técnicos".
- `app/src/main/java/mx/veltrontech/print/bluetooth/ElegirImpresoraScreen.kt` y `ElegirImpresoraViewModel.kt`.

**Inicio**
- `app/src/main/java/mx/veltrontech/print/inicio/InicioScreen.kt` y `InicioViewModel.kt` — tarjeta de impresora con estado `EstadoImpresora` (`SinProbar`, `Probando`, `Lista`, `NoResponde`) + hora de la última prueba + mensaje de error.
- `app/src/main/java/mx/veltrontech/print/ui/componentes/TarjetaImpresora.kt` — Composable de la tarjeta.

**Pruebas**
- `app/src/test/java/mx/veltrontech/print/bluetooth/PermisosBluetoothTest.kt` — API 26, 30 → lista vacía; API 31, 36 → `BLUETOOTH_CONNECT`.
- `app/src/test/java/mx/veltrontech/print/bluetooth/OrdenarDispositivosTest.kt` — orden alfabético sin distinguir mayúsculas y "Dispositivo sin nombre" para nombres vacíos o nulos.

> `ordenarDispositivos()` es una función pura (sin Android) para poder probarla con JUnit.
## Archivos a modificar

- `app/build.gradle.kts` — dependencias nuevas; `minSdk = 26`.
- `app/src/main/AndroidManifest.xml`:
  ```xml
  <uses-feature android:name="android.hardware.bluetooth" android:required="true" />
  <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
  ```
  Registrar `android:name=".VeltronPrintApp"` en `<application>`. Sin permiso `INTERNET` y sin bloqueo de orientación.
- `app/src/main/java/mx/veltrontech/print/MainActivity.kt` — `enableEdgeToEdge()`, `VeltronPrintTheme { VeltronNavHost() }`.
- `app/src/main/res/values/strings.xml` — todos los textos y mensajes exactos del spec.

## Modelo de datos

DataStore Preferences (archivo `ajustes`):

```
ajustes
├── impresora_mac     (String)  ej. "86:67:7A:12:34:56"
└── impresora_nombre  (String)  ej. "POS-8390L"
```

No hay base de datos Room en este módulo.

## Detalles técnicos

**Conexión (`PrinterConnection`)**
1. Un `Mutex` garantiza una sola conexión a la vez.
2. Antes de conectar, valida en este orden: hay Bluetooth → tiene permiso → está encendido → hay impresora guardada → sigue emparejada. Cada falla devuelve su `ResultadoEnvio`.
3. Crea el socket con `device.createRfcommSocketToServiceRecord(UUID_SPP)` y llama a `connect()` en `Dispatchers.IO`.
4. Si `connect()` lanza `IOException`, intenta una sola vez el método alterno por reflexión `createRfcommSocket(1)` (canal 1). Muchas impresoras genéricas solo aceptan esta forma.
5. `connect()` es bloqueante y no responde a la cancelación de corrutinas: el tiempo límite de 10 s se implementa con una corrutina vigilante que cierra el socket al vencer, lo que hace que `connect()` lance la excepción y se devuelva `NoResponde`.
6. Escribe los bytes en el `outputStream`, hace `flush()` y espera 300 ms antes de cerrar para que el búfer termine de salir (algunas impresoras pierden los últimos bytes si se cierra de inmediato).
7. Cierra el socket siempre en `finally`.
8. Un error de escritura después de conectar devuelve `ConexionPerdida`.

**Permisos (Android 12+)**
- Se piden con `rememberLauncherForActivityResult(RequestMultiplePermissions)`.
- Negado permanente: `shouldShowRequestPermissionRationale` es falso después de una negación → el botón abre `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- Se revisan otra vez al volver a primer plano (`LifecycleResumeEffect`), por si el usuario los dio desde los ajustes.
- No se pide `BLUETOOTH_SCAN`: la app no busca dispositivos, solo usa los emparejados.

**Encender Bluetooth:** `BluetoothAdapter.ACTION_REQUEST_ENABLE` con un launcher de resultado.

**Abrir ajustes de Bluetooth:** `Settings.ACTION_BLUETOOTH_SETTINGS`.

**Pantallas adaptables:** se decide horizontal o vertical con el ancho disponible (`BoxWithConstraints` o `currentWindowAdaptiveInfo`). La tarjeta de Inicio tiene un ancho máximo de 560 dp.

## Estado en los ViewModel

- `InicioViewModel`: `StateFlow<InicioUiState>` con `impresora: ImpresoraGuardada?`, `estado: EstadoImpresora`, `ultimaPrueba: LocalTime?`, `error: ResultadoEnvio?`. El estado de prueba vive solo en memoria (empieza en `SinProbar`).
- `ElegirImpresoraViewModel`: `StateFlow<ElegirUiState>` con `dispositivos: List<DispositivoEmparejado>`, `macActual: String?`, `cargando: Boolean`; función `recargar()` llamada al volver a primer plano.

## Datos falsos para desarrollo

No aplica: la lista sale de los dispositivos emparejados reales y la prueba necesita la impresora física. Para revisar las pantallas sin impresora se usan `@Preview` de Compose con datos de ejemplo (una lista de 3 dispositivos, la lista vacía y cada estado de la tarjeta).

## Dependencias de este módulo

- No depende de ningún módulo; es la base.
- Requiere: el proyecto creado en Android Studio, la impresora emparejada en la tablet y/o el Honor para la QA.
