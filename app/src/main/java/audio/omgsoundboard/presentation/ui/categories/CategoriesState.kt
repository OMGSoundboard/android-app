package audio.omgsoundboard.presentation.ui.categories


import audio.omgsoundboard.core.domain.models.Category

data class CategoriesState(
    val categories : List<Category> = emptyList(),
    val showAddRenameCategoryDialog: Boolean = false,
    val isRenaming: Boolean = false,
    val textFieldValue: String = "",
    val textFieldError: Boolean = false,
    val showDropMenu : Boolean = false,
    val showConfirmDeleteDialog: Boolean = false
)
