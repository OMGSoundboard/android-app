package audio.omgsoundboard.core.domain.models

import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

/** Backup payload for a single sound. */
data class SoundBackup(
    /** Database identifier for the sound. */
    val id: Int = 0,
    /** Display title shown in the sound list. */
    val title: String,
    /** Timestamp when the sound was added. */
    val date: Long,
    /** Whether the sound appears in favorites. */
    val isFavorite: Boolean = false,
    /** Category the sound belongs to, if any. */
    val categoryId: Int?,
    /** Bundled raw resource id, when the sound is not imported. */
    val resId: Int? = null,
    /** Persisted file extension for user-imported sounds. */
    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,
    /** Number of times this sound has been played. */
    val playCount: Int = 0,
)

/** Converts a Room entity into backup metadata. */
fun SoundsEntity.toBackup() = SoundBackup(
    id = id,
    title = title,
    date = date,
    isFavorite = isFavorite,
    categoryId = categoryId,
    resId = resId,
    fileExtension = fileExtension,
    playCount = playCount,
)
