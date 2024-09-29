package audio.omgsoundboard.presentation.ui.favorites


import audio.omgsoundboard.core.domain.models.PlayableSound

data class FavoritesState(
    val sounds : List<PlayableSound> = emptyList(),
)
