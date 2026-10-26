package mx.veltrontech.print.contrato

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Recorre el JSON a mano (sin @Serializable) para dar mensajes de error exactos con número de bloque.
object ValidadorTrabajo {

    private class InvalidoEx(val detalle: String) : Exception(detalle)
    private class VersionDesconocidaEx(val v: Int) : Exception()

    fun validar(json: String): ResultadoValidacion {
        val root = try {
            Json.parseToJsonElement(json) as? JsonObject
        } catch (e: Exception) { null }
            ?: return ResultadoValidacion.Danado

        return try {
            ResultadoValidacion.Valido(parsearTrabajo(root))
        } catch (e: VersionDesconocidaEx) {
            ResultadoValidacion.VersionDesconocida(e.v)
        } catch (e: InvalidoEx) {
            ResultadoValidacion.Invalido(e.detalle)
        }
    }

    private fun parsearTrabajo(root: JsonObject): Trabajo {
        // v se valida primero para dar VersionDesconocida aunque lo demás esté mal
        val vP = root["v"] as? JsonPrimitive ?: throw InvalidoEx("Trabajo: falta el campo v")
        if (vP.isString) throw InvalidoEx("Trabajo: v debe ser un número entero sin comillas")
        val v = vP.content.toIntOrNull() ?: throw InvalidoEx("Trabajo: v debe ser un número entero")
        if (v != 1) throw VersionDesconocidaEx(v)

        val tipoP = root["tipo"] as? JsonPrimitive ?: throw InvalidoEx("Trabajo: falta el campo tipo")
        if (!tipoP.isString) throw InvalidoEx("Trabajo: tipo debe ser texto")
        val tipo = tipoP.content
        if (tipo.isBlank()) throw InvalidoEx("Trabajo: tipo no puede estar vacío")

        val folio = (root["folio"] as? JsonPrimitive)
            ?.takeIf { it.isString }?.content

        val copias: Int = root["copias"]?.let { el ->
            val p = el as? JsonPrimitive ?: throw InvalidoEx("Trabajo: copias debe ser un número entero")
            if (p.isString) throw InvalidoEx("Trabajo: copias debe ser un número entero sin comillas")
            val c = p.content.toIntOrNull() ?: throw InvalidoEx("Trabajo: copias debe ser un número entero")
            if (c < 1 || c > 3) throw InvalidoEx("Trabajo: copias debe estar entre 1 y 3; llegó $c")
            c
        } ?: 1

        val bloquesArr = root["bloques"] as? JsonArray
            ?: throw InvalidoEx("Trabajo: falta el campo bloques")
        if (bloquesArr.isEmpty()) throw InvalidoEx("Trabajo: bloques no puede estar vacío")

        val bloques = bloquesArr.mapIndexed { idx, elem ->
            val n = idx + 1
            val obj = elem as? JsonObject ?: throw InvalidoEx("Bloque $n: debe ser un objeto JSON")
            parsearBloque(n, obj)
        }

        return Trabajo(v = v, tipo = tipo, folio = folio, copias = copias, bloques = bloques)
    }

    private fun parsearBloque(n: Int, obj: JsonObject): Bloque {
        val tipoP = obj["tipo"] as? JsonPrimitive ?: throw InvalidoEx("Bloque $n: falta el campo tipo")
        if (!tipoP.isString) throw InvalidoEx("Bloque $n: tipo debe ser texto")
        val tipo = tipoP.content
        val ctx = "Bloque $n ($tipo)"

        return when (tipo) {
            "texto" -> Bloque.Texto(
                valor = obj.reqString("valor", ctx),
                alinear = obj.optAlineacion("alinear", Alineacion.IZQUIERDA, ctx),
                negrita = obj.optBool("negrita", false, ctx),
                subrayado = obj.optBool("subrayado", false, ctx),
                tamano = obj.optTamanoTexto(ctx),
                fuente = obj.optFuente("fuente", Fuente.NORMAL, ctx),
            )
            "par" -> Bloque.Par(
                izquierda = obj.reqString("izquierda", ctx),
                derecha = obj.reqString("derecha", ctx),
                negrita = obj.optBool("negrita", false, ctx),
                fuente = obj.optFuente("fuente", Fuente.NORMAL, ctx),
            )
            "separador" -> {
                val carEl = obj["caracter"]
                if (carEl == null) {
                    Bloque.Separador()
                } else {
                    val p = carEl as? JsonPrimitive ?: throw InvalidoEx("$ctx: caracter debe ser texto")
                    if (!p.isString) throw InvalidoEx("$ctx: caracter debe ser texto")
                    if (p.content.length != 1) throw InvalidoEx("$ctx: caracter debe ser exactamente un carácter")
                    Bloque.Separador(p.content[0])
                }
            }
            "espacio" -> {
                val lineas = obj["lineas"]?.let { el ->
                    val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: lineas debe ser un número entero")
                    if (p.isString) throw InvalidoEx("$ctx: lineas debe ser un número entero sin comillas")
                    val v = p.content.toIntOrNull() ?: throw InvalidoEx("$ctx: lineas debe ser un número entero")
                    if (v < 1 || v > 5) throw InvalidoEx("$ctx: lineas debe estar entre 1 y 5; llegó $v")
                    v
                } ?: 1
                Bloque.Espacio(lineas)
            }
            "logo" -> Bloque.Logo(obj.optAlineacion("alinear", Alineacion.CENTRO, ctx))
            "qr" -> {
                val valor = obj.reqString("valor", ctx)
                if (valor.isEmpty()) throw InvalidoEx("$ctx: valor del QR no puede estar vacío")
                if (valor.length > 500) throw InvalidoEx(
                    "$ctx: valor del QR es demasiado largo (máx. 500 caracteres); llegó ${valor.length}")
                val tamano = obj["tamano"]?.let { el ->
                    val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: tamano debe ser texto")
                    if (!p.isString) throw InvalidoEx("$ctx: tamano debe ser texto sin comillas")
                    when (p.content) {
                        "chico" -> TamanoQr.CHICO
                        "mediano" -> TamanoQr.MEDIANO
                        "grande" -> TamanoQr.GRANDE
                        else -> throw InvalidoEx("$ctx: tamano debe ser chico, mediano o grande")
                    }
                } ?: TamanoQr.MEDIANO
                Bloque.Qr(valor, tamano, obj.optAlineacion("alinear", Alineacion.CENTRO, ctx))
            }
            "codigo_barras" -> {
                val valor = obj.reqString("valor", ctx)
                if (valor.isEmpty()) throw InvalidoEx("$ctx: valor del código de barras no puede estar vacío")
                if (valor.length > 40) throw InvalidoEx(
                    "$ctx: valor del código de barras es demasiado largo (máx. 40 caracteres); llegó ${valor.length}")
                if (valor.any { it.code < 32 || it.code > 126 }) throw InvalidoEx(
                    "$ctx: valor del código de barras contiene caracteres no permitidos (solo ASCII imprimible)")
                val formato = obj["formato"]?.let { el ->
                    val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: formato debe ser texto")
                    if (!p.isString) throw InvalidoEx("$ctx: formato debe ser texto")
                    if (p.content != "CODE128") throw InvalidoEx("$ctx: formato no reconocido; solo se admite CODE128")
                    p.content
                } ?: "CODE128"
                Bloque.CodigoBarras(
                    valor = valor,
                    formato = formato,
                    mostrarTexto = obj.optBool("mostrar_texto", true, ctx),
                    alinear = obj.optAlineacion("alinear", Alineacion.CENTRO, ctx),
                )
            }
            "corte" -> {
                val modo = obj["modo"]?.let { el ->
                    val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: modo debe ser texto")
                    if (!p.isString) throw InvalidoEx("$ctx: modo debe ser texto")
                    when (p.content) {
                        "total" -> ModoCorte.TOTAL
                        "parcial" -> ModoCorte.PARCIAL
                        else -> throw InvalidoEx("$ctx: modo debe ser total o parcial")
                    }
                }
                Bloque.Corte(modo)
            }
            "cajon" -> Bloque.Cajon
            else -> throw InvalidoEx("Bloque $n: tipo desconocido \"$tipo\"")
        }
    }

    // --- helpers de JsonObject ---

    private fun JsonObject.reqString(key: String, ctx: String): String {
        val p = this[key] as? JsonPrimitive ?: throw InvalidoEx("$ctx: falta el campo $key")
        if (!p.isString) throw InvalidoEx("$ctx: $key debe ser texto")
        return p.content
    }

    private fun JsonObject.optBool(key: String, default: Boolean, ctx: String): Boolean {
        val el = this[key] ?: return default
        val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: $key debe ser un valor booleano")
        if (p.isString) throw InvalidoEx("$ctx: $key debe ser un valor booleano sin comillas")
        return when (p.content) {
            "true" -> true
            "false" -> false
            else -> throw InvalidoEx("$ctx: $key debe ser un valor booleano")
        }
    }

    private fun JsonObject.optTamanoTexto(ctx: String): Int {
        val el = this["tamano"] ?: return 1
        val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: tamano debe ser un número entero")
        if (p.isString) throw InvalidoEx("$ctx: tamano debe ser un número entero sin comillas")
        val v = p.content.toIntOrNull() ?: throw InvalidoEx("$ctx: tamano debe ser un número entero")
        if (v !in 1..3) throw InvalidoEx("$ctx: tamano debe ser 1, 2 o 3; llegó $v")
        return v
    }

    private fun JsonObject.optAlineacion(key: String, default: Alineacion, ctx: String): Alineacion {
        val el = this[key] ?: return default
        val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: $key debe ser texto")
        if (!p.isString) throw InvalidoEx("$ctx: $key debe ser texto")
        return when (p.content) {
            "izquierda" -> Alineacion.IZQUIERDA
            "centro" -> Alineacion.CENTRO
            "derecha" -> Alineacion.DERECHA
            else -> throw InvalidoEx("$ctx: $key debe ser izquierda, centro o derecha")
        }
    }

    private fun JsonObject.optFuente(key: String, default: Fuente, ctx: String): Fuente {
        val el = this[key] ?: return default
        val p = el as? JsonPrimitive ?: throw InvalidoEx("$ctx: $key debe ser texto")
        if (!p.isString) throw InvalidoEx("$ctx: $key debe ser texto")
        return when (p.content) {
            "normal" -> Fuente.NORMAL
            "compacta" -> Fuente.COMPACTA
            else -> throw InvalidoEx("$ctx: $key debe ser normal o compacta")
        }
    }
}
