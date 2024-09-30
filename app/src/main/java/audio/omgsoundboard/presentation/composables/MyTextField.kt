package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MyTextField(
    searchTerm: String,
    onValueChange: (String) -> Unit,
    cancelSearch: () -> Unit,
){
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        value = searchTerm,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = if (searchTerm.isBlank()) {
            {
                IconButton(onClick = cancelSearch) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        trailingIcon = {
            val vector = if (searchTerm.isBlank()) {
                Icons.Default.Search
            } else {
                Icons.Default.Cancel
            }
            IconButton( onClick = cancelSearch) {
                Icon(
                    imageVector = vector,
                    tint = MaterialTheme.colorScheme.outline,
                    contentDescription = null
                )
            }
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
        )
    )
}