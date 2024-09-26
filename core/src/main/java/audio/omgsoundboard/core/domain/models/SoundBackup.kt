package audio.omgsoundboard.core.domain.models

import audio.omgsoundboard.core.data.local.entities.SoundsEntity

data class SoundBackup(
    val id: Int = 0,
    val title: String,
    val date: Long,
    val isFavorite: Boolean = false,
    val categoryId: Int?,
    val resId: Int? = null
)

fun SoundsEntity.toBackup() = SoundBackup(
    id = id,
    title = title,
    date = date,
    isFavorite = isFavorite,
    categoryId = categoryId,
    resId = resId
)