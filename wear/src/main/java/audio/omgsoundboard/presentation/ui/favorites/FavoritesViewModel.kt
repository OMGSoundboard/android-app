package audio.omgsoundboard.presentation.ui.favorites

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val soundsDao: SoundsDao
) : ViewModel(){

    private val _sounds = soundsDao.getAllFavorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _state = MutableStateFlow(FavoritesState())
    val state = combine(
        _state,
        _sounds,
    ) { state, sounds ->
        state.copy(sounds = sounds.map { it.toDomain() },)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesState())


    fun onEvent(event: FavoritesEvents){
        when(event){
            is FavoritesEvents.OnPlaySound -> {
                playSound(event.index, event.resourceId, event.uri)
            }
            is FavoritesEvents.OnToggleFav -> {
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