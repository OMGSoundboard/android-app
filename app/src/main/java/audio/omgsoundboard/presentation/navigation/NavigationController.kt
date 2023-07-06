package audio.omgsoundboard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import audio.omgsoundboard.presentation.ui.MainViewModel
import audio.omgsoundboard.presentation.ui.about.AboutScreen
import audio.omgsoundboard.presentation.ui.custom.CustomScreen
import audio.omgsoundboard.presentation.ui.favorites.FavoritesScreen
import audio.omgsoundboard.presentation.ui.onboarding.OnboardingScreen
import audio.omgsoundboard.presentation.ui.sounds.SoundsScreen
import audio.omgsoundboard.utils.Constants.CATEGORY_ALL

@Composable
fun NavigationController(
    navController: NavController,
    viewModel: MainViewModel,
) {

    NavHost(
        navController = navController as NavHostController,
        startDestination = if (viewModel.onboardingShown) {
            Screens.CategorySoundsScreen.route + "/{category}"
        } else {
            Screens.OnboardingScreen.route
        }
    ) {

        composable(route = Screens.OnboardingScreen.route){
            OnboardingScreen(mainViewModel = viewModel, onNavigate = {
                navController.popBackStack()
                navController.navigate(Screens.CategorySoundsScreen.route + "/$CATEGORY_ALL") {
                    launchSingleTop = true
                }
            })
        }

        composable(
            route = Screens.CategorySoundsScreen.route + "/{category}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = CATEGORY_ALL
                },
            )
        ) { entry ->
            SoundsScreen(
                category = entry.arguments?.getString("category")!!,
                mainViewModel = viewModel
            )
        }

        composable(route = Screens.CustomScreen.route) {
            CustomScreen(mainViewModel = viewModel)
        }

        composable(route = Screens.FavoritesScreen.route) {
            FavoritesScreen(mainViewModel = viewModel)
        }

        composable(route = Screens.AboutScreen.route) {
            AboutScreen(mainViewModel = viewModel)
        }


    }


}