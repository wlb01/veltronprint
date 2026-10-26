# Spec — Módulo 005: Receptor de enlaces

> El CONTRATO del módulo. Describe comportamiento, NO implementación (eso va en plan.md).

## 1. Objetivo

Recibir los enlaces `veltronprint://imprimir?d=...` que abre Registro (o cualquier página), decodificar y validar el trabajo contra `contrato-bloques.md` e imprimirlo de inmediato, regresando solo al navegador cuando termina bien. Si algo falla, mostrar un mensaje claro y, cuando tenga sentido, permitir reintentar.

## 2. Pantallas

### Imprimiendo

Pantalla propia que se abre encima del navegador al recibir un enlace. No muestra la barra de navegación de la app.

**Estado "Imprimiendo"**
- Logo de Veltron Print (ícono de impresora con los colores de la marca).
- Título: "Imprimiendo…"
- Subtítulo: el tipo y el folio del trabajo, por ejemplo "Registro · MZT-OCT26-0042". Si el trabajo no trae folio, solo el tipo. El tipo se muestra con la primera letra en mayúscula.
- Indicador de progreso.

**Estado "Impreso"**
- Ícono de palomita en verde.
- Título: "Ticket impreso"
- Subtítulo: "Regresando…"
- A los 2 segundos la pantalla se cierra sola y el usuario vuelve a la app de donde vino el enlace (Chrome con Registro).

**Estado "Error"**
- Ícono de advertencia en rojo.
- Título: "No se imprimió"
- Mensaje: el texto exacto del error (sección 5).
- Detalle técnico (solo en errores de formato): en texto pequeño y fuente monoespaciada debajo del mensaje, por ejemplo "Bloque 7 (qr): falta el campo valor".
- Botones según el error (sección 5): "Reintentar", "Vincular impresora" y/o "Cerrar".
- Esta pantalla **no** se cierra sola.

**Orientación:** el contenido va centrado con ancho máximo cómodo de lectura, igual en horizontal y en vertical.

## 3. Flujo del usuario

**Flujo principal — imprimir desde Registro**
1. Usuario toca "Imprimir ticket" en Registro →
2. Chrome abre el enlace y Android abre Veltron Print en la pantalla "Imprimiendo" →
3. Sistema decodifica y valida el trabajo →
4. Sistema genera los comandos ESC/POS y los envía a la impresora →
5. Impresora imprime el ticket y lo corta →
6. Sistema muestra "Ticket impreso" →
7. A los 2 segundos, sistema cierra la pantalla y el usuario vuelve a Registro.

**Flujo — la impresora no responde**
1. Pasos 1 a 4 del flujo principal →
2. La conexión falla →
3. Sistema muestra el estado "Error" con el mensaje de la impresora y los botones "Reintentar" y "Cerrar" →
4. Usuario enciende la impresora y toca "Reintentar" →
5. Sistema vuelve al estado "Imprimiendo" con el mismo trabajo y continúa en el paso 5 del flujo principal.

**Flujo — trabajo con error de formato**
1. Llega un enlace con un trabajo inválido →
2. Sistema no imprime nada →
3. Sistema muestra el estado "Error" con el mensaje, el detalle técnico y solo el botón "Cerrar" →
4. Usuario toca "Cerrar" y vuelve a Registro.

**Flujo — no hay impresora vinculada**
1. Llega un enlace válido y no hay impresora guardada →
2. Sistema muestra el estado "Error" con los botones "Vincular impresora" y "Cerrar" →
3. Usuario toca "Vincular impresora" →
4. Sistema abre la app en la pantalla "Elegir impresora" del módulo 001.

## 4. Lógica y reglas

**Recepción**
- La app responde a enlaces con esquema `veltronprint` y destino `imprimir`, tanto en la forma corta `veltronprint://imprimir?d=...` como en la forma `intent://` de Chrome definida en el contrato.
- La app imprime en cuanto recibe el enlace, sin pedir confirmación.
- Cada enlace se procesa una sola vez: girar la tablet, apagar la pantalla o volver a la app no lo vuelve a imprimir.
- Si llega otro enlace mientras uno se está imprimiendo, se imprime después del primero; ninguno se descarta.
- "Reintentar" vuelve a imprimir exactamente el mismo trabajo, sin volver a decodificarlo.

**Decodificación**
- El parámetro `d` se decodifica de Base64 URL-safe (con o sin relleno `=`) y después como texto UTF-8.
- Los acentos y la ñ del JSON llegan intactos al motor.

**Validación** (las reglas exactas están en `contrato-bloques.md`)
- Se valida el trabajo completo antes de generar un solo byte; si algo es inválido, no se imprime nada.
- Se rechaza: `v` distinto de 1; `tipo` ausente o vacío; `copias` fuera de 1–3; `bloques` ausente o vacío; un bloque con `tipo` desconocido; un campo obligatorio ausente; un valor fuera de su rango o lista; un campo con un tipo de dato equivocado; un QR de más de 500 caracteres o vacío; un código de barras vacío, de más de 40 caracteres o con caracteres fuera de ASCII imprimible; un separador cuyo `caracter` no sea exactamente un carácter.
- Los campos que el contrato no define se ignoran.
- El detalle técnico indica el número de bloque (empezando en 1), su tipo y qué está mal, y solo describe el **primer** error encontrado.
- Los bloques internos que no son del contrato (como la prueba de tablas de caracteres) nunca se aceptan desde un enlace.

**Regreso**
- Al terminar con éxito, la app se cierra y no queda en la lista de apps recientes como una pantalla suelta.
- La pantalla "Imprimiendo" no interfiere con la app principal: si Veltron Print estaba abierta en Inicio o en Ajustes, sigue ahí después.

## 5. Casos de error

| Error | Cuándo ocurre | Mensaje exacto al usuario | Botones |
|-------|---------------|----------------------------|---------|
| Sin datos | El enlace no trae el parámetro `d` o viene vacío | "El enlace no trae ningún ticket para imprimir." | Cerrar |
| Datos dañados | `d` no es Base64 válido o el resultado no es JSON | "El ticket llegó dañado y no se puede leer. Vuelve a imprimirlo desde Registro." | Cerrar |
| Versión desconocida | `v` no es 1 | "Este ticket usa una versión del formato que Veltron Print no conoce (v{n}). Actualiza la app." | Cerrar |
| Formato inválido | Cualquier otra regla de validación | "El ticket tiene un error de formato y no se imprimió." + detalle técnico | Cerrar |
| Código inválido | El motor no puede generar un QR o código de barras (segunda red del módulo 004) | "No se pudo generar un código del ticket. Revisa el contenido del QR o del código de barras." | Cerrar |
| Sin impresora | No hay impresora guardada | "No hay ninguna impresora vinculada." | Vincular impresora, Cerrar |
| Errores de conexión | Sin Bluetooth, Bluetooth apagado, permiso negado, no responde, ya no emparejada o conexión perdida | El mensaje exacto del módulo 001 | Reintentar, Cerrar |

`{n}` es el valor de `v` recibido. Ejemplos de detalle técnico:
- "Bloque 7 (qr): falta el campo valor"
- "Bloque 3 (texto): tamano debe ser 1, 2 o 3; llegó 7"
- "Bloque 12: tipo desconocido \"imagen\""
- "Trabajo: copias debe estar entre 1 y 3; llegó 5"

## 6. Criterios de aceptación

- [ ] Desde la página de pruebas en Chrome, el botón del ticket oficial abre Veltron Print y lo imprime sin pedir confirmación.
- [ ] El ticket impreso desde el enlace es idéntico al ticket oficial de `contrato-bloques.md` sección 6 (con logo y QR reales).
- [ ] La pantalla "Imprimiendo" muestra "Registro · MZT-OCT26-0042".
- [ ] Al terminar se muestra "Ticket impreso" y a los 2 segundos se regresa a Chrome solo.
- [ ] Después de imprimir, Veltron Print no queda como pantalla suelta en la lista de apps recientes.
- [ ] Funciona igual con la forma `intent://` y con la forma corta `veltronprint://`.
- [ ] Los acentos, la ñ y los símbolos del ticket con acentos salen correctos; los emojis se omiten.
- [ ] Un trabajo con `copias: 2` imprime dos tickets, cada uno con su corte.
- [ ] Girar la tablet mientras imprime no provoca una segunda impresión.
- [ ] Tocar dos botones de impresión seguidos imprime los dos tickets, uno después del otro.
- [ ] Con la impresora apagada aparece el error con "Reintentar"; al encenderla y tocar "Reintentar" se imprime el ticket.
- [ ] Cada caso de error de la página de pruebas muestra su mensaje exacto y no imprime nada.
- [ ] Los errores de formato muestran el detalle técnico correcto (número de bloque, tipo y problema).
- [ ] Sin impresora vinculada, "Vincular impresora" abre la pantalla "Elegir impresora".
- [ ] Si la app estaba abierta en Ajustes, después de imprimir por enlace sigue en Ajustes.
- [ ] Las pruebas JUnit del decodificador y del validador pasan, incluida la del ticket oficial completo.

## Módulos conectados

- Recibe datos de: Registro (enlaces), 001 (envío y errores de conexión), 002/003/004 (motor y `ResultadoImpresion`).
- Entrega datos a: 006 (que guardará en cola los trabajos que no se pudieron imprimir) y 007 (que registrará los trabajos impresos en el historial).
