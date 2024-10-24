package audio.omgsoundboard.presentation.ui.main

import android.widget.Toast
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.utils.Constants
import audio.omgsoundboard.presentation.composables.Chip
import audio.omgsoundboard.presentation.navigation.DrawerParams
import audio.omgsoundboard.presentation.navigation.Screens
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onNavigate: (route: String) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val scalingLazyListState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingLazyListState)
        },
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                },
            verticalArrangement = Arrangement.Top,
            state = scalingLazyListState
        ) {

            item {
                Text(text = stringResource(id = R.string.categories_header))
            }

            items(state.categories) {
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

            items(DrawerParams.drawerOptions) {
                Chip(icon = it.drawableId, title = stringResource(it.title)) {
                    when (it.action) {
                        Constants.OPTIONS_ABOUT -> {
                            onNavigate(Screens.AboutScreen.route)
                        }

                        Constants.OPTIONS_SYNC -> {
                            viewModel.sync()
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.data_synced_confirm),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}