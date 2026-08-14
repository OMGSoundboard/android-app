package audio.omgsoundboard.presentation.ui.sounds


import android.net.Uri
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.SoundSortOrder
import audio.omgsoundboard.core.domain.models.WearNode
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION
import audio.omgsoundboard.presentation.theme.ThemeType

/** UI state for the main sounds screen. */
data class SoundsState(
    val categories : List<Category> = emptyList(),
    val sounds: List<PlayableSound> = emptyList(),
    val wearNodes: List<WearNode> = emptyList(),
    val currentCategory: Category? = null,
    val showSearchField: Boolean = false,
    val searchTerm: String = "",
    val showDropMenu: Boolean = false,
    val showAddRenameSoundDialog: Boolean = false,
    val showChangeCategoryDialog: Boolean = false,
    val isRenaming: Boolean = false,
    val addedSoundUri: Uri? = Uri.EMPTY,
    /** Selected file extension for a sound being added. */
    val addedSoundExtension: String = DEFAULT_AUDIO_EXTENSION,
    val textFieldValue: String = "",
    val textFieldError: Boolean = false,
    val showConfirmDeleteDialog: Boolean = false,
    val showThemePicker: Boolean = false,
    val pickedTheme: ThemeType = ThemeType.DARK,
    val areParticlesEnable: Boolean = false,
    val playbackProgress: Map<Int, Float> = emptyMap(),
    val stopOnRetap: Boolean = false,
    val stopOnNewSound: Boolean = false,
    val showPlaybackBehaviorDialog: Boolean = false,
    /** Whether the sort picker dialog is visible. */
    val showSortPicker: Boolean = false,
    /** Current sort order applied to the sound list. */
    val soundSortOrder: SoundSortOrder = SoundSortOrder.TITLE_ASC,
)
