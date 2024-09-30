package audio.omgsoundboard.presentation.ui.favorites

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import audio.omgsoundboard.core.R
import audio.omgsoundboard.presentation.ui.sounds.SoundItem

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel()
){

    val context = LocalContext.current
    val scalingLazyListState = androidx.wear.compose.foundation.lazy.rememberScalingLazyListState()
    val state by viewModel.state.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        state = scalingLazyListState
    ) {
        item {
            if (state.sounds.isEmpty()) {
                Text(text = stringResource(id = R.string.no_sounds_here_yet))
            }
        }

        items(state.sounds, key = { it.id }) { sound ->
            SoundItem(
                item = sound,
                onPlay = {
                    viewModel.onEvent(FavoritesEvents.OnPlaySound(sound.id, sound.resId, sound.uri))
                },
                onFav = {
                    Toast.makeText(
                        context,
                        context.resources.getString(if (!sound.isFav) R.string.added_to_fav else R.string.remove_from_fav),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.onEvent(FavoritesEvents.OnToggleFav(sound.id))
                }
            )
        }
    }
}