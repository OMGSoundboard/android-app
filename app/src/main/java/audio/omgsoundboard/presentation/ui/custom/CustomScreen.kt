package audio.omgsoundboard.presentation.ui.custom

import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.presentation.composables.DropMenu
import audio.omgsoundboard.presentation.composables.CustomSoundDialog
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.ui.MainViewModel
import audio.omgsoundboard.presentation.ui.sounds.SoundItem
import audio.omgsoundboard.presentation.utils.getTitleFromUri

enum class CustomDialogAction
{
    CREATE,
    RENAME
}

@Composable
fun CustomScreen(mainViewModel: MainViewModel) {

    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.setCurrentScreenValue(
            Screens.CustomScreen,
            context.getString(R.string.custom_title)
        )
        mainViewModel.getCustomSounds()
    }

    var showCustomSoundDialog by remember { mutableStateOf(false) }
    var customDialogAction by remember { mutableStateOf(CustomDialogAction.CREATE) }
    var pickedSoundUri by remember { mutableStateOf(Uri.EMPTY) }
    var pickedSoundDefaultTitle by remember { mutableStateOf("") }

    val soundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { soundUri ->
        if (soundUri != null) {
            pickedSoundDefaultTitle = getTitleFromUri(context, soundUri) ?: ""
            pickedSoundUri = soundUri
            showCustomSoundDialog = true
            customDialogAction = CustomDialogAction.CREATE
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { zipUri ->
        if (zipUri != null) {
            mainViewModel.restoreBackup(zipUri)
        }
    }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) {
        if (it != null) {
            mainViewModel.exportBackup(it)
        }
    }

    val sounds = mainViewModel.customSounds
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
                if (mainViewModel.favorites.indexOfFirst { it.uri == sound.uri } != -1) {
                    mainViewModel.changeFav(index, true)
                }

                SoundItem(title = sound.title, isFav = sound.isFav, index = index, onFav = {
                    mainViewModel.favorite(sound, isCustom = true) { isFav ->
                        Toast.makeText(
                            context,
                            context.resources.getString(if (isFav) R.string.added_to_fav else R.string.remove_from_fav),
                            Toast.LENGTH_SHORT
                        ).show()
                        mainViewModel.changeFav(index, isFav)
                    }
                }, onPlay = {
                    mainViewModel.playSound(index, 0, sound.uri)
                }, onDropMenu = {
                    touchPoint = it
                    pickedSound = sound
                    mainViewModel.toggleDropMenu()
                })
            }
        }

    }

    if (showCustomSoundDialog) {
        CustomSoundDialog(
            defaultTitle = pickedSoundDefaultTitle,
            onAdd = { title ->
                if (customDialogAction == CustomDialogAction.RENAME) {
                    val renamedSound = pickedSound.copy(title = title)
                    mainViewModel.renameCustomSound(renamedSound)
                } else {
                    mainViewModel.addCustomSound(title, pickedSoundUri)
                }
            },
            onDismiss = {
                showCustomSoundDialog = false
            }
        )
    }

    if (mainViewModel.fabPress) {
        mainViewModel.fabPress(false)
        soundPicker.launch("audio/mpeg")
    }

    if (mainViewModel.exportBackup) {
        mainViewModel.backupPress(export = false, restore = null)
        createFileLauncher.launch("OMGSoundboard_backup.zip")
    }

    if (mainViewModel.restoreBackup) {
        mainViewModel.backupPress(export = null, restore = false)
        zipPicker.launch("application/zip")
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
            touchPoint = touchPoint,
            pickedSound = pickedSound,
            hasWriteSettingsPermission = hasWriteSettingsPermission,
            mainViewModel = mainViewModel,
            askForPermission = {
                showPermissionDialog = true

            },
            isCustomSound = true,
            showRenameCustomSound = {
                pickedSoundDefaultTitle = pickedSound.title
                showCustomSoundDialog = true
                customDialogAction = CustomDialogAction.RENAME
            },
            onDismiss = {
                mainViewModel.toggleDropMenu()
            }
        )
    }

}