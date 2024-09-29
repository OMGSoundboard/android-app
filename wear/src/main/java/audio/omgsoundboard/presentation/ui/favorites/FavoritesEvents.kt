package audio.omgsoundboard.presentation.ui.favorites

import android.net.Uri

sealed class FavoritesEvents {
    data class OnPlaySound(val index: Int, val resourceId: Int?, val uri: Uri) : FavoritesEvents()
    data class OnToggleFav(val id: Int) : FavoritesEvents()
}