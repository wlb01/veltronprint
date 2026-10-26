# Contrato de bloques — Veltron Print · versión 1

> Fuente única de verdad del formato con el que cualquier sistema (Registro hoy) le pide a Veltron Print que imprima. Vive en el repositorio de Veltron Print; el `CLAUDE.md` de Registro apunta a una copia de este archivo. Cualquier cambio de formato se hace **primero aquí** y sube el número de versión.

## 1. Cómo se envía un trabajo

Registro abre un enlace que Android entrega a Veltron Print. Desde Chrome se usa la sintaxis `intent://`, que es la más confiable y abre directamente la app:

```
intent://imprimir?d=<DATOS>#Intent;scheme=veltronprint;package=mx.veltrontech.print;end
```

La forma corta equivalente, válida desde cualquier otra app, es:

```
veltronprint://imprimir?d=<DATOS>
```

`<DATOS>` es el JSON del trabajo (sección 2), codificado en UTF-8 y después en **Base64 URL-safe** (caracteres `-` y `_`, sin relleno `=`).

Ejemplo en JavaScript:

```javascript
function abrirVeltronPrint(trabajo) {
  const bytes = new TextEncoder().encode(JSON.stringify(trabajo))
  let bin = ''
  bytes.forEach(b => { bin += String.fromCharCode(b) })
  const d = btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  window.location.href =
    `intent://imprimir?d=${d}#Intent;scheme=veltronprint;package=mx.veltrontech.print;end`
}
```

> El enlace debe abrirse como respuesta directa a un toque del usuario (un botón), o Chrome puede bloquearlo.

## 2. Estructura del trabajo

```json
{
  "v": 1,
  "tipo": "registro",
  "folio": "MZT-OCT26-0042",
  "copias": 1,
  "bloques": [ ... ]
}
```

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `v` | entero | Sí | Versión del contrato. Esta versión es `1` |
| `tipo` | texto | Sí | Etiqueta del ticket: `registro`, `compra`, `garantia`, `cobro`, `venta`, `prueba` u otro. Se usa en el historial |
| `folio` | texto | No | Folio o referencia; es lo que se busca en el historial |
| `copias` | entero 1–3 | No (default 1) | Número de copias; cada copia termina con su propio corte si hay bloque `corte` |
| `bloques` | arreglo | Sí, mínimo 1 | Lista ordenada de bloques (sección 3) |

## 3. Tipos de bloque

Todos los bloques tienen el campo `tipo`. Los demás campos son opcionales salvo que se indique.

### `texto`
| Campo | Valores | Default |
|---|---|---|
| `valor` (obligatorio) | Texto; `\n` hace salto de línea | — |
| `alinear` | `izquierda` · `centro` · `derecha` | `izquierda` |
| `negrita` | `true` · `false` | `false` |
| `subrayado` | `true` · `false` | `false` |
| `tamano` | `1` · `2` · `3` (multiplica alto y ancho) | `1` |
| `fuente` | `normal` (48 col.) · `compacta` (64 col.) | `normal` |

La app corta las líneas largas por palabra según las columnas disponibles (columnas de la fuente ÷ `tamano`).

### `par`
Texto a la izquierda y a la derecha en la misma línea, relleno con espacios. Ideal para totales.

| Campo | Valores | Default |
|---|---|---|
| `izquierda` (obligatorio) | Texto | — |
| `derecha` (obligatorio) | Texto | — |
| `negrita` | `true` · `false` | `false` |
| `fuente` | `normal` · `compacta` | `normal` |

Si no caben en una línea, `izquierda` se corta por palabra y `derecha` queda alineada en la última línea.

### `separador`
| Campo | Valores | Default |
|---|---|---|
| `caracter` | Un solo carácter | `-` |

Ocupa el ancho completo de la línea en fuente normal.

### `espacio`
| Campo | Valores | Default |
|---|---|---|
| `lineas` | 1–5 | `1` |

### `logo`
| Campo | Valores | Default |
|---|---|---|
| `alinear` | `izquierda` · `centro` · `derecha` | `centro` |

Imprime el logo guardado en los Ajustes de la app. Si no hay logo cargado, el bloque se omite y el trabajo continúa.

### `qr`
| Campo | Valores | Default |
|---|---|---|
| `valor` (obligatorio) | Texto o URL, máximo 500 caracteres | — |
| `tamano` | `chico` · `mediano` · `grande` | `mediano` |
| `alinear` | `izquierda` · `centro` · `derecha` | `centro` |

### `codigo_barras`
| Campo | Valores | Default |
|---|---|---|
| `valor` (obligatorio) | Texto ASCII | — |
| `formato` | `CODE128` | `CODE128` |
| `mostrar_texto` | `true` · `false` | `true` |
| `alinear` | `izquierda` · `centro` · `derecha` | `centro` |

### `corte`
| Campo | Valores | Default |
|---|---|---|
| `modo` | `total` · `parcial` | El configurado en Ajustes |

La app avanza el papel lo necesario para que el corte no se coma la última línea.

### `cajon`
Sin campos. Envía el pulso para abrir el cajón de dinero.

## 4. Reglas de validación

- Si `v` no es `1`, el trabajo se rechaza completo.
- Si falta un campo obligatorio o un bloque tiene un `tipo` desconocido, el trabajo se rechaza completo. Nunca se imprime un ticket a medias.
- Los valores fuera de rango (por ejemplo `tamano: 7`) rechazan el trabajo.
- Los acentos y la ñ se convierten a la tabla de caracteres elegida en Ajustes. Un carácter que la tabla no puede representar se sustituye por su equivalente sin acento o por `?`.
- Los emojis no se imprimen; se eliminan.
- No hay respuesta de vuelta hacia Registro: el resultado de la impresión se ve en la app.

## 5. Bloques que no existen en la versión 1

- **Imagen arbitraria (`imagen`):** una imagen en Base64 puede rebasar el tamaño que Android permite pasar en un enlace. El logo cubre la necesidad actual. Se evaluó para imprimir la firma del cliente y se descartó: la firma es evidencia para el taller y la resguarda Registro, no un papel que se lleva el cliente. Si en el futuro se necesitan otras imágenes, se diseñará en una versión 2 del contrato.

## 6. Ticket oficial de prueba — comprobante de registro

Este es el ticket de referencia que definió Daniel. Se usa como dato de prueba en los módulos 002 a 005 y como modelo para la feature de Registro.

### Vista previa (80 mm, 48 columnas)

```
                [ LOGO VELTRON ]
          WhatsApp: +52 669 327 2721
  Av. Francisco Solís 2599-Loc 16b, Plaza Bicentenario,
         Francisco Villa, 82127 Mazatlán, Sin.
          L-V 10:00-20:00 | Sáb 10:00-17:00
------------------------------------------------
Fecha de emisión:               25/10/2026 20:16
Folio:                            MZT-OCT26-0042
No. de cliente:                            #0158
Cliente:                        Juan Pérez López
Teléfono:                           669 123 4567
Atendido por:                             Daniel
------------------------------------------------
DATOS DEL EQUIPO
Marca:                                     Apple
Modelo:                                iPhone 12
IMEI/SN:                         356789012345678
------------------------------------------------
FALLA REPORTADA
No carga, se calienta al conectar el cargador
------------------------------------------------
Presupuesto cotizado:                    $890.00
Anticipo:                                $400.00
RESTANTE:                                $490.00
------------------------------------------------
[X] SIM y MicroSD devueltos al cliente
------------------------------------------------
         Puedes consultar los T&C aquí:
                     [ QR ]
------------------------------------------------
  Firma de conformidad registrada digitalmente
------------------------------------------------
    Debe presentar este ticket para recoger su equipo
              Gracias por su confianza
                 www.veltrontech.mx
```

> Las líneas en fuente compacta (datos del negocio y pie) se ven más anchas en esta vista porque usan 64 columnas en lugar de 48.

### JSON

```json
{
  "v": 1,
  "tipo": "registro",
  "folio": "MZT-OCT26-0042",
  "bloques": [
    { "tipo": "logo" },
    { "tipo": "texto", "valor": "WhatsApp: +52 669 327 2721", "alinear": "centro", "fuente": "compacta" },
    { "tipo": "texto", "valor": "Av. Francisco Solís 2599-Loc 16b, Plaza Bicentenario, Francisco Villa, 82127 Mazatlán, Sin.", "alinear": "centro", "fuente": "compacta" },
    { "tipo": "texto", "valor": "L-V 10:00-20:00 | Sáb 10:00-17:00", "alinear": "centro", "fuente": "compacta" },
    { "tipo": "separador" },
    { "tipo": "par", "izquierda": "Fecha de emisión:", "derecha": "25/10/2026 20:16" },
    { "tipo": "par", "izquierda": "Folio:", "derecha": "MZT-OCT26-0042", "negrita": true },
    { "tipo": "par", "izquierda": "No. de cliente:", "derecha": "#0158" },
    { "tipo": "par", "izquierda": "Cliente:", "derecha": "Juan Pérez López" },
    { "tipo": "par", "izquierda": "Teléfono:", "derecha": "669 123 4567" },
    { "tipo": "par", "izquierda": "Atendido por:", "derecha": "Daniel" },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "DATOS DEL EQUIPO", "negrita": true },
    { "tipo": "par", "izquierda": "Marca:", "derecha": "Apple" },
    { "tipo": "par", "izquierda": "Modelo:", "derecha": "iPhone 12" },
    { "tipo": "par", "izquierda": "IMEI/SN:", "derecha": "356789012345678" },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "FALLA REPORTADA", "negrita": true },
    { "tipo": "texto", "valor": "No carga, se calienta al conectar el cargador" },
    { "tipo": "separador" },
    { "tipo": "par", "izquierda": "Presupuesto cotizado:", "derecha": "$890.00" },
    { "tipo": "par", "izquierda": "Anticipo:", "derecha": "$400.00" },
    { "tipo": "par", "izquierda": "RESTANTE:", "derecha": "$490.00", "negrita": true },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "[X] SIM y MicroSD devueltos al cliente" },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "Puedes consultar los T&C aquí:", "alinear": "centro" },
    { "tipo": "qr", "valor": "https://veltrontech.mx/terminos" },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "Firma de conformidad registrada digitalmente", "alinear": "centro" },
    { "tipo": "separador" },
    { "tipo": "texto", "valor": "Debe presentar este ticket para recoger su equipo\nGracias por su confianza\nwww.veltrontech.mx", "alinear": "centro", "fuente": "compacta" },
    { "tipo": "corte" }
  ]
}
```

### Reglas de contenido (las aplica Registro al armar los bloques)

| Línea | Regla |
|---|---|
| `IMEI/SN` | Se omite si el campo `imei_sn` está vacío o es `null` |
| `No. de cliente` | Se omite si el cliente no tiene número de cliente |
| SIM y accesorios | Siempre se imprime marcada, porque `accesorios_confirmados` es obligatoria para guardar la orden. Se usa `[X]`, no `☑`, porque la impresora no tiene ese símbolo |
| QR de T&C | URL temporal `https://veltrontech.mx/terminos` hasta que exista la página real; se cambia solo en Registro |
| Firma | No se imprime la imagen; solo la leyenda de que quedó registrada digitalmente |

## 7. Impacto en Registro

Registro necesita una feature propia (en su repositorio) para:

1. Reemplazar `generarTextoTicket()` (texto plano de 32 columnas, pensado para 58 mm) por una función que convierta `orden + ticket_config` en el arreglo de bloques de este contrato, siguiendo las reglas de contenido de la sección 6. El ajuste al ancho real lo hace la app.
2. Reemplazar la descarga del `.txt` por `abrirVeltronPrint(trabajo)` en los botones de `PantallaConfirmacion`, `DetalleOrdenPage` y la prueba de `ConfiguracionPage`.
3. Descartar el plan anterior de generar ESC/POS en el navegador con `@point-of-sale/receipt-printer-encoder`.
4. Mantener la migración pendiente en producción: `ALTER TABLE configuracion_empresa ADD COLUMN ticket_config JSON NULL AFTER mensaje_whatsapp;`.
5. Más adelante, los recibos de Cobranza (cobro y venta de artículo) y las garantías pueden usar el mismo contrato sin cambios en la app.
