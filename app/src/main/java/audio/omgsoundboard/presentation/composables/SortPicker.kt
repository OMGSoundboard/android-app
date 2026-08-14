package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.SoundSortOrder

/** Dialog for choosing how sounds are ordered in the list. */
@Composable
fun SortPicker(
    selectedSortOrder: SoundSortOrder,
    onSortSelected: (SoundSortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(selectedSortOrder) }

    Dialog(onDismissRequest = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.sort_title),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SoundSortOrder.entries.forEach { order ->
                    Radio(
                        text = stringResource(id = order.labelResId),
                        isSelected = selected == order,
                    ) {
                        selected = order
                        onSortSelected(order)
                    }
                }
            }
        }
    }
}
