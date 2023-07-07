package audio.omgsoundboard.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.theme.OMGSoundboardTheme
import audio.omgsoundboard.presentation.ui.favorites.FavoritesScreen
import audio.omgsoundboard.presentation.ui.main.MainScreen
import audio.omgsoundboard.presentation.ui.sounds.SoundsScreen
import audio.omgsoundboard.core.utils.Constants
import audio.omgsoundboard.core.utils.Constants.CATEGORY_FAVORITES
import audio.omgsoundboard.core.utils.Constants.OPTIONS_ABOUT
import audio.omgsoundboard.presentation.ui.about.AboutScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel: WearViewModel by viewModels()

        setContent {
            OMGSoundboardTheme {

                val navController = rememberSwipeDismissableNavController()

                Scaffold(
                    timeText = {
                        TimeText(
                            timeTextStyle = TimeTextDefaults.timeTextStyle(
                                fontSize = 10.sp
                            )
                        )
                    },
                    vignette = {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                ) {
                    
                    SwipeDismissableNavHost(navController = navController, startDestination = Screens.MenuScreen.route ){

                        composable(route = Screens.MenuScreen.route){
                            MainScreen(onNavigate = { screen, category ->
                                when (category) {
                                    CATEGORY_FAVORITES, OPTIONS_ABOUT -> {
                                        navController.navigate(screen.route){
                                            launchSingleTop = true
                                        }
                                    }
                                    else -> {
                                        navController.navigate(screen.route + "/$category"){
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            })
                        }

                        composable(
                            route = Screens.CategorySoundsScreen.route + "/{category}",
                            arguments = listOf(
                                navArgument("category") {
                                    type = NavType.StringType
                                    defaultValue = Constants.CATEGORY_ALL
                                },
                            )
                        ){entry ->
                            SoundsScreen(category = entry.arguments?.getString("category")!!, viewModel = mainViewModel)
                        }

                        composable(route = Screens.FavoritesScreen.route){
                            FavoritesScreen(viewModel = mainViewModel)
                        }

                        composable(route = Screens.AboutScreen.route){
                            AboutScreen()
                        }

                    }
                }
            }
        }
    }
}



