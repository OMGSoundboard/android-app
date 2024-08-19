package audio.omgsoundboard.core.domain.models

import android.net.Uri
import audio.omgsoundboard.core.data.local.CustomSoundsEntity
import audio.omgsoundboard.core.data.local.FavoritesEntity

data class PlayableSound(
    var id: Int = 0,
    val title: String = "",
    val resId: Int = 0,
    val uri: Uri = Uri.EMPTY,
    var isFav: Boolean = false,
    val date: Long = 0L
)

fun FavoritesEntity.toDomain() = PlayableSound(
    id = id,
    title = title,
    resId = resId,
    uri = uri
)

fun CustomSoundsEntity.toDomain() = PlayableSound(
    id = id,
    title = title,
    uri = uri,
    date = date
)