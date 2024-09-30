package audio.omgsoundboard.presentation.ui.favorites

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.toEntity
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.MediaManager
import audio.omgsoundboard.core.domain.repository.PlayerRepository
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
class FavoritesViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val soundsDao: SoundsDao,
) : ViewModel() {

    private val _sounds = soundsDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _state = MutableStateFlow(FavoritesState())
    val state = combine(_state, _sounds) { state, sounds ->
        state.copy(
            sounds = sounds.map { it.toDomain() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesState())


    fun onEvent(event: FavoritesEvents) {
        when (event) {
            is FavoritesEvents.OnPlaySound -> {
                playSound(event.index, event.resourceId, event.uri)
            }
            is FavoritesEvents.OnToggleFav -> {
                toggleFav(event.id)
            }
            is FavoritesEvents.OnShareSound -> {
                shareSound(event.sound)
            }
            is FavoritesEvents.OnSetAsRingtone -> {
                setMedia(MediaManager.Ringtone, event.sound)
            }
            is FavoritesEvents.OnSetAsAlarm -> {
                setMedia(MediaManager.Alarm, event.sound)
            }
            is FavoritesEvents.OnSetAsNotification -> {
                setMedia(MediaManager.Notification, event.sound)
            }
            is FavoritesEvents.OnShowHideRenameSoundDialog -> {
                _state.value = _state.value.copy(
                    showRenameSoundDialog = !_state.value.showRenameSoundDialog,
                    textFieldValue = event.initialText,
                )
            }
            is FavoritesEvents.OnTextFieldChange -> {
                _state.value = _state.value.copy(textFieldValue = event.text, textFieldError = false)
            }
            is FavoritesEvents.OnConfirmRename -> {
                renameSound(event.sound)
            }
            is FavoritesEvents.OnShowHideDeleteSoundDialog -> {
                _state.value = _state.value.copy(showConfirmDeleteDialog = !_state.value.showConfirmDeleteDialog)
            }
            is FavoritesEvents.OnConfirmDelete -> {
                deleteSound(event.soundId)
            }
            is FavoritesEvents.OnToggleDropMenu -> {
                _state.value = _state.value.copy(showDropMenu = !_state.value.showDropMenu)
            }
            is FavoritesEvents.OnNavigateUp -> {
                sendUiEvent(UiEvent.NavigateUp)
            }
        }
    }

    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {
        player.playFile(index, resourceId, uri)
    }

    private fun shareSound(sound: PlayableSound){
        player.shareFile(sound.title, sound.resId, sound.uri)
    }

    private fun setMedia(type: MediaManager, sound: PlayableSound){
        player.setMedia(type, sound.title, sound.resId, sound.uri)
    }

    private fun toggleFav(id: Int){
        viewModelScope.launch {
            soundsDao.toggleFav(id)
        }
    }

    private fun renameSound(sound: PlayableSound){
        viewModelScope.launch {
            val newSound = sound.copy(title = _state.value.textFieldValue)
            soundsDao.updateSound(newSound.toEntity())
        }
    }

    private fun deleteSound(soundId: Int){
        viewModelScope.launch {
            soundsDao.deleteSound(soundId)
        }
    }

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}