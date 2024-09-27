package audio.omgsoundboard.presentation.ui.sounds

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.toEntity
import audio.omgsoundboard.core.domain.models.BackupResult
import audio.omgsoundboard.core.domain.models.Category
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.MediaManager
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import audio.omgsoundboard.core.domain.repository.StorageRepository
import audio.omgsoundboard.core.utils.Constants.PARTICLES_STATUS
import audio.omgsoundboard.core.utils.Constants.THEME_TYPE
import audio.omgsoundboard.domain.repository.SharedPrefRepository
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.presentation.theme.toThemeType
import audio.omgsoundboard.presentation.utils.UiEvent
import audio.omgsoundboard.presentation.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SoundsViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val categoriesDao: CategoryDao,
    private val soundsDao: SoundsDao,
    private val shared: SharedPrefRepository,
    private val storage: StorageRepository,
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    private val _categoryId = MutableStateFlow(-1)
    private val _categories = categoriesDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sounds = combine(_searchTerm, _categoryId) { searchTerm, categoryId ->
        Pair(searchTerm, categoryId)
    }.flatMapLatest { (searchTerm, categoryId) ->
        when {
            searchTerm.isEmpty() && categoryId == -1 -> soundsDao.getAllSounds()
            searchTerm.isEmpty() -> soundsDao.getSoundsByCategoryId(categoryId)
            categoryId == -1 -> soundsDao.searchAllSounds(searchTerm)
            else -> soundsDao.searchSoundByCategory(categoryId, searchTerm)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    private val _state = MutableStateFlow(SoundsState())
    val state = combine(
        _state,
        _categories,
        _categoryId,
        _sounds,
        _searchTerm
    ) { state, categories, categoryId, sounds, search ->
        val allCategory = Category(id = -1, name = "All")
        val categoriesWithAll = listOf(allCategory) + categories.map { it.toDomain() }

        val currentCategory = categoriesWithAll.find { it.id == categoryId }
        state.copy(
            categories = categoriesWithAll,
            currentCategory = currentCategory,
            sounds = sounds.map { it.toDomain() },
            searchTerm = search
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundsState())

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getUserPreferences()
    }


    fun onEvent(event: SoundsEvents) {
        when (event) {
            is SoundsEvents.OnRestoreBackup -> {
                restoreBackup(event.uri)
            }

            is SoundsEvents.OnBackupFiles -> {
                backupFiles(event.uri)
            }

            is SoundsEvents.OnSetCategoryId -> {
                _categoryId.value = event.id
            }

            is SoundsEvents.OnToggleSearch -> {
                _state.value = _state.value.copy(
                    searchTerm = "",
                    showSearchField = !_state.value.showSearchField
                )
                _searchTerm.value = ""
            }

            is SoundsEvents.OnSearchTerm -> {
                _searchTerm.value = event.term
            }

            is SoundsEvents.OnPlaySound -> {
                playSound(event.index, event.resourceId, event.uri)
            }

            is SoundsEvents.OnToggleFav -> {
                toggleFav(event.id)
            }

            is SoundsEvents.OnShareSound -> {
                shareSound(event.sound)
            }

            is SoundsEvents.OnSetAsRingtone -> {
                setMedia(MediaManager.Ringtone, event.sound)
            }

            is SoundsEvents.OnSetAsAlarm -> {
                setMedia(MediaManager.Alarm, event.sound)
            }

            is SoundsEvents.OnSetAsNotification -> {
                setMedia(MediaManager.Notification, event.sound)
            }

            is SoundsEvents.OnShowHideChangeCategoryDialog -> {
                _state.value = _state.value.copy(showChangeCategoryDialog = !_state.value.showChangeCategoryDialog,)
            }

            is SoundsEvents.OnConfirmSoundCategoryChange -> {
                changeCategory(event.soundId, event.categoryId)
            }

            is SoundsEvents.OnShowHideAddRenameSoundDialog -> {
                _state.value = _state.value.copy(
                    showAddRenameSoundDialog = !_state.value.showAddRenameSoundDialog,
                    textFieldValue = event.initialText,
                    isRenaming = event.isRenaming,
                    addedSoundUri = event.uri
                )
            }

            is SoundsEvents.OnTextFieldChange -> {
                _state.value =
                    _state.value.copy(textFieldValue = event.text, textFieldError = false)
            }

            is SoundsEvents.OnConfirmRename -> {
                renameSound(event.sound)
            }

            is SoundsEvents.OnConfirmAdd -> {
                addSound()
            }

            is SoundsEvents.OnShowHideDeleteSoundDialog -> {
                _state.value =
                    _state.value.copy(showConfirmDeleteDialog = !_state.value.showConfirmDeleteDialog)
            }

            is SoundsEvents.OnConfirmDelete -> {
                deleteSound(event.soundId)
            }

            is SoundsEvents.OnToggleDropMenu -> {
                _state.value = _state.value.copy(showDropMenu = !_state.value.showDropMenu)
            }

            is SoundsEvents.OnToggleParticles -> {
                toggleParticles()
            }

            is SoundsEvents.OnShowHideThemePicker -> {
                _state.value = _state.value.copy(showThemePicker = !_state.value.showThemePicker)
            }

            is SoundsEvents.OnChangeTheme -> {
                changeTheme(event.theme)
            }

            is SoundsEvents.OnNavigate -> {
                sendUiEvent(UiEvent.Navigate(event.route))
            }
        }
    }

    private fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            when (val result = storage.restoreBackup(uri)) {
                is BackupResult.Error -> {
                    val errorMessage = result.exception.message ?: "Unknown error"
                    sendUiEvent(
                        UiEvent.ShowInfoDialog(
                            UiText.StringResource(
                                R.string.restore_backup_error,
                                arrayOf(errorMessage)
                            )
                        )
                    )
                }

                is BackupResult.Success -> {
                    val message = result.message
                    if (message != null) {
                        sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.backup_restored_2, arrayOf(message))))
                        return@launch
                    }
                    sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.backup_restored)))
                }
            }
        }
    }

    private fun backupFiles(uri: Uri) {
        viewModelScope.launch {
            when (val result = storage.backupFiles(uri)) {
                is BackupResult.Error -> {
                    val errorMessage = result.exception.message ?: "Unknown error"
                    sendUiEvent(
                        UiEvent.ShowInfoDialog(
                            UiText.StringResource(
                                R.string.backup_error,
                                arrayOf(errorMessage)
                            )
                        )
                    )
                }
                is BackupResult.Success -> {
                    sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.files_backed)))
                }
            }
        }
    }

    private fun changeCategory(soundId: Int, categoryId: Int) {
        viewModelScope.launch {
            soundsDao.changeCategory(soundId, categoryId)
        }
    }

    private fun addSound() {
        viewModelScope.launch {
            val title = _state.value.textFieldValue.trim()
            val uri = _state.value.addedSoundUri!!

            val sound = PlayableSound(
                title = title,
                uri = uri,
                date = System.currentTimeMillis(),
                isFav = false,
                categoryId = _categoryId.value,
                resId = null
            )

            val newSoundUri = player.addSound(title, uri)

            if (newSoundUri != null) {
                soundsDao.insertSound(sound.copy(uri = newSoundUri).toEntity())
            } else {
                sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.can_not_add_sound)))
            }

            _state.value = _state.value.copy(
                showAddRenameSoundDialog = false,
                textFieldValue = "",
                addedSoundUri = Uri.EMPTY
            )
        }
    }

    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {
        player.playFile(index, resourceId, uri)
    }

    private fun shareSound(sound: PlayableSound) {
        player.shareFile(sound.title, sound.resId, sound.uri)
    }

    private fun setMedia(type: MediaManager, sound: PlayableSound) {
        player.setMedia(type, sound.title, sound.resId, sound.uri)
    }

    private fun toggleFav(id: Int) {
        viewModelScope.launch {
            soundsDao.toggleFav(id)
        }
    }

    private fun renameSound(sound: PlayableSound) {
        viewModelScope.launch {
            val newSound = sound.copy(title = _state.value.textFieldValue.trim())
            soundsDao.updateSound(newSound.toEntity())
            _state.value = _state.value.copy(
                showAddRenameSoundDialog = false,
                textFieldValue = "",
            )
        }
    }

    private fun deleteSound(soundId: Int) {
        viewModelScope.launch {
            soundsDao.deleteSound(soundId)
        }
    }

    private fun toggleParticles() {
        viewModelScope.launch {
            val currentStatus = _state.value.areParticlesEnable
            shared.putBooleanPair(PARTICLES_STATUS, !currentStatus)
            _state.value = _state.value.copy(areParticlesEnable = !currentStatus)
        }
    }

    private fun changeTheme(themeType: ThemeType) {
        viewModelScope.launch {
            shared.putStringPair(THEME_TYPE, themeType.name)
            _state.value = _state.value.copy(pickedTheme = themeType)
        }
    }

    private fun getUserPreferences() {
        viewModelScope.launch {
            val themePref = shared.getStringPair(THEME_TYPE, ThemeType.DARK.name)
            val particlesPref = shared.getBooleanPair(PARTICLES_STATUS, false)

            _state.value = _state.value.copy(
                pickedTheme = toThemeType(themePref),
                areParticlesEnable = particlesPref
            )
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}