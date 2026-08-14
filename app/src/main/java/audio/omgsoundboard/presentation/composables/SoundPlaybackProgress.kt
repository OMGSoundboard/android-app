package audio.omgsoundboard.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Returns live playback progress for [soundId], or null when that sound is not playing.
 *
 * Subscribes per sound row so progress updates do not recompose the whole list.
 */
@Composable
fun rememberSoundPlaybackProgress(
    soundId: Int,
    playbackProgress: StateFlow<Map<Int, Float>>,
): Float? {
    var progress by remember(soundId) { mutableStateOf<Float?>(null) }
    LaunchedEffect(soundId) {
        playbackProgress
            .map { progressBySoundId -> progressBySoundId[soundId] }
            .distinctUntilChanged()
            .collect { progress = it }
    }
    return progress
}
