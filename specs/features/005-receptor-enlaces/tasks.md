# Tasks — Módulo 005: Receptor de enlaces

> Checklist de tareas pequeñas y verificables. Se marcan según pasan el QA (Paso 6 del método).

## Construcción

- [x] Agregar kotlinx.serialization (plugin + librería JSON) a `libs.versions.toml` y a los `build.gradle.kts`
- [x] Crear `contrato/ResultadosContrato.kt`
- [x] Crear `contrato/DecodificadorEnlace.kt`
- [x] Crear `contrato/ValidadorTrabajo.kt` (tipos estrictos, claves desconocidas ignoradas, primer error con detalle exacto)
- [x] Crear `recepcion/RecibirTrabajoViewModel.kt` (estados, reintento con el mismo trabajo, cierre a los 2 s)
- [x] Crear `recepcion/RecibirTrabajoScreen.kt`
- [x] Crear `recepcion/RecibirTrabajoActivity.kt` (procesa el enlace solo si `savedInstanceState == null`; `finishAndRemoveTask()` al terminar)
- [x] Declarar la Activity y su `intent-filter` en `AndroidManifest.xml`
- [x] "Vincular impresora" abre `MainActivity` directo en "Elegir impresora"
- [x] Agregar mensajes y textos a `strings.xml`
- [x] Copiar el JSON del ticket oficial a `app/src/test/resources/ticket_oficial.json`
- [x] Crear `DecodificadorEnlaceTest.kt` y `ValidadorTrabajoTest.kt`
- [x] Confirmar que `herramientas/prueba-print.html` está en el repositorio
- [x] Agregar `recepcion/` y `herramientas/` a la estructura del CLAUDE.md
- [x] `./gradlew test` pasa
- [x] `./gradlew assembleDebug` pasa

## Preparación de la QA (Daniel)

- [x] Subir `herramientas/prueba-print.html` por el Administrador de archivos de cPanel a la carpeta pública de register.veltrontech.mx
- [x] Abrir `https://register.veltrontech.mx/prueba-print.html` en Chrome de la tablet

## Verificación (copiado de los criterios de aceptación de spec.md)

- [x] Desde la página de pruebas en Chrome, el botón del ticket oficial abre Veltron Print y lo imprime sin pedir confirmación.
- [x] El ticket impreso desde el enlace es idéntico al ticket oficial de `contrato-bloques.md` sección 6 (con logo y QR reales).
- [x] La pantalla "Imprimiendo" muestra "Registro · MZT-OCT26-0042".
- [x] Al terminar se muestra "Ticket impreso" y a los 2 segundos se regresa a Chrome solo.
- [x] Después de imprimir, Veltron Print no queda como pantalla suelta en la lista de apps recientes.
- [x] Funciona igual con la forma `intent://` y con la forma corta `veltronprint://` (casilla de la página de pruebas).
- [x] Los acentos, la ñ y los símbolos del ticket con acentos salen correctos; los emojis se omiten.
- [x] Un trabajo con `copias: 2` imprime dos tickets, cada uno con su corte.
- [x] Girar la tablet mientras imprime no provoca una segunda impresión.
- [x] Tocar dos botones de impresión seguidos imprime los dos tickets, uno después del otro.
- [x] Con la impresora apagada aparece el error con "Reintentar"; al encenderla y tocar "Reintentar" se imprime el ticket.
- [x] Cada caso de error de la página de pruebas muestra su mensaje exacto y no imprime nada.
- [x] Los errores de formato muestran el detalle técnico correcto (número de bloque, tipo y problema).
- [x] Sin impresora vinculada, "Vincular impresora" abre la pantalla "Elegir impresora".
- [x] Si la app estaba abierta en Ajustes, después de imprimir por enlace sigue en Ajustes.
- [x] Las pruebas JUnit del decodificador y del validador pasan, incluida la del ticket oficial completo.

## Verificación de casos de error (de la sección 5 de spec.md, con la página de pruebas)

- [x] "Sin datos" → "El enlace no trae ningún ticket para imprimir." · solo Cerrar
- [x] "Datos dañados" y "JSON roto" → "El ticket llegó dañado y no se puede leer. Vuelve a imprimirlo desde Registro." · solo Cerrar
- [x] "Versión 2" → "Este ticket usa una versión del formato que Veltron Print no conoce (v2). Actualiza la app." · solo Cerrar
- [x] "Sin bloques", "Copias fuera de rango", "Bloque desconocido", "Falta campo obligatorio", "Valor fuera de rango", "Número como texto", "Booleano como texto", "Código de barras largo", "Código de barras con acento", "QR demasiado largo", "Separador inválido" y "Bloque interno" → "El ticket tiene un error de formato y no se imprimió." + detalle técnico correcto · solo Cerrar
- [x] Impresora apagada → mensaje de "No responde" del módulo 001 · Reintentar y Cerrar
- [x] Sin impresora vinculada → "No hay ninguna impresora vinculada." · Vincular impresora y Cerrar
- [x] "Campo extra desconocido" imprime normal (no es error)

## Verificación visual

- [x] Los tres estados de "Imprimiendo" se ven bien en la tablet en horizontal y en vertical
- [x] Se ven bien en el Honor X8
- [x] Modo claro y modo oscuro se ven correctamente

## Cierre

- [x] Todos los criterios de arriba pasan
- [x] Commit hecho (`feat: receptor de enlaces veltronprint://`)
- [x] ESTADO_PROYECTO.md actualizado
