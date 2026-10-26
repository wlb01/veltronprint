package mx.veltrontech.print.ajustes

import mx.veltrontech.print.contrato.Alineacion
import mx.veltrontech.print.contrato.Bloque
import mx.veltrontech.print.contrato.BloquePruebaTabla
import mx.veltrontech.print.contrato.Fuente
import mx.veltrontech.print.contrato.Trabajo
import mx.veltrontech.print.escpos.TablaCaracteres

fun ticketPrueba(): Trabajo = Trabajo(
    v = 1,
    tipo = "registro",
    folio = "MZT-OCT26-0042",
    bloques = listOf(
        // Encabezado del negocio
        Bloque.Logo(),
        Bloque.Texto("WhatsApp: +52 669 327 2721", alinear = Alineacion.CENTRO, fuente = Fuente.COMPACTA),
        Bloque.Texto(
            "Av. Francisco Solís 2599-Loc 16b, Plaza Bicentenario, Francisco Villa, 82127 Mazatlán, Sin.",
            alinear = Alineacion.CENTRO,
            fuente = Fuente.COMPACTA,
        ),
        Bloque.Texto("L-V 10:00-20:00 | Sáb 10:00-17:00", alinear = Alineacion.CENTRO, fuente = Fuente.COMPACTA),
        Bloque.Separador(),

        // Datos del registro
        Bloque.Par("Fecha de emisión:", "25/10/2026 20:16"),
        Bloque.Par("Folio:", "MZT-OCT26-0042", negrita = true),
        Bloque.Par("No. de cliente:", "#0158"),
        Bloque.Par("Cliente:", "Juan Pérez López"),
        Bloque.Par("Teléfono:", "669 123 4567"),
        Bloque.Par("Atendido por:", "Daniel"),
        Bloque.Separador(),

        // Datos del equipo
        Bloque.Texto("DATOS DEL EQUIPO", negrita = true),
        Bloque.Par("Marca:", "Apple"),
        Bloque.Par("Modelo:", "iPhone 12"),
        Bloque.Par("IMEI/SN:", "356789012345678"),
        Bloque.Separador(),

        // Falla
        Bloque.Texto("FALLA REPORTADA", negrita = true),
        Bloque.Texto("No carga, se calienta al conectar el cargador"),
        Bloque.Separador(),

        // Presupuesto
        Bloque.Par("Presupuesto cotizado:", "\$890.00"),
        Bloque.Par("Anticipo:", "\$400.00"),
        Bloque.Par("RESTANTE:", "\$490.00", negrita = true),
        Bloque.Separador(),

        // Accesorios
        Bloque.Texto("[X] SIM y MicroSD devueltos al cliente"),
        Bloque.Separador(),

        // QR de T&C
        Bloque.Texto("Puedes consultar los T&C aquí:", alinear = Alineacion.CENTRO),
        Bloque.Qr("https://veltrontech.mx/terminos"),
        Bloque.Separador(),

        // Firma y pie
        Bloque.Texto("Firma de conformidad registrada digitalmente", alinear = Alineacion.CENTRO),
        Bloque.Separador(),
        Bloque.Texto(
            "Debe presentar este ticket para recoger su equipo\nGracias por su confianza\nwww.veltrontech.mx",
            alinear = Alineacion.CENTRO,
            fuente = Fuente.COMPACTA,
        ),

        // Prueba de acentos (temporal: se imprime con su propia tabla cada línea)
        Bloque.Separador(),
        Bloque.Texto("PRUEBA DE ACENTOS", negrita = true),
        BloquePruebaTabla("CP850:   áéíóú ÁÉÍÓÚ ñÑ üÜ ¿¡ \$ °", TablaCaracteres.CP850),
        BloquePruebaTabla("WIN1252: áéíóú ÁÉÍÓÚ ñÑ üÜ ¿¡ \$ °", TablaCaracteres.WIN1252),

        Bloque.Corte(),
    ),
)
