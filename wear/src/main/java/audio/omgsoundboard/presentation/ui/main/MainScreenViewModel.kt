package audio.omgsoundboard.presentation.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val storage: StorageRepository
) : ViewModel() {

    var uiState by mutableStateOf(MainState())
        private set

    init {
        getCategories()
    }

    fun sync(){
        viewModelScope.launch {
            storage.syncWearFiles()
        }
    }

    private fun getCategories(){
        viewModelScope.launch {
            val categories = categoryDao.getAllCategoriesOnce().map { it.toDomain() }
            uiState = uiState.copy(categories = categories)
        }
    }
}