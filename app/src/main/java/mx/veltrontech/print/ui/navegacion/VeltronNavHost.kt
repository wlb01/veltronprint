package mx.veltrontech.print.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.veltrontech.print.AppContainer
import mx.veltrontech.print.ajustes.AjustesScreen
import mx.veltrontech.print.ajustes.AjustesViewModel
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
                onAbrirAjustes = { navController.navigate(Rutas.AJUSTES) },
            )
        }
        composable(Rutas.ELEGIR_IMPRESORA) {
            ElegirImpresoraScreen(
                container = container,
                onRegresar = { navController.popBackStack() },
            )
        }
        composable(Rutas.AJUSTES) {
            val vm = viewModel<AjustesViewModel> {
                AjustesViewModel(
                    container.logoRepository,
                    container.servicioImpresion,
                    container.preferenciasRepository,
                )
            }
            AjustesScreen(
                viewModel = vm,
                onRegresar = { navController.popBackStack() },
            )
        }
    }
}
