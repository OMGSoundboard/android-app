package audio.omgsoundboard.presentation.ui.sounds

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound

@Composable
fun SoundsScreen(
    categoryId: Int,
    viewModel: SoundsScreenViewModel = hiltViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.onEvent(SoundsEvents.OnSetCategoryId(categoryId))
    }

    val context = LocalContext.current
    val scalingLazyListState = rememberScalingLazyListState()
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
                    viewModel.onEvent(SoundsEvents.OnPlaySound(sound.id, sound.resId, sound.uri))
                },
                onFav = {
                    Toast.makeText(
                        context,
                        context.resources.getString(if (!sound.isFav) R.string.added_to_fav else R.string.remove_from_fav),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.onEvent(SoundsEvents.OnToggleFav(sound.id))
                }
            )
        }
    }
}

@Composable
fun SoundItem(
    item: PlayableSound,
    onPlay: () -> Unit,
    onFav: () -> Unit,
) {
    Card(modifier = Modifier.padding(vertical = 2.dp), onClick = onPlay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                textAlign = TextAlign.Start
            )
            Button(onClick = onFav) {
                Icon(
                    painter = painterResource(
                        id = if (item.isFav) {
                            R.drawable.fav
                        } else {
                            R.drawable.fav_outlined
                        }
                    ),
                    contentDescription = null,
                )
            }
        }
    }
}
