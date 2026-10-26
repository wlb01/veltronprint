package mx.veltrontech.print.datos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mx.veltrontech.print.bluetooth.ImpresoraGuardada

// Extensión de Context para acceder al DataStore — se crea una sola instancia
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

// DataStore: almacenamiento de clave-valor asíncrono que reemplaza SharedPreferences.
// Expone los datos como Flow (stream asíncrono), así la UI siempre refleja el estado actual.
class PreferenciasRepository(private val context: Context) {

    private companion object {
        val IMPRESORA_MAC = stringPreferencesKey("impresora_mac")
        val IMPRESORA_NOMBRE = stringPreferencesKey("impresora_nombre")
    }

    // Flow<T>: como un Observable. collect() recibe cada nuevo valor cuando cambia.
    val impresoraGuardada: Flow<ImpresoraGuardada?> = context.dataStore.data.map { prefs ->
        val mac = prefs[IMPRESORA_MAC] ?: return@map null
        val nombre = prefs[IMPRESORA_NOMBRE] ?: return@map null
        ImpresoraGuardada(nombre, mac)
    }

    suspend fun guardarImpresora(impresora: ImpresoraGuardada) {
        context.dataStore.edit { prefs ->
            prefs[IMPRESORA_MAC] = impresora.mac
            prefs[IMPRESORA_NOMBRE] = impresora.nombre
        }
    }

    // Lectura de una sola vez (para PrinterConnection antes de conectar)
    suspend fun impresoraGuardadaUnaVez(): ImpresoraGuardada? =
        context.dataStore.data.first().let { prefs ->
            val mac = prefs[IMPRESORA_MAC] ?: return null
            val nombre = prefs[IMPRESORA_NOMBRE] ?: return null
            ImpresoraGuardada(nombre, mac)
        }
}
