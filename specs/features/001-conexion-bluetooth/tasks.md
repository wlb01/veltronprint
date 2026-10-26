# Tasks — Módulo 001: Conexión Bluetooth

> Checklist de tareas pequeñas y verificables. Se marcan según pasan el QA (Paso 6 del método).

## Construcción

**Base**
- [ ] Agregar dependencias a `libs.versions.toml` y `app/build.gradle.kts`
- [ ] Configurar permisos y `uses-feature` en `AndroidManifest.xml`; registrar `VeltronPrintApp`
- [ ] Crear `VeltronPrintApp.kt` y `AppContainer.kt`
- [ ] Agregar fuentes Montserrat a `res/font/`
- [ ] Crear `Color.kt`, `Type.kt` y `Theme.kt` con los colores exactos del CLAUDE.md
- [ ] Crear `Rutas.kt` y `VeltronNavHost.kt`
- [ ] Actualizar `MainActivity.kt` (edge-to-edge + tema + navegación)
- [ ] Cargar todos los textos del spec en `strings.xml`

**Datos y Bluetooth**
- [ ] Crear `PreferenciasRepository.kt` (guardar y leer la impresora)
- [ ] Crear `ImpresoraGuardada.kt`, `DispositivoEmparejado.kt` y `ResultadoEnvio.kt`
- [ ] Crear `PermisosBluetooth.kt`
- [ ] Crear `BluetoothRepository.kt` con `ordenarDispositivos()`
- [ ] Crear `PrinterConnection.kt` (mutex, tiempo límite de 10 s, método alterno de canal 1, cierre en `finally`)

**Pantallas**
- [ ] Crear `AvisoPermiso.kt`
- [ ] Crear `TarjetaImpresora.kt` con sus 4 estados
- [ ] Crear `InicioScreen.kt` e `InicioViewModel.kt`
- [ ] Crear `ElegirImpresoraScreen.kt` y `ElegirImpresoraViewModel.kt`
- [ ] Agregar `@Preview` de cada estado de las pantallas

**Pruebas y cierre técnico**
- [ ] Crear `PermisosBluetoothTest.kt` y `OrdenarDispositivosTest.kt`
- [ ] `./gradlew test` pasa
- [ ] `./gradlew assembleDebug` pasa

## Verificación (copiado de los criterios de aceptación de spec.md)

- [ ] Al abrir la app sin impresora guardada, Inicio muestra "No hay impresora vinculada" y el botón "Vincular impresora".
- [ ] En Android 12 o superior sin permiso, la app muestra el Aviso de permiso y no la tarjeta de la impresora.
- [ ] Tras negar el permiso de forma permanente, el botón del aviso dice "Abrir ajustes de la app" y abre los ajustes de Veltron Print.
- [ ] En Android 11 (Honor X8) la app no pide ningún permiso y funciona igual.
- [ ] Con Bluetooth apagado, tocar "Vincular impresora" muestra el diálogo del sistema para encenderlo.
- [ ] "Elegir impresora" lista todos los dispositivos emparejados, en orden alfabético, con nombre y MAC.
- [ ] Sin dispositivos emparejados, se muestra "No hay dispositivos emparejados." con el botón "Abrir ajustes de Bluetooth".
- [ ] "Abrir ajustes de Bluetooth" abre los ajustes de Bluetooth de Android.
- [ ] Al regresar de los ajustes de Android, la lista incluye el dispositivo recién emparejado sin reiniciar la app.
- [ ] Al tocar la impresora, la app regresa a Inicio mostrando su nombre, su MAC y "Sin probar".
- [ ] La impresora guardada sigue apareciendo después de cerrar la app por completo y volverla a abrir.
- [ ] Con la impresora encendida, "Probar conexión" muestra "Lista" y la hora de la prueba.
- [ ] "Probar conexión" no imprime nada ni avanza el papel.
- [ ] Con la impresora apagada, "Probar conexión" muestra "No responde" y su mensaje exacto en máximo 10 segundos.
- [ ] Mientras dice "Probando…", el botón "Probar conexión" está deshabilitado.
- [ ] Después de una prueba, la impresora acepta inmediatamente una conexión desde otro equipo (la app no la deja ocupada).
- [ ] Si la impresora se desempareja en Android, "Probar conexión" muestra el mensaje de "Ya no emparejada".
- [ ] "Cambiar impresora" muestra la impresora actual marcada como "Actual" y permite reemplazarla.

## Verificación de casos de error (de la sección 5 de spec.md)

- [ ] Bluetooth apagado (rechazar el diálogo) muestra: "Bluetooth está apagado. Enciéndelo para usar la impresora."
- [ ] Permiso negado muestra: "Veltron Print necesita el permiso de Dispositivos cercanos para conectarse a la impresora."
- [ ] Impresora apagada muestra: "La impresora no responde. Verifica que esté encendida, cerca del equipo y que ningún otro dispositivo esté conectado a ella."
- [ ] Impresora desemparejada muestra: "La impresora guardada ya no está emparejada. Vuelve a emparejarla o elige otra."
- [ ] "Sin Bluetooth" y "Conexión perdida" tienen su texto exacto en `strings.xml` (no se pueden provocar en QA manual con estos equipos)

## Verificación visual

- [ ] Galaxy Tab S10 Lite en horizontal se ve correctamente
- [ ] Galaxy Tab S10 Lite en vertical se ve correctamente
- [ ] Honor X8 en vertical y en horizontal se ve correctamente
- [ ] Modo claro y modo oscuro se ven correctamente
- [ ] Ningún contenido queda debajo de las barras del sistema
- [ ] Colores exactos del CLAUDE.md y tipografía Montserrat
- [ ] La tarjeta muestra los 4 estados: Sin probar, Probando…, Lista, No responde

## Cierre

- [ ] Todos los criterios de arriba pasan
- [ ] Commit hecho (`feat: conexión Bluetooth con la impresora`)
- [ ] ESTADO_PROYECTO.md actualizado
