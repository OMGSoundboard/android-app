@file:OptIn(ExperimentalMaterial3Api::class)

package audio.omgsoundboard.presentation.ui.favorites

import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.presentation.composables.AddRenameSoundDialog
import audio.omgsoundboard.presentation.composables.DropMenu
import audio.omgsoundboard.presentation.composables.InfoDialog
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.composables.SoundItem
import audio.omgsoundboard.presentation.utils.UiEvent


@Composable
fun FavoritesScreen(
    onNavigateUp: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
){

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateUp -> onNavigateUp()
                else -> Unit
            }
        }
    }

    val state by viewModel.state.collectAsState()
    FavoritesScreenContent(state, viewModel::onEvent)
}

@Composable
fun FavoritesScreenContent(
    state: FavoritesState,
    onEvents: (FavoritesEvents) -> Unit
){
    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    var pickedSound by remember { mutableStateOf(PlayableSound()) }

    Column {
        TopAppBar(
            title = {
                Text(text = stringResource(R.string.favorites_title))
            },
            navigationIcon = {
                IconButton(onClick = {
                    onEvents(FavoritesEvents.OnNavigateUp)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ){

            item {
                if (state.sounds.isEmpty()){
                    Text(
                        text = stringResource(id = R.string.no_sounds_here_yet),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp)
                    )
                }
            }

            itemsIndexed(state.sounds, key = { _, sound -> sound.id }) { index, sound ->
                SoundItem(
                    item = sound,
                    index = index,
                    onFav = {
                        onEvents(FavoritesEvents.OnToggleFav(sound.id))
                    },
                    onPlay = {
                        onEvents(FavoritesEvents.OnPlaySound(sound.id, sound.resId, sound.uri))
                    },
                    onDropMenu = {
                        touchPoint = it
                        pickedSound = sound
                        onEvents(FavoritesEvents.OnToggleDropMenu)
                    }
                )
            }
        }
    }

    if (state.showDropMenu) {
        DropMenu(
            touchPoint = touchPoint,
            hasWriteSettingsPermission = hasWriteSettingsPermission,
            askForPermission = {
                showPermissionDialog = true
            },
            onShare = {
                onEvents(FavoritesEvents.OnShareSound(pickedSound))
            },
            onSetAsRingtone = {
                onEvents(FavoritesEvents.OnSetAsRingtone(pickedSound))
            },
            onSetAsAlarm = {
                onEvents(FavoritesEvents.OnSetAsAlarm(pickedSound))
            },
            onSetAsNotification = {
                onEvents(FavoritesEvents.OnSetAsNotification(pickedSound))
            },
            onRename = {
                onEvents(FavoritesEvents.OnShowHideRenameSoundDialog(pickedSound.title))
            },
            onDelete = {
                onEvents(FavoritesEvents.OnShowHideDeleteSoundDialog)
            },
            onDismiss = {
                onEvents(FavoritesEvents.OnToggleDropMenu)
            }
        )
    }

    if (state.showRenameSoundDialog){
        AddRenameSoundDialog(
            isRename = true,
            text = state.textFieldValue,
            onChange = {
                onEvents(FavoritesEvents.OnTextFieldChange(it))
            },
            error = state.textFieldError,
            onFinish = {
                onEvents(FavoritesEvents.OnConfirmRename(pickedSound))
            },
            onDismiss = {
                onEvents(FavoritesEvents.OnShowHideRenameSoundDialog(""))
            }
        )
    }

    if (state.showConfirmDeleteDialog){
        InfoDialog(
            text = stringResource(R.string.delete_sound_confirm),
            onConfirmation = {
                onEvents(FavoritesEvents.OnConfirmDelete(pickedSound.id))
                onEvents(FavoritesEvents.OnShowHideDeleteSoundDialog)
            },
            onDismissRequest = {
                onEvents(FavoritesEvents.OnShowHideDeleteSoundDialog)
            }
        )
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
}