# Estado del Proyecto — Veltron Print

Última actualización: 2026-10-25

## Estado general

| Módulo | Estado | Notas |
|--------|--------|-------|
| 001 Conexión Bluetooth | En progreso | Código completo, pendiente QA en dispositivo físico |
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

**Pendiente:**
- QA en Samsung Galaxy Tab S10 Lite (Android 16) y Honor X8 (Android 11)
- Verificar los 17 criterios de aceptación de spec.md con la impresora real
- Commit `feat: conexión Bluetooth con la impresora` (pendiente aprobación de Daniel)

## Decisiones técnicas de esta sesión

- Montserrat se usa como fuente variable (`montserrat_variable.ttf`, peso 100–900).
  Los estáticos ya no están en el repo de Google Fonts; la variable cubre todos los pesos.
- `LocalLifecycleOwner` se obtiene via `LocalContext.current as ComponentActivity`
  para evitar dependencia explícita de `lifecycle-runtime-compose`.
- El plugin `kotlin-android` se agregó explícitamente al no estar en la plantilla original
  (AGP 9.4.1 no lo aplica implícitamente en este template).

## Próximos pasos

1. Daniel hace QA en dispositivo físico con la impresora.
2. Si todo pasa → Claude Code hace el commit del módulo 001.
3. Iniciar módulo 002 (Motor ESC/POS).
