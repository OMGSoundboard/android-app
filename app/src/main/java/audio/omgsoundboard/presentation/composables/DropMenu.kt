package audio.omgsoundboard.presentation.composables

import android.widget.Toast
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import audio.omgsoundboard.core.R


@Composable
fun DropMenu(
    touchPoint: Offset,
    hasWriteSettingsPermission: Boolean,
    askForPermission: () -> Unit,
    onShare: () -> Unit,
    onSetAsRingtone: () -> Unit,
    onSetAsAlarm: () -> Unit,
    onSetAsNotification: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
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
                    onShare()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.drop_menu_ringtone))
                },
                onClick = {
                    isExpanded = !isExpanded
                    if (hasWriteSettingsPermission) {
                        onSetAsRingtone()
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
                        onSetAsAlarm()
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
                        onSetAsNotification()
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
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.rename))
                },
                onClick = {
                    isExpanded = !isExpanded
                    onRename()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.delete))
                },
                onClick = {
                    isExpanded = !isExpanded
                    onDelete()
                },
            )
        }
    }
}


@Composable
fun SimpleDropMenu(
    touchPoint: Offset,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
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
                    Text(stringResource(id = R.string.rename))
                },
                onClick = {
                    isExpanded = !isExpanded
                    onRename()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.delete))
                },
                onClick = {
                    isExpanded = !isExpanded
                    onDelete()
                },
            )
        }
    }
}