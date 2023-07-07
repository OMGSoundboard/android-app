package audio.omgsoundboard.presentation.ui.favorites

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import audio.omgsoundboard.core.R
import audio.omgsoundboard.presentation.ui.WearViewModel
import audio.omgsoundboard.presentation.ui.sounds.SoundItem

@Composable
fun FavoritesScreen(viewModel: WearViewModel){

    val context = LocalContext.current
    val scalingLazyListState = rememberScalingLazyListState()

    val sounds = viewModel.favorites

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        state = scalingLazyListState
    ) {

        item {
            if (sounds.isEmpty()) {
                Text(text = stringResource(id = R.string.no_sounds_here_yet))
            }
        }

        itemsIndexed(sounds) { index, sound ->

            SoundItem(title = sound.title, isFav = true, onPlay = {
                viewModel.playSound(index, sound.resId, null)
            }, onFav = {
                viewModel.favorite(sound) {
                    Toast.makeText(context, context.resources.getString(R.string.remove_from_fav), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}