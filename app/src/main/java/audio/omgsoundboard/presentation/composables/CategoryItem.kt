package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import audio.omgsoundboard.core.domain.models.Category
import kotlinx.coroutines.launch


@Composable
fun CategoryItem(
    item: Category,
    index: Int,
    onDropMenu: (Offset) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var layoutCoordinates: LayoutCoordinates? = null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 6.dp)
            .onGloballyPositioned { coordinates ->
                layoutCoordinates = coordinates
            }
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        scope.launch {
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            layoutCoordinates?.let { cords ->
                                val globalOffset = cords.localToWindow(offset)
                                onDropMenu(globalOffset)
                            }
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    }
                )
            },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (index % 2 == 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .width(3.dp)
                    .height(60.dp)
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                text = item.name,
            )
        }
    }
}