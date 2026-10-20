package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

/** Imported sound payload returned from bulk import. */
data class SoundWithUri(
    /** Normalized sound title. */
    val title: String,
    /** File provider URI for the imported sound. */
    val uri: Uri,
    /** File extension without a leading dot. */
    val extension: String = DEFAULT_AUDIO_EXTENSION,
)
