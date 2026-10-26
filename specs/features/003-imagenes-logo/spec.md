# Spec — Módulo 003: Imágenes y logo

> El CONTRATO del módulo. Describe comportamiento, NO implementación (eso va en plan.md).

## 1. Objetivo

Permitir cargar el logo de Veltron una sola vez desde la tablet, convertirlo a blanco y negro con vista previa, elegir su tamaño y guardarlo, para que el bloque `logo` lo imprima en lugar del texto `[LOGO]`. Dejar lista la conversión e impresión de imágenes en blanco y negro que reutiliza el módulo 004.

## 2. Pantallas

### Inicio (cambio)

- En la barra superior, a la derecha del título, aparece un ícono de engrane que abre **Ajustes**.

### Ajustes

Pantalla nueva. En este módulo solo contiene la sección "Logo"; el módulo 008 le agrega más secciones.

**Encabezado**
- Botón de regresar y título "Ajustes".

**Sección "Logo" — sin logo guardado**
- Título de la sección: "Logo".
- Texto: "Aún no hay logo. Se imprime en los tickets que lo incluyan."
- Botón principal: "Elegir imagen".

**Sección "Logo" — editando o con logo guardado**
- **Vista previa:** un rectángulo blanco que representa el papel de 72 mm, con la imagen ya convertida a blanco y negro, al tamaño y centrada como se va a imprimir. Siempre fondo blanco y puntos negros, también en modo oscuro.
- **Modo de conversión:** selector de dos opciones: "Umbral" y "Tramado".
- **Claridad:** control deslizante, visible solo en modo "Umbral". Al moverlo, la vista previa se actualiza.
- **Tamaño:** selector de tres opciones: "50%", "75%" y "100%".
- Botones:
  - "Elegir otra imagen" (en lugar de "Elegir imagen").
  - "Guardar logo" (principal; habilitado solo si hay cambios sin guardar).
  - "Imprimir logo de prueba" (habilitado solo si hay un logo guardado, no hay cambios pendientes y hay impresora guardada).
  - "Quitar logo" (habilitado solo si hay un logo guardado).

**Orientación:** en horizontal, la vista previa queda a la izquierda y los controles a la derecha; en vertical, la vista previa queda arriba y los controles debajo.

### Diálogo "Cambios sin guardar"

Aparece al intentar salir de Ajustes con cambios sin guardar.
- Texto: "Tienes cambios sin guardar en el logo. ¿Quieres descartarlos?"
- Botones: "Seguir editando" y "Descartar".

### Diálogo "Quitar logo"

- Texto: "Se quitará el logo de todos los tickets. ¿Continuar?"
- Botones: "Cancelar" y "Quitar".

## 3. Flujo del usuario

**Flujo principal — cargar el logo por primera vez**
1. Usuario toca el engrane en Inicio →
2. Sistema abre Ajustes con la sección "Logo" vacía →
3. Usuario toca "Elegir imagen" →
4. Sistema abre el selector de fotos de Android →
5. Usuario elige el archivo del logo →
6. Sistema muestra la vista previa en modo "Umbral", claridad media y tamaño "75%" →
7. Usuario ajusta modo, claridad y tamaño mirando la vista previa →
8. Usuario toca "Guardar logo" →
9. Sistema guarda el logo y muestra el aviso "Logo guardado." →
10. Usuario toca "Imprimir logo de prueba" →
11. Impresora imprime solo el logo y hace el corte predeterminado.

**Flujo — el ticket usa el logo**
1. Usuario imprime el ticket de prueba desde Inicio →
2. Sistema imprime el logo guardado donde antes salía `[LOGO]`.

**Flujo — salir con cambios sin guardar**
1. Usuario modifica algún control y toca regresar →
2. Sistema muestra el diálogo "Cambios sin guardar" →
3. Usuario toca "Descartar" → Sistema regresa a Inicio y conserva el logo anterior; o toca "Seguir editando" → Sistema cierra el diálogo.

**Flujo — quitar el logo**
1. Usuario toca "Quitar logo" →
2. Sistema muestra el diálogo "Quitar logo" →
3. Usuario toca "Quitar" →
4. Sistema borra el logo y la sección vuelve al estado "sin logo guardado".

## 4. Lógica y reglas

**Carga**
- Se aceptan imágenes PNG, JPG y WEBP elegidas con el selector de fotos de Android; la app no pide permisos de almacenamiento.
- Las partes transparentes de la imagen se tratan como blanco.
- La imagen original se guarda dentro de la app, para poder cambiar modo, claridad y tamaño después sin volver a elegir el archivo.

**Tamaño**
- "100%" = 576 puntos de ancho (72 mm); "75%" = 432 puntos; "50%" = 288 puntos.
- La imagen se escala conservando su proporción para alcanzar el ancho elegido.
- Si al escalar el alto supera 600 puntos (unos 75 mm), se reduce hasta que el alto sea 600 puntos, conservando la proporción.

**Conversión a blanco y negro**
- Cada punto se convierte primero a escala de grises.
- **Umbral:** un punto es negro si es más oscuro que el nivel de claridad elegido; si no, es blanco. Mover la claridad hacia "más claro" vuelve negros más puntos.
- **Tramado:** los tonos intermedios se representan con patrones de puntos negros (difusión de error Floyd–Steinberg); la claridad no aplica.
- La vista previa muestra exactamente los mismos puntos que se van a imprimir.

**Guardado**
- Se guardan la imagen original, la imagen ya convertida, el modo, la claridad y el tamaño.
- El logo se conserva al cerrar la app y al reiniciar el equipo.
- Los cambios en los controles no afectan los tickets hasta tocar "Guardar logo".

**Impresión**
- El bloque `logo` imprime la imagen guardada con la alineación del bloque (por defecto, centrada).
- Si no hay logo guardado, el bloque `logo` se omite y el resto del trabajo se imprime normal (regla del contrato).
- "Imprimir logo de prueba" imprime únicamente el logo, centrado, seguido del corte predeterminado.
- Toda imagen se imprime dentro de los 576 puntos de ancho del papel.

## 5. Casos de error

| Error | Cuándo ocurre | Mensaje exacto al usuario |
|-------|---------------|----------------------------|
| Imagen inválida | El archivo elegido no se puede abrir como imagen | "No se pudo abrir la imagen. Elige un archivo PNG o JPG." |
| No se pudo guardar | Falla la escritura del logo en el almacenamiento de la app | "No se pudo guardar el logo. Intenta de nuevo." |
| Errores de conexión | Falla "Imprimir logo de prueba" | El mensaje exacto del módulo 001, mostrado en un aviso (snackbar) |

## 6. Criterios de aceptación

- [ ] Inicio muestra el ícono de engrane y al tocarlo abre Ajustes.
- [ ] Sin logo, la sección muestra "Aún no hay logo. Se imprime en los tickets que lo incluyan." y el botón "Elegir imagen".
- [ ] "Elegir imagen" abre el selector de fotos sin pedir ningún permiso.
- [ ] Al elegir el logo, la vista previa aparece en modo "Umbral" y tamaño "75%".
- [ ] La vista previa se ve en blanco y negro puro (sin grises) sobre fondo blanco, también en modo oscuro.
- [ ] Mover la claridad cambia la vista previa en menos de 1 segundo.
- [ ] El control de claridad desaparece en modo "Tramado".
- [ ] Cambiar entre "50%", "75%" y "100%" cambia el tamaño de la vista previa.
- [ ] Un PNG con fondo transparente se ve con fondo blanco en la vista previa.
- [ ] "Guardar logo" está deshabilitado cuando no hay cambios.
- [ ] Al guardar aparece "Logo guardado.".
- [ ] El logo guardado sigue ahí después de cerrar la app por completo y volverla a abrir, con el mismo modo, claridad y tamaño.
- [ ] "Imprimir logo de prueba" imprime solo el logo, centrado, y hace corte parcial.
- [ ] El logo impreso coincide con la vista previa (mismos detalles visibles y mismo tamaño relativo).
- [ ] Al 100%, el logo ocupa el ancho imprimible completo sin cortarse por ningún lado.
- [ ] El ticket de prueba imprime el logo donde antes salía `[LOGO]`.
- [ ] Sin logo guardado, el ticket de prueba se imprime completo y sin la línea `[LOGO]`.
- [ ] Salir con cambios sin guardar muestra el diálogo "Cambios sin guardar"; "Descartar" conserva el logo anterior.
- [ ] "Quitar logo" pide confirmación y, al aceptar, deja la sección en estado "sin logo guardado".
- [ ] Un archivo que no es imagen muestra el mensaje de "Imagen inválida".
- [ ] Ajustes se ve correctamente en horizontal y en vertical, en la tablet y en el Honor, en modo claro y oscuro.
- [ ] Las pruebas JUnit de conversión, escalado y comando de imagen pasan.

## Módulos conectados

- Recibe datos de: 001 (envío de bytes) y 002 (motor que procesa el bloque `logo` y el corte).
- Entrega datos a: 004 (conversión e impresión de imágenes en blanco y negro para QR y códigos de barras), 008 (pantalla de Ajustes que se amplía).
