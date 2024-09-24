package audio.omgsoundboard.presentation.composables


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import audio.omgsoundboard.core.R

@Composable
fun InfoDialog(
    title: Int = android.R.string.dialog_alert_title,
    text: String,
    showInfoIcon: Boolean = true,
    showDismissButton: Boolean = false,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
){
    AlertDialog(
        icon =  {
            if (showInfoIcon){
                Icon(Icons.Default.Info, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
            }
        },
        title = {
            Text(text = stringResource(id = title))
        },
        text = {
            Text(text = text)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(id = if (showDismissButton) R.string.continue_dialog else R.string.ok))
            }
        },
        dismissButton = if (showDismissButton){
            @Composable {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        } else {
            null
        }
    )
}


@Preview(showBackground = true)
@Composable
fun InfoDialogPreview() {
    InfoDialog(text = "Something went bad", onDismissRequest =  {}, onConfirmation = {})
}