package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text

@Composable
fun Chip(icon: Int? = null, title: String, onTap: () -> Unit) {
    androidx.wear.compose.material.Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onTap,
        label = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = if (icon != null) -(12).dp else (0).dp),
                text = title,
                textAlign = TextAlign.Center
            )
        },
        icon = if (icon != null) {
            {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null
                )
            }
        } else null
    )
}