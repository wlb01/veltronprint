package mx.veltrontech.print.imagen

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.logoDataStore: DataStore<Preferences> by preferencesDataStore(name = "logo")

class LogoRepository(private val context: Context) {

    private val logoDir get() = File(context.filesDir, "logo")
    private val archivoOriginal get() = File(logoDir, "original.png")
    private val archivoConvertido get() = File(logoDir, "convertido.png")

    private companion object {
        val KEY_MODO = stringPreferencesKey("logo_modo")
        val KEY_CLARIDAD = intPreferencesKey("logo_claridad")
        val KEY_TAMANO = stringPreferencesKey("logo_tamano")
    }

    val logo: Flow<LogoGuardado?> = context.logoDataStore.data.map { prefs ->
        val modo = prefs[KEY_MODO]?.let { runCatching { ModoConversion.valueOf(it) }.getOrNull() }
            ?: return@map null
        val claridad = prefs[KEY_CLARIDAD] ?: 128
        val tamano = prefs[KEY_TAMANO]?.let { runCatching { TamanoLogo.valueOf(it) }.getOrNull() }
            ?: return@map null
        if (!archivoConvertido.exists()) return@map null
        LogoGuardado(OpcionesLogo(modo, claridad, tamano))
    }

    suspend fun guardar(original: Bitmap, opciones: OpcionesLogo, convertido: ImagenMonocromo) =
        withContext(Dispatchers.IO) {
            logoDir.mkdirs()
            val tempOrig = File(logoDir, "original.tmp.png")
            val tempConv = File(logoDir, "convertido.tmp.png")

            val origReducido = ProcesadorImagen.reducirAMaxAncho(original, 1152)
            ProcesadorImagen.guardar(origReducido, tempOrig)
            if (origReducido !== original) origReducido.recycle()

            val bmpConvertido = ProcesadorImagen.monoCromoABitmap(convertido)
            ProcesadorImagen.guardar(bmpConvertido, tempConv)
            bmpConvertido.recycle()

            tempOrig.renameTo(archivoOriginal)
            tempConv.renameTo(archivoConvertido)

            context.logoDataStore.edit { prefs ->
                prefs[KEY_MODO] = opciones.modo.name
                prefs[KEY_CLARIDAD] = opciones.claridad
                prefs[KEY_TAMANO] = opciones.tamano.name
            }
        }

    suspend fun quitar() = withContext(Dispatchers.IO) {
        archivoOriginal.delete()
        archivoConvertido.delete()
        context.logoDataStore.edit { it.clear() }
    }

    suspend fun cargarMonocromo(): ImagenMonocromo? = withContext(Dispatchers.IO) {
        if (!archivoConvertido.exists()) null
        else ProcesadorImagen.cargarMonocromo(archivoConvertido)
    }

    suspend fun cargarOriginal(): Bitmap? = withContext(Dispatchers.IO) {
        if (!archivoOriginal.exists()) null
        else ProcesadorImagen.cargar(archivoOriginal)
    }
}
