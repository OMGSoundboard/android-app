package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import audio.omgsoundboard.core.R

@Composable
fun AddRenameSoundDialog(
    isRename: Boolean,
    text: String,
    onChange: (String) -> Unit,
    error: Boolean,
    onFinish: () -> Unit,
    onDismiss: () -> Unit,
) {


    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text =  stringResource(id = if (isRename) R.string.update_sound else R.string.sound_add_title),
                    fontSize = 20.sp
                )
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    isError = error,
                    placeholder = {
                        Text(text = stringResource(id = R.string.custom_sound_add_placeholder))
                    },
                    onValueChange = onChange,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = onFinish
                ) {
                    Text(text = stringResource(id = if (isRename) R.string.save else  R.string.custom_sound_add), fontSize = 18.sp)
                }
            }
        }
    }
}