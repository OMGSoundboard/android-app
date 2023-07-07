package audio.omgsoundboard.presentation.ui.sounds

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.presentation.composables.DropMenu
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.ui.MainViewModel
import audio.omgsoundboard.core.utils.Constants.CATEGORY_ALL
import audio.omgsoundboard.core.utils.Constants.CATEGORY_FUNNY
import audio.omgsoundboard.core.utils.Constants.CATEGORY_GAMES
import audio.omgsoundboard.core.utils.Constants.CATEGORY_MOVIES
import audio.omgsoundboard.core.utils.Constants.CATEGORY_MUSIC
import kotlinx.coroutines.launch


@Composable
fun SoundsScreen(
    category: String,
    mainViewModel: MainViewModel,
) {

    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }


    val sounds = remember { mutableStateListOf<PlayableSound>() }
    val soundsTemp = remember { mutableStateListOf<PlayableSound>() }

    LaunchedEffect(category) {
        mainViewModel.setCurrentScreenValue(Screens.CategorySoundsScreen, category)
        var soundsArray = 0
        var soundsIdsArray = 0

        when (category) {
            CATEGORY_ALL -> {
                soundsArray = R.array.all
                soundsIdsArray = R.array.all_ids
            }
            CATEGORY_FUNNY -> {
                soundsArray = R.array.funny
                soundsIdsArray = R.array.funny_ids
            }
            CATEGORY_GAMES -> {
                soundsArray = R.array.games
                soundsIdsArray = R.array.games_ids
            }
            CATEGORY_MOVIES -> {
                soundsArray = R.array.movies
                soundsIdsArray = R.array.movies_ids
            }
            CATEGORY_MUSIC -> {
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
            soundsTemp.add(
                PlayableSound(
                    id = 0,
                    title = allSounds[i],
                    resId = allSoundsIds.getResourceId(i, 0),
                    isFav = false
                )
            )
        }

        allSoundsIds.recycle()

        if (!hasWriteSettingsPermission) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(mainViewModel.searchText) {
        sounds.clear()
        if (mainViewModel.searchText == "") {
            sounds.addAll(soundsTemp)
        } else {
            soundsTemp.forEach {
                if (it.title.lowercase().contains(mainViewModel.searchText.lowercase())) {
                    sounds.add(it)
                }
            }
        }
    }

    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    var pickedSound by remember { mutableStateOf(PlayableSound()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (mainViewModel.areParticlesEnabled) {
            Particles()
        }
        if (sounds.isEmpty()) {
            Text(text = stringResource(id = R.string.no_sounds_here_yet))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            itemsIndexed(sounds) { index, sound ->
                if (mainViewModel.favorites.indexOfFirst { it.resId == sound.resId } != -1) {
                    sounds[index] = sounds[index].copy(isFav = true)
                }

                SoundItem(title = sound.title, isFav = sound.isFav, index = index, onFav = {
                    mainViewModel.favorite(sound) { isFav ->
                        sounds[index] = sounds[index].copy(isFav = isFav)
                        Toast.makeText(
                            context,
                            context.resources.getString(if (isFav) R.string.added_to_fav else R.string.remove_from_fav),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, onPlay = {
                    mainViewModel.playSound(index, sound.resId, null)
                }, onDropMenu = {
                    touchPoint = it
                    pickedSound = sound
                    mainViewModel.toggleDropMenu()
                })
            }
        }
    }


    if (showPermissionDialog) {
        PermissionDialog(
            result = {
                hasWriteSettingsPermission = it
                showPermissionDialog = false
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }

    if (mainViewModel.isDropMenuExpanded) {
        DropMenu(
            touchPoint,
            pickedSound,
            hasWriteSettingsPermission,
            mainViewModel,
            askForPermission = {
                showPermissionDialog = true
            },
            onDismiss = {
                mainViewModel.toggleDropMenu()
            }
        )
    }
}

@Composable
fun SoundItem(
    title: String,
    isFav: Boolean,
    index: Int,
    onFav: () -> Unit,
    onPlay: () -> Unit,
    onDropMenu: (Offset) -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 6.dp)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        scope.launch {
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                        onPlay()
                    },
                    onLongPress = {
                        onDropMenu(it)
                    }
                )
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (index % 2 == 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .width(3.dp)
                    .height(60.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    text = title
                )
                IconButton(onClick = onFav) {
                    Icon(
                        painter = painterResource(
                            id = if (isFav) {
                                R.drawable.fav
                            } else {
                                R.drawable.fav_outlined
                            }
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}




