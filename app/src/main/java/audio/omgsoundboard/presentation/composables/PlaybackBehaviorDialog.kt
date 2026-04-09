package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.R

@Composable
fun PlaybackBehaviorDialog(
    stopOnRetap: Boolean,
    stopOnNewSound: Boolean,
    onToggleStopOnRetap: () -> Unit,
    onToggleStopOnNewSound: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.options_playback_behavior)) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.stop_on_retap))
                        Text(
                            text = stringResource(R.string.stop_on_retap_desc),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = stopOnRetap,
                        onCheckedChange = { onToggleStopOnRetap() },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.stop_on_new_sound))
                        Text(
                            text = stringResource(R.string.stop_on_new_sound_desc),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = stopOnNewSound,
                        onCheckedChange = { onToggleStopOnNewSound() },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
