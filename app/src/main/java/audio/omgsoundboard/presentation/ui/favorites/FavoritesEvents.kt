package audio.omgsoundboard.presentation.ui.favorites

import android.net.Uri
import audio.omgsoundboard.core.domain.models.PlayableSound


sealed class FavoritesEvents {
    data class OnPlaySound(val index: Int, val resourceId: Int?, val uri: Uri) : FavoritesEvents()
    data class OnToggleFav(val id: Int) : FavoritesEvents()
    data class OnShareSound(val sound: PlayableSound) : FavoritesEvents()
    data class OnSetAsRingtone(val sound: PlayableSound) : FavoritesEvents()
    data class OnSetAsAlarm(val sound: PlayableSound) : FavoritesEvents()
    data class OnSetAsNotification(val sound: PlayableSound) : FavoritesEvents()
    data class OnShowHideRenameSoundDialog(val initialText: String) : FavoritesEvents()
    data class OnTextFieldChange(val text: String) : FavoritesEvents()
    data class OnConfirmRename(val sound: PlayableSound) : FavoritesEvents()
    object OnShowHideDeleteSoundDialog : FavoritesEvents()
    data class OnConfirmDelete(val soundId: Int) : FavoritesEvents()
    object OnToggleDropMenu : FavoritesEvents()
    object OnNavigateUp : FavoritesEvents()
}