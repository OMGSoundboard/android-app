package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

/** Domain model for a sound that can be played in the UI. */
data class PlayableSound(
    val id: Int = 0,
    /** Display title shown in the sound list. */
    val title: String = "",
    /** Content or file URI used for playback. */
    val uri: Uri = Uri.EMPTY,
    val date: Long = 0L,
    val isFav: Boolean = false,
    val categoryId: Int? = null,
    val resId: Int? = null,
    /** Persisted file extension for user-imported sounds. */
    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,
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
    fileExtension = fileExtension
)
