package audio.omgsoundboard.presentation.ui.favorites


import audio.omgsoundboard.core.domain.models.PlayableSound

data class FavoritesState(
    val sounds: List<PlayableSound> = emptyList(),
    val showDropMenu: Boolean = false,
    val showRenameSoundDialog: Boolean = false,
    val textFieldValue: String = "",
    val textFieldError: Boolean = false,
    val showConfirmDeleteDialog: Boolean = false,
)
