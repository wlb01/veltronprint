# Estado del Proyecto — Veltron Print

Última actualización: 2026-10-25

## Estado general

| Módulo | Estado | Notas |
|--------|--------|-------|
| 001 Conexión Bluetooth | ✅ Completo | QA aprobado en Galaxy Tab S10 Lite (Android 16) |
| 002–009 | Sin iniciar | — |

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

## Decisiones técnicas de esta sesión

- Montserrat se usa como fuente variable (`montserrat_variable.ttf`, peso 100–900).
  Los estáticos ya no están en el repo de Google Fonts; la variable cubre todos los pesos.
- `LocalLifecycleOwner` se obtiene via `LocalContext.current as ComponentActivity`
  para evitar dependencia explícita de `lifecycle-runtime-compose`.
- El plugin `kotlin-android` se agregó explícitamente al no estar en la plantilla original
  (AGP 9.4.1 no lo aplica implícitamente en este template).

## Próximos pasos

1. Iniciar módulo 002 (Motor ESC/POS) — bytes, comandos, codificación de texto.
