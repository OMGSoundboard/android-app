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
                SortRadio(
                    text = stringResource(id = R.string.sort_alpha_asc),
                    isSelected = selected == SoundSortOrder.TITLE_ASC,
                ) {
                    selected = SoundSortOrder.TITLE_ASC
                    onSortSelected(SoundSortOrder.TITLE_ASC)
                }
                SortRadio(
                    text = stringResource(id = R.string.sort_alpha_desc),
                    isSelected = selected == SoundSortOrder.TITLE_DESC,
                ) {
                    selected = SoundSortOrder.TITLE_DESC
                    onSortSelected(SoundSortOrder.TITLE_DESC)
                }
                SortRadio(
                    text = stringResource(id = R.string.sort_most_used),
                    isSelected = selected == SoundSortOrder.MOST_USED,
                ) {
                    selected = SoundSortOrder.MOST_USED
                    onSortSelected(SoundSortOrder.MOST_USED)
                }
                SortRadio(
                    text = stringResource(id = R.string.sort_recently_added),
                    isSelected = selected == SoundSortOrder.RECENTLY_ADDED,
                ) {
                    selected = SoundSortOrder.RECENTLY_ADDED
                    onSortSelected(SoundSortOrder.RECENTLY_ADDED)
                }
            }
        }
    }
}

@Composable
private fun SortRadio(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        RadioButton(selected = isSelected, onClick = onClick)
    }
}
