package audio.omgsoundboard.presentation.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Constants
import audio.omgsoundboard.presentation.composables.Chip
import audio.omgsoundboard.presentation.navigation.DrawerParams
import audio.omgsoundboard.presentation.navigation.Screens

@Composable
fun MainScreen(
    onNavigate: (route: String) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel(),
){
    val state = viewModel.uiState
    val scalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        state = scalingLazyListState
    ){

        item {
            Text(text = stringResource(id = R.string.categories_header))
        }

        items(state.categories){
            Chip(title = it.name) {
                onNavigate(Screens.SoundsScreen.route + "/${it.id}")
            }
        }

        item {
            Chip(title = stringResource(R.string.favorites_title)) {
                onNavigate(Screens.FavoritesScreen.route)
            }
        }

        item {
            Text(text = stringResource(id = R.string.options_header))
        }

        items(DrawerParams.drawerOptions){
            Chip(icon = it.drawableId, title = stringResource(it.title)) {
               when(it.action){
                   Constants.OPTIONS_ABOUT -> {
                       onNavigate(Screens.AboutScreen.route)
                   }
               }
            }
        }
    }
}