package audio.omgsoundboard.presentation.ui.sounds

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SoundsScreenViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val soundsDao: SoundsDao
) : ViewModel() {

    private val _categoryId = MutableStateFlow(-1)
    private val _sounds = _categoryId.flatMapLatest { categoryId ->
        when (categoryId) {
            -1 -> soundsDao.getAllSounds()
            else -> soundsDao.getSoundsByCategoryId(categoryId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(SoundsState())
    val state = combine(
        _state,
        _categoryId,
        _sounds,
    ) { state, categoryId, sounds ->

        state.copy(
            sounds = sounds.map { it.toDomain() },
            currentCategoryId = categoryId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundsState())

    fun onEvent(event: SoundsEvents){
        when(event){
            is SoundsEvents.OnSetCategoryId -> {
                _categoryId.value = event.id
            }
            is SoundsEvents.OnPlaySound -> {
                playSound(event.index, event.resourceId, event.uri)
            }
            is SoundsEvents.OnToggleFav -> {
                toggleFav(event.id)
            }
        }
    }

    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {
        player.playFile(index, resourceId, uri)
    }

    private fun toggleFav(id: Int) {
        viewModelScope.launch {
            soundsDao.toggleFav(id)
        }
    }
}