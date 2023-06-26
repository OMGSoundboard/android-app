package audio.omgsoundboard.presentation.composables

import android.widget.Toast
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import audio.omgsoundboard.R
import audio.omgsoundboard.domain.models.PlayableSound
import audio.omgsoundboard.domain.repository.MediaManager
import audio.omgsoundboard.presentation.ui.MainViewModel


@Composable
fun DropMenu(
    touchPoint: Offset,
    pickedSound: PlayableSound,
    hasWriteSettingsPermission: Boolean,
    mainViewModel: MainViewModel,
    askForPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    val (xDp, yDp) = with(density) { (touchPoint.x.toDp()) to (touchPoint.y.toDp()) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        DropdownMenu(
            offset = DpOffset(xDp, -maxHeight + yDp),
            expanded = isExpanded,
            onDismissRequest = onDismiss,
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.drop_menu_share))
                },
                onClick = {
                    isExpanded = !isExpanded
                    mainViewModel.shareSound(pickedSound.title, pickedSound.resId)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.drop_menu_ringtone))
                },
                onClick = {
                    isExpanded = !isExpanded
                    if (hasWriteSettingsPermission) {
                        mainViewModel.setMedia(
                            MediaManager.Ringtone,
                            pickedSound.title,
                            pickedSound.resId,
                            pickedSound.uri
                        )
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.ringtone_set),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        askForPermission()
                    }
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.drop_menu_alarm))
                },
                onClick = {
                    isExpanded = !isExpanded
                    if (hasWriteSettingsPermission) {
                        mainViewModel.setMedia(
                            MediaManager.Alarm,
                            pickedSound.title,
                            pickedSound.resId,
                            pickedSound.uri
                        )
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.alarm_set),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        askForPermission()
                    }
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.drop_menu_notification))
                },
                onClick = {
                    isExpanded = !isExpanded
                    if (hasWriteSettingsPermission) {
                        mainViewModel.setMedia(
                            MediaManager.Notification,
                            pickedSound.title,
                            pickedSound.resId,
                            pickedSound.uri
                        )
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.notification_set),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        askForPermission()
                    }
                },
            )
        }
    }
}