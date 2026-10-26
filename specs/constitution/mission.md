# Mission — Veltron Print

> Parte 1 de la Spec de Sistema (la "Constitución" del proyecto). Se escribe una sola vez.

## Descripción general

Veltron Print es una app Android nativa que imprime tickets en la impresora térmica de 80 mm de Veltron Tech por Bluetooth clásico (SPP), usando comandos ESC/POS. Reemplaza a RawBT: recibe tickets desde Veltron Tech Registro mediante un enlace `veltronprint://` con los datos en JSON, los interpreta como una lista de bloques y los imprime con logo, centrado, QR, corte automático y apertura de cajón. También permite capturar a mano tickets de compra y garantía mientras Registro no tenga esos módulos.

## Para quién es

- **Negocio:** Veltron Tech, taller de reparación de celulares en Mazatlán, Sinaloa. Uso exclusivo interno.
- **Usuarios:** personal de recepción del taller.
- **Equipos:** Samsung Galaxy Tab S10 Lite (Android 16), dedicada exclusivamente al taller, como equipo principal de recepción. El teléfono Honor X8 (Android 11) queda como respaldo si falla la tablet.
- **Impresora:** Ofichido POS-8390L (genérica), 80 mm, 203 DPI, ancho efectivo 72 mm (576 puntos), Bluetooth clásico sin BLE + USB, comandos ESC/POS, corte automático, puerto para cajón de dinero.

## Alcance de esta versión (V1)

- Vincular una sola impresora Bluetooth, recordarla y reconectar automáticamente.
- Recibir trabajos de impresión desde Registro por enlace `veltronprint://` (formato definido en `contrato-bloques.md`).
- Imprimir los bloques del contrato: texto con alineación, negritas, subrayado y tamaños; pares izquierda/derecha; separadores; espacios; logo; QR; código de barras; corte; apertura de cajón.
- Ajustar el texto automáticamente al ancho real del papel (48 columnas en fuente normal, 64 en compacta).
- Cargar el logo una vez en Ajustes, convertirlo a blanco y negro con vista previa, y guardarlo en la app.
- Cola de impresión: si la impresora no responde, el trabajo se guarda y se reintenta.
- Historial local de 30 días con búsqueda por folio y reimpresión idéntica.
- Ajustes: tabla de caracteres (acentos y ñ), densidad de impresión, modo de corte e impresión de prueba.
- Formulario manual para tickets de compra y garantía.
- Interfaz con la identidad visual de Veltron Tech, en modo claro y oscuro.

## Fuera de alcance V1

| Fuera de alcance | Cuándo entraría |
|---|---|
| Varias impresoras vinculadas | No planeado (una sola impresora en recepción) |
| Aparecer en el menú "Compartir" de Android para imprimir PDF, imágenes o texto de otras apps | Descartado por Daniel |
| Publicación en Play Store o venta a otros técnicos | Descartado — uso exclusivo de Veltron Tech |
| Generar el PDF descargable del QR o la página de tracking | Pertenece a Registro, no a esta app |
| Generar los bloques de compra/garantía desde Registro | Cuando Registro tenga esos módulos; la app no cambia |
| Conexión por USB a la impresora | V2 si se necesita |

## Módulos del sistema

| # | Módulo | Qué hace |
|---|--------|----------|
| 001 | Conexión Bluetooth | Emparejar, elegir la impresora, recordarla y reconectar |
| 002 | Motor ESC/POS | Texto, alineación, tamaños, negritas, acentos, ajuste de línea, corte y cajón |
| 003 | Imágenes y logo | Convertir imágenes a blanco y negro; cargar, previsualizar y guardar el logo |
| 004 | QR y códigos de barras | Generarlos a partir del bloque recibido e imprimirlos |
| 005 | Receptor de enlaces | Abrir `veltronprint://`, validar el JSON contra el contrato y mostrar errores claros |
| 006 | Cola y reintento | Guardar el trabajo si la impresora no responde y reintentarlo |
| 007 | Historial | Registro local de 30 días, búsqueda por folio y reimpresión |
| 008 | Ajustes y prueba | Tabla de caracteres, densidad, modo de corte, logo e impresión de prueba |
| 009 | Tickets manuales | Formulario para capturar tickets de compra y garantía dentro de la app |

## Reglas de negocio

- Registro decide **qué** se imprime; la app decide **cómo** se imprime. La app nunca agrega, quita ni reordena contenido de un trabajo recibido.
- El único contenido que la app genera por sí misma son la impresión de prueba y los tickets manuales del módulo 009.
- Solo existe una impresora vinculada a la vez.
- Todo trabajo usa el ancho fijo de 80 mm (576 puntos).
- Un trabajo que no se pudo imprimir nunca se pierde en silencio: queda en la cola hasta imprimirse o hasta que el usuario lo cancele.
- La reimpresión usa el JSON original del trabajo, por lo que sale idéntica.
- El historial conserva los trabajos 30 días y después se borra automáticamente.
- La app funciona sin internet: todo lo necesario llega en el enlace o vive en la tablet.
- El logo vive en la app, no viaja en cada enlace.
- El formato de los enlaces y bloques está definido únicamente en `contrato-bloques.md`; cualquier cambio de formato se hace primero ahí.
