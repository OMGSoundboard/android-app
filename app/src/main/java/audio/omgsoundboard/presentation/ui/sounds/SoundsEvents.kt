package audio.omgsoundboard.presentation.ui.sounds

import android.net.Uri
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.presentation.theme.ThemeType


sealed class SoundsEvents {
    data class OnRestoreBackup(val uri: Uri) : SoundsEvents()
    data class OnBackupFiles(val uri: Uri) : SoundsEvents()
    data class OnSyncWear(val nodeId: String): SoundsEvents()
    data class OnSetCategoryId(val id: Int) : SoundsEvents()
    object OnToggleSearch : SoundsEvents()
    data class OnSearchTerm(val term: String) : SoundsEvents()
    data class OnPlaySound(val index: Int, val resourceId: Int?, val uri: Uri) : SoundsEvents()
    data class OnToggleFav(val id: Int) : SoundsEvents()
    data class OnShareSound(val sound: PlayableSound) : SoundsEvents()
    data class OnSetAsRingtone(val sound: PlayableSound) : SoundsEvents()
    data class OnSetAsAlarm(val sound: PlayableSound) : SoundsEvents()
    data class OnSetAsNotification(val sound: PlayableSound) : SoundsEvents()
    object OnShowHideChangeCategoryDialog: SoundsEvents()
    data class OnConfirmSoundCategoryChange(val soundId: Int, val categoryId: Int): SoundsEvents()
    data class OnShowHideAddRenameSoundDialog(
        val initialText: String,
        val isRenaming: Boolean,
        val uri: Uri = Uri.EMPTY,
    ) : SoundsEvents()

    data class OnTextFieldChange(val text: String) : SoundsEvents()
    data class OnConfirmRename(val sound: PlayableSound) : SoundsEvents()
    object OnConfirmAdd : SoundsEvents()
    data class OnAddMultipleSounds(val uris: List<Uri>) : SoundsEvents()
    object OnShowHideDeleteSoundDialog : SoundsEvents()
    data class OnConfirmDelete(val soundId: Int) : SoundsEvents()
    object OnToggleDropMenu : SoundsEvents()
    object OnToggleParticles : SoundsEvents()
    object OnShowHideThemePicker : SoundsEvents()
    data class OnChangeTheme(val theme: ThemeType) : SoundsEvents()
    data class OnNavigate(val route: String) : SoundsEvents()
}