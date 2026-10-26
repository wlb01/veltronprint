package mx.veltrontech.print.imagen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Único lugar que usa Bitmap.
 * Uri: identificador de un archivo en Android, como una URL pero para el sistema de archivos local.
 * ImageDecoder (API 28+): API moderna para decodificar imágenes desde Uri sin permisos de almacenamiento.
 */
object ProcesadorImagen {

    /**
     * Decodifica la Uri y devuelve un Bitmap en ARGB_8888.
     * Dispatchers.IO: hilo de fondo para operaciones de disco/red.
     */
    suspend fun decodificarBitmap(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) { decodificarUriInterno(context, uri) }

    /**
     * Escala el Bitmap al tamaño definido por opciones y lo convierte a ImagenMonocromo.
     * Dispatchers.Default: pool de hilos para trabajo intensivo de CPU como procesar imágenes.
     */
    suspend fun procesarBitmap(bitmap: Bitmap, opciones: OpcionesLogo): ImagenMonocromo =
        withContext(Dispatchers.Default) { procesarInterno(bitmap, opciones) }

    /** Convierte ImagenMonocromo a ImageBitmap para mostrar en la interfaz. */
    fun aImageBitmap(imagen: ImagenMonocromo): ImageBitmap =
        monoCromoABitmap(imagen).asImageBitmap()

    fun guardar(bitmap: Bitmap, file: File) {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    fun cargar(file: File): Bitmap? = BitmapFactory.decodeFile(file.absolutePath)

    fun cargarMonocromo(file: File): ImagenMonocromo? {
        val bmp = cargar(file) ?: return null
        val ancho = bmp.width
        val alto = bmp.height
        val pixels = IntArray(ancho * alto)
        bmp.getPixels(pixels, 0, ancho, 0, 0, ancho, alto)
        bmp.recycle()
        val negros = BooleanArray(pixels.size) { i -> (pixels[i] and 0xFFFFFF) == 0 }
        return ImagenMonocromo(ancho, alto, negros)
    }

    fun monoCromoABitmap(imagen: ImagenMonocromo): Bitmap {
        val bmp = Bitmap.createBitmap(imagen.ancho, imagen.alto, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(imagen.ancho * imagen.alto) { i ->
            if (imagen.negros[i]) Color.BLACK else Color.WHITE
        }
        bmp.setPixels(pixels, 0, imagen.ancho, 0, 0, imagen.ancho, imagen.alto)
        return bmp
    }

    fun reducirAMaxAncho(bitmap: Bitmap, maxAncho: Int): Bitmap {
        if (bitmap.width <= maxAncho) return bitmap
        val alto = (bitmap.height * maxAncho.toDouble() / bitmap.width).toInt()
        return Bitmap.createScaledBitmap(bitmap, maxAncho, alto, true)
    }

    private fun decodificarUriInterno(context: Context, uri: Uri): Bitmap? = try {
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } ?: return null
        if (bmp.config == Bitmap.Config.ARGB_8888) bmp
        else bmp.copy(Bitmap.Config.ARGB_8888, false).also { bmp.recycle() }
    } catch (_: Exception) { null }

    private fun procesarInterno(original: Bitmap, opciones: OpcionesLogo): ImagenMonocromo {
        val (anchoDestino, altoDestino) = ConversorImagen.calcularTamano(
            original.width, original.height, opciones.tamano.ancho
        )
        val escalado = Bitmap.createScaledBitmap(original, anchoDestino, altoDestino, true)
        val pixels = IntArray(anchoDestino * altoDestino)
        escalado.getPixels(pixels, 0, anchoDestino, 0, 0, anchoDestino, altoDestino)
        if (escalado !== original) escalado.recycle()
        val grises = ConversorImagen.aGrises(pixels)
        return when (opciones.modo) {
            ModoConversion.UMBRAL  -> ConversorImagen.umbral(grises, anchoDestino, altoDestino, opciones.claridad)
            ModoConversion.TRAMADO -> ConversorImagen.tramado(grises, anchoDestino, altoDestino)
        }
    }
}
