package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

data class SoundWithUri(
    val title: String,
    val uri: Uri,
    /** File extension without a leading dot. */
    val extension: String = DEFAULT_AUDIO_EXTENSION,
)
