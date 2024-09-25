package audio.omgsoundboard.presentation.ui.categories

import audio.omgsoundboard.core.domain.models.Category


sealed class CategoriesEvents {
    data class OnShowHideAddRenameCategoryDialog(val initialText: String, val isRenaming: Boolean) : CategoriesEvents()
    data class OnTextFieldChange(val text: String) : CategoriesEvents()
    data class OnConfirmRename(val category: Category) : CategoriesEvents()
    object OnConfirmAdd : CategoriesEvents()
    object OnShowHideDeleteCategoryDialog : CategoriesEvents()
    data class OnConfirmDelete(val id: Int): CategoriesEvents()
    object OnToggleDropMenu : CategoriesEvents()
    object OnNavigateUp: CategoriesEvents()
}