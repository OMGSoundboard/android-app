package audio.omgsoundboard.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.navigation.NavigationController
import audio.omgsoundboard.presentation.theme.OMGSoundboardTheme
import audio.omgsoundboard.presentation.theme.ThemeType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel by viewModels<MainViewModel>()
        splashScreen.setKeepOnScreenCondition { mainViewModel.keepSplash }

        setContent {
            OMGSoundboardTheme(
                darkTheme = when (mainViewModel.selectedTheme) {
                    ThemeType.DARK -> true
                    ThemeType.SYSTEM ->  isSystemInDarkTheme()
                    else -> false
                },
                dynamicColor = mainViewModel.selectedTheme == ThemeType.DYNAMIC
            ) {

                val navController = rememberNavController()
                Box(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
                ) {
                    if (mainViewModel.areParticlesEnabled) {
                        Particles()
                    }

                    NavigationController(
                        mainViewModel = mainViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}


