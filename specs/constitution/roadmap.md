# Roadmap — Veltron Print

> Parte 3 de la Spec de Sistema. Orden en el que se construyen los módulos y por qué ese orden.

## Orden de construcción

| Orden | Módulo | Depende de | Por qué este orden |
|-------|--------|-----------|---------------------|
| 1 | 001-conexion-bluetooth | — (base) | Sin conexión no se puede probar nada; es el primer riesgo técnico a eliminar |
| 2 | 002-motor-escpos | 001 | Con texto, centrado y corte ya se resuelven dos de los tres problemas que RawBT no resolvía |
| 3 | 003-imagenes-logo | 002 | Resuelve el tercer problema (imágenes) y deja lista la conversión a 1 bit que usa el 004 |
| 4 | 004-qr-codigo-barras | 003 | Reutiliza la impresión de imágenes raster |
| 5 | 005-receptor-enlaces | 002, 003, 004 | Ya existe un motor que sabe imprimir todos los bloques; ahora Registro puede mandarle trabajos |
| 6 | 006-cola-reintento | 005 | Los trabajos ya llegan desde fuera; hay que garantizar que ninguno se pierda |
| 7 | 007-historial | 006 | Se apoya en la misma base de datos de la cola |
| 8 | 008-ajustes-prueba | 002, 003 | Reúne en una pantalla los ajustes que los módulos anteriores fueron necesitando |
| 9 | 009-tickets-manuales | 005, 006 | Genera bloques del mismo contrato y los mete al mismo flujo; reutiliza todo lo anterior |

## Hitos

- [ ] **Hito 1 — Primera impresión propia:** la app imprime texto centrado y corta el papel — módulos 001-002
- [ ] **Hito 2 — Ticket completo:** logo, QR y código de barras — módulos 003-004
- [ ] **Hito 3 — Adiós RawBT:** Registro imprime directo con `veltronprint://` — módulo 005 (+ cambio en Registro)
- [ ] **Hito 4 — Operación confiable:** cola, historial y ajustes — módulos 006-008
- [ ] **Hito 5 — Garantías y compras:** tickets manuales — módulo 009
- [ ] **Lanzamiento V1:** instalada en la tablet de recepción

## Trabajo relacionado en Registro (fuera de esta app)

Después del Hito 3, Registro necesita una feature propia (en su repositorio y con su propia spec) para dejar de generar `.txt` y armar el JSON de bloques. Ver la sección "Impacto en Registro" de `contrato-bloques.md`.
