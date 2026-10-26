package mx.veltrontech.print.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.veltrontech.print.AppContainer
import mx.veltrontech.print.bluetooth.ElegirImpresoraScreen
import mx.veltrontech.print.inicio.InicioScreen

@Composable
fun VeltronNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Rutas.INICIO) {
        composable(Rutas.INICIO) {
            InicioScreen(
                container = container,
                onVincularImpresora = { navController.navigate(Rutas.ELEGIR_IMPRESORA) },
            )
        }
        composable(Rutas.ELEGIR_IMPRESORA) {
            ElegirImpresoraScreen(
                container = container,
                onRegresar = { navController.popBackStack() },
            )
        }
    }
}
