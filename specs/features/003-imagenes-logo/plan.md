# Plan — Módulo 003: Imágenes y logo

> El CÓMO técnico. Se escribe después de spec.md y antes de programar.

## Archivos a crear

**Conversión de imágenes** (funciones puras, sin `Bitmap`, para poder probarlas con JUnit)
- `app/src/main/java/mx/veltrontech/print/imagen/ImagenMonocromo.kt` — `class ImagenMonocromo(val ancho: Int, val alto: Int, val negros: BooleanArray)`; `negros[y * ancho + x] = true` significa punto negro.
- `app/src/main/java/mx/veltrontech/print/imagen/ConversorImagen.kt`:
  - `fun aGrises(argb: IntArray): IntArray` — compone el alfa sobre blanco y aplica `gris = 0.299 R + 0.587 G + 0.114 B` (0–255).
  - `fun umbral(grises: IntArray, ancho: Int, alto: Int, nivel: Int): ImagenMonocromo` — negro si `gris < nivel`.
  - `fun tramado(grises: IntArray, ancho: Int, alto: Int): ImagenMonocromo` — Floyd–Steinberg (7/16, 3/16, 5/16, 1/16), umbral 128.
  - `fun calcularTamano(anchoOriginal: Int, altoOriginal: Int, anchoDestino: Int, altoMaximo: Int = 600): Pair<Int, Int>`.
- `app/src/main/java/mx/veltrontech/print/imagen/OpcionesLogo.kt` — `enum ModoConversion { UMBRAL, TRAMADO }`, `enum TamanoLogo(val ancho: Int) { CHICO(288), MEDIANO(432), COMPLETO(576) }`, `data class OpcionesLogo(modo, claridad: Int /* 0–255, inicial 128 */, tamano)`.
  - En la interfaz, el deslizador de claridad muestra "Más oscuro ← → Más claro"; internamente es el `nivel` del umbral (más claro = nivel más alto = más puntos negros).
- `app/src/main/java/mx/veltrontech/print/imagen/ProcesadorImagen.kt` — único lugar que toca `Bitmap`: decodifica la `Uri` (con `ImageDecoder` en API 28+ y `BitmapFactory` en API 26–27), escala con filtrado (`Bitmap.createScaledBitmap(..., filter = true)`), extrae `IntArray` con `getPixels` y llama a `ConversorImagen`. También convierte `ImagenMonocromo` a `ImageBitmap` para la vista previa. Todo en `Dispatchers.Default`.

**Impresión de imágenes**
- `ComandosEscPos.kt` (modificar) — agregar:
  - `fun imagenRaster(anchoBytes: Int, alto: Int, datos: ByteArray): ByteArray` = `1D 76 30 00 xL xH yL yH` + datos. Comentario: "GS v 0 — imprime una imagen de puntos; cada byte son 8 puntos horizontales, el bit más alto es el de la izquierda, 1 = negro".
- `app/src/main/java/mx/veltrontech/print/imagen/RasterEscPos.kt`:
  - `fun posicionar(imagen: ImagenMonocromo, alineacion: Alineacion): ImagenMonocromo` — coloca la imagen en un lienzo de 576 puntos de ancho (blanco alrededor) según la alineación. Se hace así en vez de usar `ESC a`, porque varias impresoras genéricas ignoran la alineación en imágenes.
  - `fun empaquetar(imagen: ImagenMonocromo): ByteArray` — 8 puntos por byte, bit más alto = izquierda.
  - `fun generar(imagen: ImagenMonocromo, alineacion: Alineacion): ByteArray` — posiciona, empaqueta y divide en franjas de máximo 128 filas, cada una con su propio comando `GS v 0`, para no desbordar el búfer de la impresora.

**Almacenamiento del logo**
- `app/src/main/java/mx/veltrontech/print/imagen/LogoRepository.kt`:
  - Archivos en `context.filesDir/logo/`: `original.png` (la imagen elegida, ya reducida a un máximo de 1152 px de ancho para no guardar fotos enormes) y `convertido.png` (blanco y negro, ya al tamaño de impresión).
  - Opciones en DataStore: `logo_modo`, `logo_claridad`, `logo_tamano`.
  - `val logo: Flow<LogoGuardado?>`, `suspend fun guardar(original: Bitmap, opciones: OpcionesLogo, convertido: ImagenMonocromo)`, `suspend fun quitar()`, `suspend fun cargarMonocromo(): ImagenMonocromo?`.
  - Guarda primero en archivos temporales y luego los renombra, para que un fallo a la mitad no deje un logo corrupto.

**Pantalla de Ajustes**
- `app/src/main/java/mx/veltrontech/print/ajustes/AjustesScreen.kt` y `AjustesViewModel.kt`.
- `app/src/main/java/mx/veltrontech/print/ajustes/SeccionLogo.kt` — Composable de la sección.
- `app/src/main/java/mx/veltrontech/print/ui/componentes/VistaPreviaPapel.kt` — rectángulo blanco proporcional a 576 puntos que dibuja la `ImagenMonocromo` posicionada; usa `FilterQuality.None` para que los puntos se vean nítidos.

**Pruebas JUnit** (`app/src/test/java/mx/veltrontech/print/imagen/`)
- `ConversorImagenTest.kt` — negro puro → negro; blanco → blanco; transparente → blanco; gris 100 con nivel 128 → negro y con nivel 90 → blanco; tramado de un gris 50% produce aproximadamente la mitad de puntos negros; `calcularTamano(611, 290, 432)` = `(432, 205)`; una imagen muy alta se limita a 600 de alto.
- `RasterEscPosTest.kt` — `empaquetar` de una fila `10000001` = `0x81`; ancho no múltiplo de 8 se rellena con blanco; `posicionar` centrado de 288 puntos deja 144 blancos a cada lado; una imagen de 300 filas genera 3 comandos `GS v 0` (128 + 128 + 44); encabezado `1D 76 30 00 48 00 80 00` para una franja de 576 × 128.

## Archivos a modificar

- `escpos/MotorEscPos.kt` — el bloque `Logo` pide la imagen al proveedor de logo; si existe, agrega `RasterEscPos.generar(logo, alineacion)`; si no, no agrega nada. Se quita el texto temporal `[LOGO]`.
  - Para no acoplar el motor a Android, recibe el logo como parámetro: `generar(trabajo: Trabajo, logo: ImagenMonocromo?)`. `ServicioImpresion` lo carga de `LogoRepository` antes de llamar al motor.
- `escpos/ServicioImpresion.kt` — carga el logo y lo pasa al motor; agrega `suspend fun imprimirLogoPrueba(): ResultadoEnvio` (trabajo con bloques `Logo` + `Corte`).
- `escpos/MotorEscPosTest.kt` — actualizar: `logo` con imagen genera `GS v 0`; `logo` sin imagen no genera bytes.
- `ui/navegacion/Rutas.kt` y `VeltronNavHost.kt` — ruta `ajustes`.
- `inicio/InicioScreen.kt` — ícono de engrane en la barra superior.
- `AppContainer.kt` — crea `LogoRepository` y `ProcesadorImagen`.
- `res/values/strings.xml` — textos del spec.
- `CLAUDE.md` — en la sección 4, el comentario de `ajustes/` cambia a "← 003 logo; 008 resto de ajustes".

## Modelo de datos

```
filesDir/logo/
├── original.png      (imagen elegida, máx. 1152 px de ancho)
└── convertido.png    (blanco y negro, al tamaño de impresión)

DataStore "ajustes" (se agregan)
├── logo_modo       (String: "UMBRAL" | "TRAMADO")
├── logo_claridad   (Int: 0–255)
└── logo_tamano     (String: "CHICO" | "MEDIANO" | "COMPLETO")
```

## Estado en el ViewModel

`AjustesViewModel` expone `StateFlow<AjustesUiState>`:
- `guardado: LogoGuardado?` — lo que está guardado.
- `edicion: EdicionLogo?` — imagen original cargada + opciones actuales + `ImagenMonocromo` de vista previa.
- `hayCambios: Boolean`, `procesando: Boolean`, `imprimiendo: Boolean`, `error: Int?` (id de string).
- La vista previa se recalcula con `debounce` de 150 ms al mover la claridad, en `Dispatchers.Default`.
- Evento de una sola vez para los snackbars ("Logo guardado.", errores).

## Datos falsos para desarrollo

- `@Preview` de `SeccionLogo` con: sin logo, editando en Umbral, editando en Tramado, logo guardado.
- Para la QA, Daniel pasa a la tablet el logo oficial (`veltron_logo_original.jpg`, 611 × 290) y, opcionalmente, un PNG con fondo transparente para el criterio de transparencia.

## Dependencias de este módulo

- Necesita 001 (envío) y 002 (motor y corte).
- Sin dependencias externas nuevas.
