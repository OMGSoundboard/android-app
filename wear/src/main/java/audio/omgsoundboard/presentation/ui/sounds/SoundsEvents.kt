package audio.omgsoundboard.presentation.ui.sounds

import android.net.Uri

sealed class SoundsEvents {
    data class OnSetCategoryId(val id: Int) : SoundsEvents()
    data class OnPlaySound(val index: Int, val resourceId: Int?, val uri: Uri) : SoundsEvents()
    data class OnToggleFav(val id: Int) : SoundsEvents()
}