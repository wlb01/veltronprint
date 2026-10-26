package mx.veltrontech.print.escpos

import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.BloquePruebaTabla
import mx.veltrontech.print.contrato.Fuente
import mx.veltrontech.print.contrato.ModoCorte
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.escpos.AjusteLinea.componerPar
import mx.veltrontech.print.escpos.AjusteLinea.cortarPorPalabra
import mx.veltrontech.print.escpos.Codificador.codificar
import mx.veltrontech.print.escpos.ComandosEscPos.ABRIR_CAJON
import mx.veltrontech.print.escpos.ComandosEscPos.INICIALIZAR
import mx.veltrontech.print.escpos.ComandosEscPos.SALTO_LINEA
import mx.veltrontech.print.escpos.ComandosEscPos.alinear
import mx.veltrontech.print.escpos.ComandosEscPos.avanzarLineas
import mx.veltrontech.print.escpos.ComandosEscPos.cortar
import mx.veltrontech.print.escpos.ComandosEscPos.fuente
import mx.veltrontech.print.escpos.ComandosEscPos.negrita
import mx.veltrontech.print.escpos.ComandosEscPos.subrayado
import mx.veltrontech.print.escpos.ComandosEscPos.tablaCaracteres
import mx.veltrontech.print.escpos.ComandosEscPos.tamano
import java.io.ByteArrayOutputStream

class MotorEscPos(
    private val tabla: TablaCaracteres = ConfigImpresion.TABLA_PREDETERMINADA,
    private val corteDefault: ModoCorte = ConfigImpresion.CORTE_PREDETERMINADO,
) {

    fun generar(trabajo: Trabajo): ByteArray {
        val out = ByteArrayOutputStream()
        repeat(trabajo.copias.coerceAtLeast(1)) {
            out.write(INICIALIZAR)
            out.write(tablaCaracteres(tabla.numero))
            for (bloque in trabajo.bloques) {
                out.write(generarBloque(bloque))
                out.write(resetEstilos())
            }
        }
        return out.toByteArray()
    }

    private fun generarBloque(bloque: Bloque): ByteArray = when (bloque) {
        is Bloque.Texto -> generarTexto(bloque)
        is Bloque.Par -> generarPar(bloque)
        is Bloque.Separador -> generarSeparador(bloque)
        is Bloque.Espacio -> avanzarLineas(bloque.lineas)
        is Bloque.Logo -> generarTextoSimple("[LOGO]", bloque.alinear.ordinal)
        is Bloque.Qr -> generarTextoSimple("[QR]", bloque.alinear.ordinal)
        is Bloque.CodigoBarras -> generarTextoSimple("[CÓDIGO DE BARRAS]", bloque.alinear.ordinal)
        is Bloque.Corte -> cortar(bloque.modo ?: corteDefault)
        Bloque.Cajon -> ABRIR_CAJON
        is BloquePruebaTabla -> generarPruebaTabla(bloque)
    }

    private fun generarTexto(b: Bloque.Texto): ByteArray {
        val out = ByteArrayOutputStream()
        val cols = columnas(b.fuente) / b.tamano
        out.write(fuente(b.fuente.ordinal))
        out.write(alinear(b.alinear.ordinal))
        out.write(negrita(b.negrita))
        out.write(subrayado(b.subrayado))
        out.write(tamano(b.tamano))
        for (linea in cortarPorPalabra(b.valor, cols)) {
            out.write(codificar(linea, tabla))
            out.write(SALTO_LINEA)
        }
        return out.toByteArray()
    }

    private fun generarPar(b: Bloque.Par): ByteArray {
        val out = ByteArrayOutputStream()
        val cols = columnas(b.fuente)
        out.write(fuente(b.fuente.ordinal))
        out.write(negrita(b.negrita))
        for (linea in componerPar(b.izquierda, b.derecha, cols)) {
            out.write(codificar(linea, tabla))
            out.write(SALTO_LINEA)
        }
        return out.toByteArray()
    }

    private fun generarSeparador(b: Bloque.Separador): ByteArray =
        codificar(b.caracter.toString().repeat(ConfigImpresion.COLUMNAS_NORMAL), tabla) + SALTO_LINEA

    private fun generarTextoSimple(texto: String, alineacion: Int): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(alinear(alineacion))
        out.write(codificar(texto, tabla))
        out.write(SALTO_LINEA)
        return out.toByteArray()
    }

    private fun generarPruebaTabla(b: BloquePruebaTabla): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(tablaCaracteres(b.tabla.numero))
        out.write(codificar(b.etiqueta, b.tabla))
        out.write(SALTO_LINEA)
        out.write(tablaCaracteres(tabla.numero))  // restaurar tabla predeterminada
        return out.toByteArray()
    }

    private fun resetEstilos(): ByteArray =
        alinear(0) + negrita(false) + subrayado(false) + tamano(1) + fuente(0)

    private fun columnas(f: Fuente) = when (f) {
        Fuente.NORMAL -> ConfigImpresion.COLUMNAS_NORMAL
        Fuente.COMPACTA -> ConfigImpresion.COLUMNAS_COMPACTA
    }
}
