package audio.omgsoundboard.presentation.ui.main


import audio.omgsoundboard.core.domain.models.Category

data class MainState(
    val categories : List<Category> = emptyList(),
)
