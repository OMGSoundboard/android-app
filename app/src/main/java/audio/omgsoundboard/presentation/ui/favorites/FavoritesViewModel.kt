package audio.omgsoundboard.presentation.ui.favorites

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
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
    val state = combine(_state, _sounds, player.playbackProgress, ::buildFavoritesState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritesState())


    fun onEvent(event: FavoritesEvents) {
        favoritesEventHandlers.any { it(event) }
    }

    private val favoritesEventHandlers: List<(FavoritesEvents) -> Boolean> = listOf(
        ::dispatchPlaybackEvent,
        ::dispatchDialogEvent,
        ::dispatchCrudEvent,
        ::dispatchNavigationEvent,
    )

    private fun dispatchPlaybackEvent(event: FavoritesEvents): Boolean =
        dispatchCorePlaybackEvent(event) || dispatchMediaAssignmentEvent(event)

    private fun dispatchCorePlaybackEvent(event: FavoritesEvents): Boolean = when (event) {
        is FavoritesEvents.OnPlaySound -> {
            playSound(event.index, event.resourceId, event.uri)
            true
        }
        is FavoritesEvents.OnToggleFav -> {
            toggleFav(event.id)
            true
        }
        is FavoritesEvents.OnShareSound -> {
            shareSound(event.sound)
            true
        }
        else -> false
    }

    private fun dispatchMediaAssignmentEvent(event: FavoritesEvents): Boolean = when (event) {
        is FavoritesEvents.OnSetAsRingtone -> {
            setMedia(MediaManager.Ringtone, event.sound)
            true
        }
        is FavoritesEvents.OnSetAsAlarm -> {
            setMedia(MediaManager.Alarm, event.sound)
            true
        }
        is FavoritesEvents.OnSetAsNotification -> {
            setMedia(MediaManager.Notification, event.sound)
            true
        }
        else -> false
    }

    private fun dispatchDialogEvent(event: FavoritesEvents): Boolean = when (event) {
        is FavoritesEvents.OnShowHideRenameSoundDialog -> {
            _state.value = _state.value.copy(
                showRenameSoundDialog = !_state.value.showRenameSoundDialog,
                textFieldValue = event.initialText,
            )
            true
        }
        is FavoritesEvents.OnTextFieldChange -> {
            _state.value = _state.value.copy(textFieldValue = event.text, textFieldError = false)
            true
        }
        is FavoritesEvents.OnShowHideDeleteSoundDialog -> {
            _state.value = _state.value.copy(showConfirmDeleteDialog = !_state.value.showConfirmDeleteDialog)
            true
        }
        is FavoritesEvents.OnToggleDropMenu -> {
            _state.value = _state.value.copy(showDropMenu = !_state.value.showDropMenu)
            true
        }
        else -> false
    }

    private fun dispatchCrudEvent(event: FavoritesEvents): Boolean = when (event) {
        is FavoritesEvents.OnConfirmRename -> {
            renameSound(event.sound)
            true
        }
        is FavoritesEvents.OnConfirmDelete -> {
            deleteSound(event.soundId)
            true
        }
        else -> false
    }

    private fun dispatchNavigationEvent(event: FavoritesEvents): Boolean = when (event) {
        is FavoritesEvents.OnNavigateUp -> {
            sendUiEvent(UiEvent.NavigateUp)
            true
        }
        else -> false
    }

    private fun buildFavoritesState(
        state: FavoritesState,
        sounds: List<SoundsEntity>,
        progress: Map<Int, Float>,
    ): FavoritesState = state.copy(
        sounds = sounds.map { it.toDomain() },
        playbackProgress = progress,
    )

    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {
        player.playFile(index, resourceId, uri)
    }

    private fun shareSound(sound: PlayableSound){
        player.shareFile(sound.title, sound.resId, sound.uri)
    }

    private fun setMedia(type: MediaManager, sound: PlayableSound){
        player.setMedia(type, sound.title, sound.resId, sound.uri, sound.fileExtension)
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