# Tasks — Módulo 002: Motor ESC/POS

> Checklist de tareas pequeñas y verificables. Se marcan según pasan el QA (Paso 6 del método).

## Construcción

**Modelo**
- [ ] Crear `contrato/Trabajo.kt` y `contrato/Bloque.kt` con los tipos y valores por defecto del contrato

**Motor**
- [ ] Crear `escpos/ComandosEscPos.kt` con todos los comandos de la tabla del plan, comentados
- [ ] Crear `escpos/TablaCaracteres.kt` con los mapas de CP850 y Windows-1252
- [ ] Crear `escpos/Codificador.kt`
- [ ] Crear `escpos/AjusteLinea.kt` (`cortarPorPalabra` y `componerPar`)
- [ ] Crear `escpos/ConfigImpresion.kt` (CP850 y corte parcial por defecto)
- [ ] Crear `escpos/MotorEscPos.kt` (reinicio, tabla, bloques, restablecer estilos, copias)
- [ ] Crear `escpos/ServicioImpresion.kt`

**Ticket de prueba e interfaz**
- [ ] Crear `ajustes/TicketPrueba.kt` con el ticket oficial + sección "PRUEBA DE ACENTOS"
- [ ] Crear `ui/componentes/TarjetaPruebaImpresion.kt`
- [ ] Actualizar `InicioViewModel.kt` e `InicioScreen.kt` (botón, estado "Imprimiendo…", snackbar, dos tarjetas)
- [ ] Agregar los textos nuevos a `strings.xml`
- [ ] Registrar `MotorEscPos` y `ServicioImpresion` en `AppContainer.kt`

**Pruebas y cierre técnico**
- [ ] Crear `ComandosEscPosTest.kt`, `CodificadorTest.kt`, `AjusteLineaTest.kt` y `MotorEscPosTest.kt`
- [ ] `./gradlew test` pasa
- [ ] `./gradlew assembleDebug` pasa

## Verificación (copiado de los criterios de aceptación de spec.md)

- [ ] Sin impresora guardada, el botón "Imprimir ticket de prueba" está deshabilitado.
- [ ] Mientras imprime, el botón dice "Imprimiendo…" y está deshabilitado.
- [ ] El ticket impreso contiene, en orden, todos los bloques del ticket oficial de `contrato-bloques.md` sección 6.
- [ ] `[LOGO]` y `[QR]` aparecen centrados.
- [ ] Las líneas de datos del negocio, la frase de T&C, la leyenda de la firma y el pie salen centrados.
- [ ] Cada separador ocupa exactamente una línea completa, sin partirse en dos.
- [ ] En cada `par`, la etiqueta queda pegada a la izquierda y el valor pegado a la derecha, y todos los valores terminan en la misma columna.
- [ ] El folio y "RESTANTE" salen en negritas.
- [ ] "DATOS DEL EQUIPO" y "FALLA REPORTADA" salen en negritas.
- [ ] Los datos del negocio y el pie salen en fuente compacta; la dirección se divide en máximo 2 líneas, sin palabras cortadas a la mitad.
- [ ] "Debe presentar este ticket para recoger su equipo" sale en una sola línea.
- [ ] Al final se imprimen las dos líneas de "PRUEBA DE ACENTOS".
- [ ] Al menos una de las dos líneas de prueba muestra correctamente todos sus caracteres.
- [ ] **Daniel indica la tabla correcta → Claude Code la fija en `ConfigImpresion.kt`.**
- [ ] Con la tabla elegida como predeterminada, todos los acentos y la ñ del cuerpo del ticket salen correctos.
- [ ] El papel se corta de forma parcial (queda una pestaña que lo une al rollo).
- [ ] El corte no se come ninguna línea impresa.
- [ ] No sale papel en blanco de más después del corte (más allá del avance necesario para cortar).
- [ ] Se pueden imprimir dos tickets seguidos sin reiniciar la app ni la impresora.
- [ ] La segunda impresión sale idéntica a la primera (ningún estilo se arrastra de una a otra).
- [ ] Al imprimir con éxito aparece "Ticket enviado a la impresora."
- [ ] Las pruebas JUnit del motor pasan (comandos, ajuste de línea, par, acentos, copias y cajón).

## Verificación de casos de error (de la sección 5 de spec.md)

- [ ] Con la impresora apagada, el mensaje "La impresora no responde. Verifica que esté encendida, cerca del equipo y que ningún otro dispositivo esté conectado a ella." aparece en la tarjeta "Impresora" en máximo 10 segundos y el botón vuelve a su estado normal.

## Verificación visual

- [ ] Galaxy Tab S10 Lite en horizontal: las dos tarjetas lado a lado
- [ ] Galaxy Tab S10 Lite en vertical: una tarjeta debajo de la otra
- [ ] Honor X8 en vertical y en horizontal se ve correctamente
- [ ] Modo claro y modo oscuro se ven correctamente

## Pendiente de verificar con hardware futuro

- [ ] El bloque `cajon` abre el cajón de dinero (cuando Daniel lo tenga; mientras tanto, solo lo cubre la prueba JUnit)

## Cierre

- [ ] Todos los criterios de arriba pasan (excepto el pendiente de hardware futuro)
- [ ] Commit hecho (`feat: motor ESC/POS e impresión de prueba`)
- [ ] ESTADO_PROYECTO.md actualizado
