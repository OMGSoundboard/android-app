package audio.omgsoundboard.presentation.ui.sounds

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.utils.Constants
import audio.omgsoundboard.presentation.ui.WearViewModel

@Composable
fun SoundsScreen(category: String, viewModel: WearViewModel) {

    val context = LocalContext.current
    val scalingLazyListState = rememberScalingLazyListState()
    val sounds = remember { mutableStateListOf<PlayableSound>() }

    LaunchedEffect(category) {
        var soundsArray = 0
        var soundsIdsArray = 0

        when (category) {
            Constants.CATEGORY_ALL -> {
                soundsArray = R.array.all
                soundsIdsArray = R.array.all_ids
            }
            Constants.CATEGORY_FUNNY -> {
                soundsArray = R.array.funny
                soundsIdsArray = R.array.funny_ids
            }
            Constants.CATEGORY_GAMES -> {
                soundsArray = R.array.games
                soundsIdsArray = R.array.games_ids
            }
            Constants.CATEGORY_MOVIES -> {
                soundsArray = R.array.movies
                soundsIdsArray = R.array.movies_ids
            }
            Constants.CATEGORY_MUSIC -> {
                soundsArray = R.array.music
                soundsIdsArray = R.array.music_ids
            }
        }

        val allSounds = context.resources.getStringArray(soundsArray)
        val allSoundsIds = context.resources.obtainTypedArray(soundsIdsArray)

        for (i in 0 until allSoundsIds.length()) {
            sounds.add(
                PlayableSound(
                    id = 0,
                    title = allSounds[i],
                    resId = allSoundsIds.getResourceId(i, 0),
                    isFav = false
                )
            )
        }

        allSoundsIds.recycle()
    }

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
            if (viewModel.favorites.indexOfFirst { it.resId == sound.resId } != -1) {
                sounds[index] = sounds[index].copy(isFav = true)
            }
            SoundItem(title = sound.title, isFav = sound.isFav, onPlay = {
                viewModel.playSound(index, sound.resId, null)
            }, onFav = {
                viewModel.favorite(sound) { isFav ->
                    sounds[index] = sounds[index].copy(isFav = isFav)
                    Toast.makeText(
                        context,
                        context.resources.getString(if (isFav) R.string.added_to_fav else R.string.remove_from_fav),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}

@Composable
fun SoundItem(
    title: String,
    isFav: Boolean,
    onPlay: () -> Unit,
    onFav: () -> Unit
) {
    Card(modifier = Modifier.padding(vertical = 2.dp), onClick = onPlay) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start
            )
            Button(onClick = onFav) {
                Icon(
                    painter = painterResource(
                        id = if (isFav) {
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
