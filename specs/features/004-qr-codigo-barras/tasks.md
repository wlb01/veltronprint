# Tasks — Módulo 004: QR y códigos de barras

> Checklist de tareas pequeñas y verificables. Se marcan según pasan el QA (Paso 6 del método).

## Construcción

- [x] Agregar ZXing core 3.5.3 a `libs.versions.toml` y `app/build.gradle.kts`
- [x] Crear `codigos/CodigoInvalidoException.kt`
- [x] Crear `codigos/GeneradorQr.kt` (nivel M, margen de 4 módulos, 4/6/8 puntos por módulo, reducción si no cabe)
- [x] Crear `codigos/GeneradorCodigoBarras.kt` (CODE128, 1–40 ASCII, margen de 10, barra de 2 o 1 puntos, alto 80)
- [x] Crear `escpos/ResultadoImpresion.kt`
- [x] Actualizar `MotorEscPos.kt` (QR y código de barras reales, sin textos temporales)
- [x] Actualizar `ServicioImpresion.kt` (devuelve `ResultadoImpresion`, atrapa `CodigoInvalidoException`)
- [x] Actualizar `InicioViewModel.kt` y `AjustesViewModel.kt` para `ResultadoImpresion`
- [x] Agregar la sección "PRUEBA DE CÓDIGOS" a `TicketPrueba.kt`
- [x] Agregar `error_codigo_invalido` a `strings.xml`
- [x] Crear `GeneradorQrTest.kt` y `GeneradorCodigoBarrasTest.kt`; actualizar `MotorEscPosTest.kt`
- [x] `./gradlew test` pasa
- [x] `./gradlew assembleDebug` pasa

## Verificación (copiado de los criterios de aceptación de spec.md)

- [x] El ticket de prueba imprime un QR real donde antes salía `[QR]`.
- [x] Los tres QR de "PRUEBA DE CÓDIGOS" salen centrados y con tamaños visiblemente distintos (chico < mediano < grande).
- [x] Cada QR tiene margen blanco alrededor y ningún texto o línea lo invade.
- [x] Los cuatro QR del ticket se leen con la cámara y muestran `https://veltrontech.mx/terminos`.
- [x] El QR chico se lee a la primera desde unos 15 cm.
- [x] El código de barras sale centrado, con margen blanco a ambos lados.
- [x] El código de barras se lee con la cámara y muestra `MZT-OCT26-0042`.
- [x] Debajo del código de barras aparece `MZT-OCT26-0042` centrado.
- [x] Los QR y el código de barras salen con negros sólidos, sin franjas blancas ni partes desplazadas.
- [x] El resto del ticket se imprime igual que en el módulo 003.
- [x] Las pruebas JUnit generan un QR y un código de barras, los vuelven a leer con ZXing y obtienen el mismo contenido.
- [x] Las pruebas JUnit verifican el tamaño en puntos de cada tamaño de QR y que un código de barras de 41 caracteres produce el error de contenido.

## Verificación de casos de error (de la sección 5 de spec.md)

- [x] "No se pudo generar un código del ticket. Revisa el contenido del QR o del código de barras." existe con su texto exacto en `strings.xml` y lo cubren las pruebas JUnit (no se puede provocar desde la interfaz hasta el módulo 005)
- [x] Con la impresora apagada, el ticket de prueba sigue mostrando el mensaje de "No responde" del módulo 001

## Verificación visual

Sin cambios de pantalla en este módulo.

## Cierre

- [x] Todos los criterios de arriba pasan
- [x] Commit hecho (`feat: QR y códigos de barras`)
- [x] ESTADO_PROYECTO.md actualizado
