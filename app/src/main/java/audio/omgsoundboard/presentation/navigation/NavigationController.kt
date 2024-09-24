package audio.omgsoundboard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import audio.omgsoundboard.presentation.ui.MainViewModel
import audio.omgsoundboard.presentation.ui.about.AboutScreen
import audio.omgsoundboard.presentation.ui.favorites.FavoritesScreen
import audio.omgsoundboard.presentation.ui.onboarding.OnboardingScreen
import audio.omgsoundboard.presentation.ui.sounds.SoundsScreen

@Composable
fun NavigationController(
    navController: NavController,
    mainViewModel: MainViewModel,
) {

    NavHost(
        navController = navController as NavHostController,
        startDestination = if (mainViewModel.onboardingShown) {
           Graph.HOME
        } else {
            Graph.ONBOARDING
        }
    ) {

        navigation(
            startDestination = Screens.OnboardingScreen.route, route = Graph.ONBOARDING
        ) {
            composable(route = Screens.OnboardingScreen.route){
                OnboardingScreen(
                    setOnboardingAsShown = {
                        mainViewModel.setOnboardingAsShown()
                        navController.popBackStack()
                        navController.navigate(Graph.HOME)
                    },
                )
            }
        }

        navigation(
            startDestination = Screens.SoundsScreen.route,
            route = Graph.HOME
        ) {
            composable(
                route = Screens.SoundsScreen.route,
            ) {
                SoundsScreen(onNavigate = {
                    navController.navigate(it)
                })
            }

            composable(route = Screens.FavoritesScreen.route) {
                FavoritesScreen(onNavigateUp = {
                    navController.navigateUp()
                })
            }

            composable(route = Screens.AboutScreen.route) {
                AboutScreen(onNavigateUp = {
                    navController.navigateUp()
                })
            }
        }
    }
}