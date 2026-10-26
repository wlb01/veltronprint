# Estado del Proyecto — Veltron Print

Última actualización: 2026-10-25

## Estado general

| Módulo | Estado | Notas |
|--------|--------|-------|
| 001 Conexión Bluetooth | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 002 Motor ESC/POS | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 003–009 | Sin iniciar | — |

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

**QA aprobado** — 2026-10-25. Ambas tablas (CP850 y WIN1252) salieron correctas; se usa CP850 como predeterminada.
Commit `feat: motor ESC/POS e impresión de prueba` hecho.

**Pendiente de hardware futuro:**
- El bloque `cajon` abre el cajón de dinero (cuando Daniel lo tenga; cubierto por JUnit).

## Módulo 001 — Conexión Bluetooth

**Completado (código):**
- Infraestructura base: tema Veltron, tipografía Montserrat (fuente variable), colores exactos
- Navegación: NavHost con rutas `inicio` y `elegir_impresora`
- DataStore: guardado y lectura de impresora vinculada (nombre + MAC)
- Bluetooth: permisos, lista de emparejados, conexión RFCOMM con fallback por reflexión
- PrinterConnection: mutex, watchdog 10 s, delay 300 ms tras escritura, cierre en `finally`
- Pantallas: Inicio (tarjeta con 4 estados), ElegirImpresora (lista adaptable horizontal/vertical)
- Tests JUnit: PermisosBluetoothTest, OrdenarDispositivosTest

**QA aprobado** — 2026-10-25. Commit `feat: conexión Bluetooth con la impresora` hecho.

## Decisiones técnicas

- Montserrat se usa como fuente variable (`montserrat_variable.ttf`, peso 100–900).
  Los estáticos ya no están en el repo de Google Fonts; la variable cubre todos los pesos.
- `LocalLifecycleOwner` se obtiene via `LocalContext.current as ComponentActivity`
  para evitar dependencia explícita de `lifecycle-runtime-compose`.
- El plugin `kotlin-android` NO se declara explícitamente: AGP 9.4.1 lo aplica automáticamente.
- `TablaCaracteres`: mapa propio en lugar de `Charset.forName("IBM850")` porque no todas las
  versiones de Android incluyen esa codificación y el mapa es testable con JUnit.
- `BloquePruebaTabla` vive en `contrato/` (mismo paquete que el sealed interface `Bloque`)
  pero es interno; se elimina en el módulo 008.

## Próximos pasos

1. Iniciar módulo 003 (Conversión de imagen a 1 bit y logo).
