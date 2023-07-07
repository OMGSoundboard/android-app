package audio.omgsoundboard.presentation.ui.favorites

import android.provider.Settings
import android.widget.Toast
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
import audio.omgsoundboard.presentation.composables.Particles
import audio.omgsoundboard.presentation.composables.PermissionDialog
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.ui.MainViewModel
import audio.omgsoundboard.presentation.ui.sounds.SoundItem

@Composable
fun FavoritesScreen(
    mainViewModel: MainViewModel
){

    val context = LocalContext.current
    var hasWriteSettingsPermission by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        mainViewModel.setCurrentScreenValue(Screens.FavoritesScreen, context.getString(R.string.favorites_title))
    }

    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    var pickedSound by remember { mutableStateOf(PlayableSound()) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        if (mainViewModel.areParticlesEnabled){
            Particles()
        }

        if (mainViewModel.favorites.isEmpty()){
            Text(text = stringResource(id = R.string.no_sounds_here_yet))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ){
            itemsIndexed(mainViewModel.favorites){index, sound ->
                SoundItem(title = sound.title, isFav = true, index = index, onFav = {
                    mainViewModel.favorite(sound){
                        Toast.makeText(context, context.resources.getString(R.string.remove_from_fav), Toast.LENGTH_SHORT).show()
                    }
                }, onPlay = {
                    mainViewModel.playSound(index, sound.resId, sound.uri)
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

    if (mainViewModel.isDropMenuExpanded){
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