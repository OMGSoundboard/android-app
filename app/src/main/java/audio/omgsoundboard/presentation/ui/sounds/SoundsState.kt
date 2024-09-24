package audio.omgsoundboard.presentation.ui.sounds


import android.net.Uri
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.presentation.theme.ThemeType

data class SoundsState(
    val categories : List<Category> = emptyList(),
    val sounds: List<PlayableSound> = emptyList(),
    val currentCategory: Category? = null,
    val showSearchField: Boolean = false,
    val searchTerm: String = "",
    val showDropMenu: Boolean = false,
    val showAddRenameSoundDialog: Boolean = false,
    val isRenaming: Boolean = false,
    val addedSoundUri: Uri? = Uri.EMPTY,
    val textFieldValue: String = "",
    val textFieldError: Boolean = false,
    val showConfirmDeleteDialog: Boolean = false,
    val showThemePicker: Boolean = false,
    val pickedTheme: ThemeType = ThemeType.DARK,
    val areParticlesEnable: Boolean = false,
)
