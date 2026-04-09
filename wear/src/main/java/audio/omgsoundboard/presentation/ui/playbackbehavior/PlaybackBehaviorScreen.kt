package audio.omgsoundboard.presentation.ui.playbackbehavior

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import audio.omgsoundboard.core.R
import kotlinx.coroutines.launch

@Composable
fun PlaybackBehaviorScreen(
    viewModel: PlaybackBehaviorViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    val scalingLazyListState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingLazyListState)
        },
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                },
            state = scalingLazyListState,
        ) {
            item {
                Text(text = stringResource(R.string.options_playback_behavior))
            }

            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = state.stopOnRetap,
                    onCheckedChange = { viewModel.toggleStopOnRetap() },
                    label = { Text(stringResource(R.string.stop_on_retap)) },
                    secondaryLabel = { Text(stringResource(R.string.stop_on_retap_desc)) },
                    toggleControl = {
                        Switch(checked = state.stopOnRetap, onCheckedChange = null)
                    },
                )
            }

            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = state.stopOnNewSound,
                    onCheckedChange = { viewModel.toggleStopOnNewSound() },
                    label = { Text(stringResource(R.string.stop_on_new_sound)) },
                    secondaryLabel = { Text(stringResource(R.string.stop_on_new_sound_desc)) },
                    toggleControl = {
                        Switch(checked = state.stopOnNewSound, onCheckedChange = null)
                    },
                )
            }
        }
    }
}
