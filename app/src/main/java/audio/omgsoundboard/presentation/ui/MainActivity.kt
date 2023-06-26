package audio.omgsoundboard.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import audio.omgsoundboard.presentation.composables.ThemePicker
import audio.omgsoundboard.presentation.navigation.*
import audio.omgsoundboard.presentation.theme.OMGSoundboardTheme
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.utils.Constants.CATEGORY_CUSTOM
import audio.omgsoundboard.utils.Constants.OPTIONS_ABOUT
import audio.omgsoundboard.utils.Constants.OPTIONS_PARTICLES
import audio.omgsoundboard.utils.Constants.OPTIONS_THEME_PICKER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        setContent {
            OMGSoundboardTheme(
                darkTheme = when (mainViewModel.selectedTheme) {
                    ThemeType.DARK -> {
                        true
                    }
                    ThemeType.SYSTEM -> {
                        isSystemInDarkTheme()
                    }
                    else -> {
                        false
                    }
                },
                dynamicColor = mainViewModel.selectedTheme == ThemeType.DYNAMIC
            ) {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                var showThemePicker by remember { mutableStateOf(false) }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            drawerState = drawerState,
                            defaultPick = DrawerParams.drawerCategories[0],
                            areParticlesEnable = mainViewModel.areParticlesEnabled,
                            onClick = {
                                when (it.category) {
                                    CATEGORY_CUSTOM -> {
                                        navController.navigate(Screens.CustomScreen.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                    OPTIONS_ABOUT -> {
                                        navController.navigate(Screens.AboutScreen.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                    OPTIONS_PARTICLES -> {
                                        mainViewModel.setParticlesState()
                                    }
                                    OPTIONS_THEME_PICKER -> {
                                        showThemePicker = true
                                    }
                                    else -> {
                                        navController.popBackStack()
                                        navController.navigate(Screens.CategorySoundsScreen.route + "/${it.category}") {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            AppBar(
                                viewModel = mainViewModel,
                                navController = navController,
                                openDrawer = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                })
                        },
                        floatingActionButton = {
                            if (mainViewModel.currentScreen == Screens.CustomScreen) {
                                Fab(
                                    onFabClick = { mainViewModel.fabPress(true) }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            NavigationController(
                                navController = navController,
                                viewModel = mainViewModel,
                            )
                        }
                    }
                }

                if (showThemePicker) {
                    ThemePicker(selectedThemeType = mainViewModel.selectedTheme, pickTheme = {
                        mainViewModel.setThemeType(it)
                    }, onDismiss = {
                        showThemePicker = false
                    })
                }
            }
        }
    }
}


@Composable
fun Fab(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}