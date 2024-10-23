package audio.omgsoundboard.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.theme.OMGSoundboardTheme
import audio.omgsoundboard.presentation.ui.about.AboutScreen
import audio.omgsoundboard.presentation.ui.favorites.FavoritesScreen
import audio.omgsoundboard.presentation.ui.main.MainScreen
import audio.omgsoundboard.presentation.ui.sounds.SoundsScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

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
                            MainScreen(onNavigate = { route ->
                                navController.navigate(route){
                                    launchSingleTop = true
                                }
                            })
                        }

                        composable(
                            route = Screens.SoundsScreen.route + "/{categoryId}",
                            arguments = listOf(
                                navArgument("categoryId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                },
                            )
                        ){entry ->
                            SoundsScreen(categoryId = entry.arguments?.getInt("categoryId")!!)
                        }

                        composable(route = Screens.FavoritesScreen.route){
                            FavoritesScreen()
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



