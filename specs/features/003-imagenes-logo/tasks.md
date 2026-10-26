# Tasks — Módulo 003: Imágenes y logo

> Checklist de tareas pequeñas y verificables. Se marcan según pasan el QA (Paso 6 del método).

## Construcción

**Conversión e impresión de imágenes**
- [x] Crear `imagen/ImagenMonocromo.kt` y `imagen/OpcionesLogo.kt`
- [x] Crear `imagen/ConversorImagen.kt` (grises, umbral, tramado, cálculo de tamaño)
- [x] Crear `imagen/ProcesadorImagen.kt` (único archivo que usa `Bitmap`)
- [x] Agregar `imagenRaster()` a `ComandosEscPos.kt`, comentado
- [x] Crear `imagen/RasterEscPos.kt` (posicionar, empaquetar, franjas de 128 filas)

**Logo**
- [x] Crear `imagen/LogoRepository.kt` (archivos + DataStore, guardado seguro con temporales)
- [x] Actualizar `MotorEscPos.kt` y `ServicioImpresion.kt` (logo real, sin `[LOGO]`, `imprimirLogoPrueba()`)

**Pantallas**
- [x] Crear `ui/componentes/VistaPreviaPapel.kt`
- [x] Crear `ajustes/SeccionLogo.kt`, `AjustesScreen.kt` y `AjustesViewModel.kt`
- [x] Agregar la ruta `ajustes` y el engrane en Inicio
- [x] Diálogos "Cambios sin guardar" y "Quitar logo"
- [x] Agregar los textos nuevos a `strings.xml`
- [x] Registrar `LogoRepository` y `ProcesadorImagen` en `AppContainer.kt`
- [x] Actualizar el comentario de `ajustes/` en el CLAUDE.md

**Pruebas y cierre técnico**
- [x] Crear `ConversorImagenTest.kt` y `RasterEscPosTest.kt`; actualizar `MotorEscPosTest.kt`
- [x] `./gradlew test` pasa
- [x] `./gradlew assembleDebug` pasa

## Verificación (copiado de los criterios de aceptación de spec.md)

- [x] Inicio muestra el ícono de engrane y al tocarlo abre Ajustes.
- [x] Sin logo, la sección muestra "Aún no hay logo. Se imprime en los tickets que lo incluyan." y el botón "Elegir imagen".
- [x] "Elegir imagen" abre el selector de fotos sin pedir ningún permiso.
- [x] Al elegir el logo, la vista previa aparece en modo "Umbral" y tamaño "75%".
- [x] La vista previa se ve en blanco y negro puro (sin grises) sobre fondo blanco, también en modo oscuro.
- [x] Mover la claridad cambia la vista previa en menos de 1 segundo.
- [x] El control de claridad desaparece en modo "Tramado".
- [x] Cambiar entre "50%", "75%" y "100%" cambia el tamaño de la vista previa.
- [x] Un PNG con fondo transparente se ve con fondo blanco en la vista previa.
- [x] "Guardar logo" está deshabilitado cuando no hay cambios.
- [x] Al guardar aparece "Logo guardado.".
- [x] El logo guardado sigue ahí después de cerrar la app por completo y volverla a abrir, con el mismo modo, claridad y tamaño.
- [x] "Imprimir logo de prueba" imprime solo el logo, centrado, y hace corte parcial.
- [x] El logo impreso coincide con la vista previa (mismos detalles visibles y mismo tamaño relativo).
- [x] Al 100%, el logo ocupa el ancho imprimible completo sin cortarse por ningún lado.
- [x] El ticket de prueba imprime el logo donde antes salía `[LOGO]`.
- [x] Sin logo guardado, el ticket de prueba se imprime completo y sin la línea `[LOGO]`.
- [x] Salir con cambios sin guardar muestra el diálogo "Cambios sin guardar"; "Descartar" conserva el logo anterior.
- [x] "Quitar logo" pide confirmación y, al aceptar, deja la sección en estado "sin logo guardado".
- [x] Las pruebas JUnit de conversión, escalado y comando de imagen pasan.

## Verificación de casos de error (de la sección 5 de spec.md)

- [x] Elegir un archivo que no es imagen (por ejemplo, un PDF) muestra: "No se pudo abrir la imagen. Elige un archivo PNG o JPG."
- [x] "No se pudo guardar el logo. Intenta de nuevo." existe con su texto exacto en `strings.xml` (no se puede provocar en QA manual)
- [x] "Imprimir logo de prueba" con la impresora apagada muestra el mensaje exacto de "No responde" del módulo 001

## Verificación visual

- [x] Galaxy Tab S10 Lite en horizontal: vista previa a la izquierda, controles a la derecha
- [x] Galaxy Tab S10 Lite en vertical: vista previa arriba, controles abajo
- [ ] Honor X8 en vertical y en horizontal se ve correctamente
- [x] Modo claro y modo oscuro se ven correctamente

## Cierre

- [x] Todos los criterios de arriba pasan
- [x] Commit hecho (`feat: logo e impresión de imágenes`)
- [x] ESTADO_PROYECTO.md actualizado
