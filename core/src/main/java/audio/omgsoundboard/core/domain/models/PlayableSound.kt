package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.data.local.entities.SoundsEntity

data class PlayableSound(
    val id: Int = 0,
    val title: String = "",
    val uri: Uri = Uri.EMPTY,
    val date: Long = 0L,
    val isFav: Boolean = false,
    val categoryId: Int? = null,
    val resId: Int? = null,
)

fun SoundsEntity.toDomain() = PlayableSound(
    id = id,
    title = title,
    uri = uri,
    date = date,
    isFav = isFavorite,
    categoryId = categoryId,
    resId = resId,
)