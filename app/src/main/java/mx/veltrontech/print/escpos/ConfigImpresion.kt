package mx.veltrontech.print.escpos

import mx.veltrontech.print.contrato.ModoCorte

// Constantes temporales hasta que el módulo 008 las mueva a DataStore.
object ConfigImpresion {
    val TABLA_PREDETERMINADA = TablaCaracteres.CP850
    val CORTE_PREDETERMINADO = ModoCorte.PARCIAL
    const val COLUMNAS_NORMAL = 48
    const val COLUMNAS_COMPACTA = 64
}
