package audio.omgsoundboard.presentation.ui.sounds


import audio.omgsoundboard.core.domain.models.PlayableSound

data class SoundsState(
    val sounds : List<PlayableSound> = emptyList(),
    val currentCategoryId: Int = -1,
)
