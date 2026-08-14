package audio.omgsoundboard.core.domain.models

import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

data class SoundBackup(
    val id: Int = 0,
    val title: String,
    val date: Long,
    val isFavorite: Boolean = false,
    val categoryId: Int?,
    val resId: Int? = null,
    /** Persisted file extension for user-imported sounds. */
    val fileExtension: String = DEFAULT_AUDIO_EXTENSION,
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
