package audio.omgsoundboard.presentation.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Fab(
    modifier: Modifier = Modifier,
    onFabClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier,
        onClick = onFabClick,
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}