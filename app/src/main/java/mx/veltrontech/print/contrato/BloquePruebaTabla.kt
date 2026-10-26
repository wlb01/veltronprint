package mx.veltrontech.print.contrato

import mx.veltrontech.print.escpos.TablaCaracteres

// Interno: solo para la sección "PRUEBA DE ACENTOS" de TicketPrueba.
// No forma parte del contrato público; nunca se acepta desde un enlace externo.
// Se elimina en el módulo 008.
data class BloquePruebaTabla(val etiqueta: String, val tabla: TablaCaracteres) : Bloque
