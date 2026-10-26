# Spec — Módulo 002: Motor ESC/POS

> El CONTRATO del módulo. Describe comportamiento, NO implementación (eso va en plan.md).

## 1. Objetivo

Convertir un trabajo formado por bloques del contrato (texto, par, separador, espacio, corte y cajón) en comandos ESC/POS e imprimirlo con el servicio del módulo 001, con alineación, estilos, ajuste de línea, acentos y corte parcial correctos. Permitir probarlo con un botón temporal que imprime el ticket oficial de registro.

## 2. Pantallas

### Inicio (se agrega una tarjeta)

Debajo de la tarjeta "Impresora" del módulo 001 aparece la tarjeta **"Prueba de impresión"**:
- Título: "Prueba de impresión".
- Texto: "Imprime el ticket oficial de registro con datos de ejemplo y una prueba de acentos."
- Botón principal: "Imprimir ticket de prueba".
  - Deshabilitado si no hay impresora guardada.
  - Mientras se imprime, muestra "Imprimiendo…" con indicador de progreso y queda deshabilitado.
- Al terminar con éxito: aviso breve (snackbar) "Ticket enviado a la impresora."
- Al fallar: el mensaje exacto del error del módulo 001 aparece en la tarjeta "Impresora" y su estado cambia a "No responde".

**Orientación:** en horizontal, las dos tarjetas quedan lado a lado; en vertical, una debajo de la otra.

> Esta tarjeta es temporal: en el módulo 008 la impresión de prueba se mueve a Ajustes y la tarjeta desaparece de Inicio.

### Ticket impreso de prueba

Es el ticket oficial de `contrato-bloques.md` (sección 6) con estos cambios temporales:
- El bloque `logo` imprime el texto `[LOGO]` centrado (hasta el módulo 003).
- El bloque `qr` imprime el texto `[QR]` centrado (hasta el módulo 004).
- Antes del corte, agrega esta sección de prueba de acentos:

```
------------------------------------------------
PRUEBA DE ACENTOS
CP850:   áéíóú ÁÉÍÓÚ ñÑ üÜ ¿¡ $ °
WIN1252: áéíóú ÁÉÍÓÚ ñÑ üÜ ¿¡ $ °
```

Cada una de las dos líneas se imprime con su propia tabla de caracteres; el resto del ticket usa la tabla predeterminada.

## 3. Flujo del usuario

**Flujo principal — imprimir el ticket de prueba**
1. Usuario toca "Imprimir ticket de prueba" →
2. Sistema cambia el botón a "Imprimiendo…" y lo deshabilita →
3. Sistema convierte el ticket a ESC/POS y lo envía a la impresora →
4. Impresora imprime el ticket y hace un corte parcial →
5. Sistema muestra "Ticket enviado a la impresora." y el botón vuelve a su estado normal.

**Flujo — elegir la tabla de caracteres (una sola vez, durante la QA)**
1. Daniel revisa la sección "PRUEBA DE ACENTOS" del ticket impreso →
2. Daniel indica cuál de las dos líneas salió correcta →
3. Claude Code fija esa tabla como predeterminada →
4. Daniel imprime de nuevo y los acentos del cuerpo del ticket salen correctos.

**Flujo — error al imprimir**
1. Usuario toca "Imprimir ticket de prueba" con la impresora apagada →
2. Sistema intenta conectar durante un máximo de 10 segundos →
3. Sistema muestra el mensaje de error en la tarjeta "Impresora", cambia su estado a "No responde" y el botón vuelve a su estado normal.

## 4. Lógica y reglas

**Generales**
- Todo trabajo empieza reiniciando la impresora y seleccionando la tabla de caracteres predeterminada.
- Después de cada bloque, los estilos vuelven a los valores por defecto: alineación izquierda, sin negrita, sin subrayado, tamaño 1, fuente normal.
- El trabajo completo se envía en un solo envío al servicio del módulo 001.
- Si el trabajo tiene `copias` mayor a 1, la secuencia completa de bloques se repite ese número de veces, en el mismo envío.

**Columnas**
- La fuente normal tiene 48 columnas y la compacta 64.
- Las columnas disponibles de un bloque `texto` son las de su fuente divididas entre su `tamano` (redondeado hacia abajo): normal ×2 = 24, normal ×3 = 16, compacta ×2 = 32, compacta ×3 = 21.

**Bloque `texto`**
- Respeta los saltos de línea `\n` del valor.
- Cada línea que excede las columnas disponibles se corta por palabra; una palabra más larga que una línea completa se corta a la mitad.
- Los espacios al inicio y al final de cada línea cortada se eliminan.
- La alineación, la negrita, el subrayado, el tamaño y la fuente se aplican a todas sus líneas.

**Bloque `par`**
- Imprime `izquierda` pegado al margen izquierdo y `derecha` pegado al margen derecho, rellenando con espacios.
- Entre ambos queda al menos un espacio.
- Si no caben en una línea, `izquierda` se corta por palabra en el ancho que deja libre `derecha` más un espacio, y `derecha` se imprime alineado a la derecha en la última línea.
- Si `derecha` por sí solo no cabe junto con un espacio, `izquierda` se imprime completo y `derecha` va en la línea siguiente alineado a la derecha.
- La negrita aplica a ambos lados.

**Bloque `separador`**
- Repite su carácter 48 veces, en fuente normal, ocupando exactamente una línea.

**Bloque `espacio`**
- Avanza el número de líneas en blanco indicado.

**Bloque `corte`**
- Avanza el papel lo necesario para que la cuchilla no corte la última línea impresa y después corta.
- Sin `modo`, usa el corte predeterminado: **parcial**.

**Bloque `cajon`**
- Envía el pulso de apertura del cajón de dinero; no imprime nada.

**Bloques `logo`, `qr` y `codigo_barras` (temporal)**
- `logo` imprime `[LOGO]`, `qr` imprime `[QR]` y `codigo_barras` imprime `[CÓDIGO DE BARRAS]`, centrados, hasta que existan los módulos 003 y 004.

**Acentos y caracteres especiales**
- Las tablas soportadas son CP850 y Windows-1252.
- Se convierten a la tabla activa: á é í ó ú Á É Í Ó Ú ñ Ñ ü Ü ¿ ¡ °.
- Cualquier otro carácter fuera de ASCII se reemplaza por su letra sin acento si la tiene (por ejemplo, à → a); si no la tiene, por `?`.
- Los emojis se eliminan.
- Antes de la QA, la tabla predeterminada es CP850; después de la QA queda la que Daniel elija.

## 5. Casos de error

| Error | Cuándo ocurre | Mensaje exacto al usuario |
|-------|---------------|----------------------------|
| Errores de conexión | Cualquier resultado de error del servicio del módulo 001 | El mensaje exacto definido en la sección 5 del spec del módulo 001 |

Este módulo no agrega mensajes de error nuevos.

## 6. Criterios de aceptación

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
- [ ] Con la tabla elegida como predeterminada, todos los acentos y la ñ del cuerpo del ticket salen correctos.
- [ ] El papel se corta de forma parcial (queda una pestaña que lo une al rollo).
- [ ] El corte no se come ninguna línea impresa.
- [ ] No sale papel en blanco de más después del corte (más allá del avance necesario para cortar).
- [ ] Se pueden imprimir dos tickets seguidos sin reiniciar la app ni la impresora.
- [ ] La segunda impresión sale idéntica a la primera (ningún estilo se arrastra de una a otra).
- [ ] Con la impresora apagada, el error aparece en la tarjeta "Impresora" en máximo 10 segundos y el botón vuelve a su estado normal.
- [ ] Al imprimir con éxito aparece "Ticket enviado a la impresora."
- [ ] Inicio se ve correctamente con las dos tarjetas en horizontal y en vertical.
- [ ] Las pruebas JUnit del motor pasan (comandos, ajuste de línea, par, acentos, copias y cajón).

## Módulos conectados

- Recibe datos de: 001 (servicio de envío de bytes).
- Entrega datos a: 003 y 004 (que reemplazan los textos temporales de `logo`, `qr` y `codigo_barras`), 005 (que le entrega trabajos recibidos de Registro), 006 (que lo usa para imprimir desde la cola) y 008 (impresión de prueba y tabla de caracteres desde Ajustes).
