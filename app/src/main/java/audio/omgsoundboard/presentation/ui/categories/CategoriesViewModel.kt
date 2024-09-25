package audio.omgsoundboard.presentation.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.entities.toEntity
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.presentation.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val dao: CategoryDao
) : ViewModel(){

    private val _categories = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(CategoriesState())
    val state = combine(_state, _categories) { state, categories ->
        state.copy(
            categories = categories.map { it.toDomain() },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesState())


    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: CategoriesEvents) {
        when (event) {
            is CategoriesEvents.OnToggleDropMenu -> {
                _state.value = state.value.copy(showDropMenu = !state.value.showDropMenu)
            }

            is CategoriesEvents.OnShowHideAddRenameCategoryDialog -> {
                _state.value = state.value.copy(
                    showAddRenameCategoryDialog = !state.value.showAddRenameCategoryDialog,
                    textFieldValue = event.initialText,
                    isRenaming = event.isRenaming
                )
            }

            is CategoriesEvents.OnTextFieldChange -> {
                _state.value = state.value.copy(textFieldValue = event.text)
            }

            is CategoriesEvents.OnConfirmRename -> {
                rename(event.category)
            }

            is CategoriesEvents.OnConfirmAdd -> {
                add()
            }

            is CategoriesEvents.OnShowHideDeleteCategoryDialog -> {
                _state.value = state.value.copy(showConfirmDeleteDialog = !state.value.showConfirmDeleteDialog)
            }

            is CategoriesEvents.OnConfirmDelete -> {
                delete(event.id)
            }

            is CategoriesEvents.OnNavigateUp -> sendUiEvent(UiEvent.NavigateUp)
        }
    }

    private fun add(){
        viewModelScope.launch {
            val category = Category(name = state.value.textFieldValue.trim())
            dao.insertCategory(category.toEntity())

            _state.value = _state.value.copy(
                showAddRenameCategoryDialog = false,
                textFieldValue = "",
            )
        }
    }

    private fun rename(category: Category) {
        viewModelScope.launch {
            dao.updateCategory(category.copy(name = state.value.textFieldValue.trim()).toEntity())
            _state.value = _state.value.copy(
                showAddRenameCategoryDialog = false,
                textFieldValue = "",
            )
        }
    }

    private fun delete(id: Int) {
        viewModelScope.launch {
            dao.deleteCategory(id)
        }
    }


    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}