package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

/** Domain model for a sound that can be played in the UI. */
data class PlayableSound(
    /** Database identifier for the sound. */
    val id: Int = 0,
    /** Display title shown in the sound list. */
    val title: String = "",
    /** Content or file URI used for playback. */
    val uri: Uri = Uri.EMPTY,
    /** Timestamp when the sound was added. */
    val date: Long = 0L,
    /** Whether the sound appears in favorites. */
    val isFav: Boolean = false,
    /** Category the sound belongs to, if any. */
    val categoryId: Int? = null,
    /** Bundled raw resource id, when the sound is not imported. */
    val resId: Int? = null,
    /** Persisted file extension for user-imported sounds. */
    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,
    /** Number of times this sound has been played. */
    val playCount: Int = 0,
)

/** Converts a Room entity into a domain sound. */
fun SoundsEntity.toDomain() = PlayableSound(
    id = id,
    title = title,
    uri = uri,
    date = date,
    isFav = isFavorite,
    categoryId = categoryId,
    resId = resId,
    fileExtension = fileExtension,
    playCount = playCount,
)
