package mx.veltrontech.print

import android.app.Application

// Application: clase que Android instancia antes que cualquier Activity.
// Se registra en AndroidManifest con android:name=".VeltronPrintApp".
class VeltronPrintApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
