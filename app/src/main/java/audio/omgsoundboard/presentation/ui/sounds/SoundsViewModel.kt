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
import audio.omgsoundboard.core.utils.Constants.SOUND_SORT_ORDER
import audio.omgsoundboard.core.utils.Constants.STOP_ON_NEW_SOUND
import audio.omgsoundboard.core.utils.Constants.STOP_ON_RETAP
import audio.omgsoundboard.core.utils.Constants.THEME_TYPE
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION
import audio.omgsoundboard.core.utils.existingSoundFileKeys
import audio.omgsoundboard.core.domain.models.SoundSortOrder
import audio.omgsoundboard.core.domain.models.sortedBy
import audio.omgsoundboard.core.domain.models.toSoundSortOrder
import audio.omgsoundboard.domain.repository.SharedPrefRepository
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.presentation.theme.toThemeType
import audio.omgsoundboard.presentation.utils.UiEvent
import audio.omgsoundboard.presentation.utils.UiText
import audio.omgsoundboard.presentation.utils.combine
import audio.omgsoundboard.sync_manager.DataLayerRepository
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
    private val dataLayer: DataLayerRepository
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    private val _categoryId = MutableStateFlow(-1)
    private val _sortOrder = MutableStateFlow(SoundSortOrder.TITLE_ASC)
    private val _categories = categoriesDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _wearNodes = dataLayer.getConnectedWearNodesAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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
        _searchTerm,
        _wearNodes,
        _sortOrder,
    ) { state, categories, categoryId, sounds, search, wearNodes, sortOrder ->
        val allCategory = Category(id = -1, name = "All")
        val categoriesWithAll = listOf(allCategory) + categories.map { it.toDomain() }

        val currentCategory = categoriesWithAll.find { it.id == categoryId }
        state.copy(
            categories = categoriesWithAll,
            currentCategory = currentCategory,
            sounds = sounds.map { it.toDomain() }.sortedBy(sortOrder),
            searchTerm = search,
            wearNodes = wearNodes,
            soundSortOrder = sortOrder,
        )
    }.combine(player.playbackProgress) { state, progress ->
        state.copy(playbackProgress = progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SoundsState())

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getUserPreferences()
    }


    fun onEvent(event: SoundsEvents) {
        soundsEventHandlers.any { it(event) }
    }

    private val soundsEventHandlers: List<(SoundsEvents) -> Boolean> = listOf(
        ::dispatchBackupEvent,
        ::dispatchSearchEvent,
        ::dispatchSoundPlaybackEvent,
        ::dispatchSoundDialogEvent,
        ::dispatchSoundCrudEvent,
        ::dispatchMenuPreferenceEvent,
        ::dispatchPlaybackPreferenceEvent,
        ::dispatchSortEvent,
        ::dispatchNavigationEvent,
    )

    private fun dispatchBackupEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnRestoreBackup -> {
            restoreBackup(event.uri)
            true
        }
        is SoundsEvents.OnBackupFiles -> {
            backupFiles(event.uri)
            true
        }
        is SoundsEvents.OnSyncWear -> {
            syncWear(event.nodeId)
            true
        }
        else -> false
    }

    private fun dispatchSearchEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnSetCategoryId -> {
            _categoryId.value = event.id
            true
        }
        is SoundsEvents.OnToggleSearch -> {
            _state.value = _state.value.copy(
                searchTerm = "",
                showSearchField = !_state.value.showSearchField
            )
            _searchTerm.value = ""
            true
        }
        is SoundsEvents.OnSearchTerm -> {
            _searchTerm.value = event.term
            true
        }
        else -> false
    }

    private fun dispatchSoundPlaybackEvent(event: SoundsEvents): Boolean =
        dispatchCorePlaybackEvent(event) || dispatchMediaAssignmentEvent(event)

    private fun dispatchCorePlaybackEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnPlaySound -> {
            playSound(event.index, event.resourceId, event.uri)
            true
        }
        is SoundsEvents.OnToggleFav -> {
            toggleFav(event.id)
            true
        }
        is SoundsEvents.OnShareSound -> {
            shareSound(event.sound)
            true
        }
        else -> false
    }

    private fun dispatchMediaAssignmentEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnSetAsRingtone -> {
            setMedia(MediaManager.Ringtone, event.sound)
            true
        }
        is SoundsEvents.OnSetAsAlarm -> {
            setMedia(MediaManager.Alarm, event.sound)
            true
        }
        is SoundsEvents.OnSetAsNotification -> {
            setMedia(MediaManager.Notification, event.sound)
            true
        }
        else -> false
    }

    private fun dispatchSoundDialogEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnShowHideChangeCategoryDialog -> {
            _state.value = _state.value.copy(
                showChangeCategoryDialog = !_state.value.showChangeCategoryDialog,
            )
            true
        }
        is SoundsEvents.OnShowHideAddRenameSoundDialog -> {
            _state.value = _state.value.copy(
                showAddRenameSoundDialog = !_state.value.showAddRenameSoundDialog,
                textFieldValue = event.initialText,
                isRenaming = event.isRenaming,
                addedSoundUri = event.uri,
                addedSoundExtension = event.extension,
            )
            true
        }
        is SoundsEvents.OnTextFieldChange -> {
            _state.value = _state.value.copy(textFieldValue = event.text, textFieldError = false)
            true
        }
        is SoundsEvents.OnShowHideDeleteSoundDialog -> {
            _state.value = _state.value.copy(
                showConfirmDeleteDialog = !_state.value.showConfirmDeleteDialog
            )
            true
        }
        else -> false
    }

    private fun dispatchSoundCrudEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnConfirmSoundCategoryChange -> {
            changeCategory(event.soundId, event.categoryId)
            true
        }
        is SoundsEvents.OnConfirmRename -> {
            renameSound(event.sound)
            true
        }
        is SoundsEvents.OnConfirmAdd -> {
            addSound()
            true
        }
        is SoundsEvents.OnAddMultipleSounds -> {
            addMultipleSounds(event.uris)
            true
        }
        is SoundsEvents.OnConfirmDelete -> {
            deleteSound(event.soundId)
            true
        }
        else -> false
    }

    private fun dispatchMenuPreferenceEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnToggleDropMenu -> {
            _state.value = _state.value.copy(showDropMenu = !_state.value.showDropMenu)
            true
        }
        is SoundsEvents.OnToggleParticles -> {
            toggleParticles()
            true
        }
        is SoundsEvents.OnShowHideThemePicker -> {
            _state.value = _state.value.copy(showThemePicker = !_state.value.showThemePicker)
            true
        }
        is SoundsEvents.OnChangeTheme -> {
            changeTheme(event.theme)
            true
        }
        else -> false
    }

    private fun dispatchPlaybackPreferenceEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnShowHidePlaybackBehaviorDialog -> {
            _state.value = _state.value.copy(
                showPlaybackBehaviorDialog = !_state.value.showPlaybackBehaviorDialog
            )
            true
        }
        is SoundsEvents.OnToggleStopOnRetap -> {
            toggleStopOnRetap()
            true
        }
        is SoundsEvents.OnToggleStopOnNewSound -> {
            toggleStopOnNewSound()
            true
        }
        else -> false
    }

    private fun dispatchSortEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnShowHideSortPicker -> {
            _state.value = _state.value.copy(showSortPicker = !_state.value.showSortPicker)
            true
        }
        is SoundsEvents.OnChangeSortOrder -> {
            changeSortOrder(event.sortOrder)
            true
        }
        else -> false
    }

    private fun dispatchNavigationEvent(event: SoundsEvents): Boolean = when (event) {
        is SoundsEvents.OnNavigate -> {
            sendUiEvent(UiEvent.Navigate(event.route))
            true
        }
        else -> false
    }

    private fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            when (val result = storage.restoreBackup(uri)) {
                is BackupResult.Error -> showRestoreError(result)
                is BackupResult.Success -> showRestoreSuccess(result)
            }
        }
    }

    private fun showRestoreError(result: BackupResult.Error) {
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

    private fun showRestoreSuccess(result: BackupResult.Success) {
        val message = result.message
        if (message != null) {
            sendUiEvent(
                UiEvent.ShowInfoDialog(
                    UiText.StringResource(R.string.backup_restored_2, arrayOf(message))
                )
            )
            return
        }
        sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.backup_restored)))
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

    private fun syncWear(nodeId: String){
        viewModelScope.launch {
            dataLayer.syncDataToWearable(nodeId)
            sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.data_synced)))
        }
    }

    private fun addSound() {
        viewModelScope.launch {
            val title = _state.value.textFieldValue.trim()
            val uri = _state.value.addedSoundUri!!
            val extension = _state.value.addedSoundExtension

            if (soundsDao.countByTitleAndExtension(title, extension) > 0) {
                _state.value = _state.value.copy(textFieldError = true)
                sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.sound_already_exists)))
                return@launch
            }

            val sound = PlayableSound(
                title = title,
                uri = uri,
                date = System.currentTimeMillis(),
                isFav = false,
                categoryId = _categoryId.value,
                resId = null,
                fileExtension = extension,
            )

            val newSoundUri = player.addSound(title, uri, extension)

            if (newSoundUri != null) {
                soundsDao.insertSound(sound.copy(uri = newSoundUri).toEntity())
            } else {
                sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.can_not_add_sound)))
            }

            _state.value = _state.value.copy(
                showAddRenameSoundDialog = false,
                textFieldValue = "",
                addedSoundUri = Uri.EMPTY,
                addedSoundExtension = DEFAULT_AUDIO_EXTENSION,
            )
        }
    }

    private fun addMultipleSounds(uris: List<Uri>) {
        viewModelScope.launch {
            val existingSoundKeys = existingSoundFileKeys(
                soundsDao.getAllSoundIdentities().map { it.title to it.fileExtension }
            )
            val result = player.addMultipleSounds(uris, existingSoundKeys)
            val sounds = result.map {
                PlayableSound(
                    title = it.title,
                    uri = it.uri,
                    date = System.currentTimeMillis(),
                    isFav = false,
                    categoryId = _categoryId.value,
                    resId = null,
                    fileExtension = it.extension,
                )
            }

            soundsDao.insertSounds(sounds.map { it.toEntity() })

            if (uris.size == sounds.size){
                sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.sounds_added)))
            } else {
                val skippedSounds = uris.size - sounds.size
                sendUiEvent(UiEvent.ShowInfoDialog(UiText.StringResource(R.string.sounds_added_2, arrayOf(skippedSounds.toString()))))
            }
        }
    }

    private fun playSound(index: Int, resourceId: Int?, uri: Uri) {
        player.playFile(index, resourceId, uri)
        trackPlayCount(index)
    }

    private fun trackPlayCount(index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            soundsDao.incrementPlayCount(index)
        }
    }

    private fun shareSound(sound: PlayableSound) {
        player.shareFile(sound.title, sound.resId, sound.uri)
    }

    private fun setMedia(type: MediaManager, sound: PlayableSound) {
        player.setMedia(type, sound.title, sound.resId, sound.uri, sound.fileExtension)
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


    private fun changeSortOrder(sortOrder: SoundSortOrder) {
        viewModelScope.launch {
            shared.putStringPair(SOUND_SORT_ORDER, sortOrder.name)
            _sortOrder.value = sortOrder
        }
    }

    private fun toggleStopOnRetap() {
        viewModelScope.launch {
            val new = !_state.value.stopOnRetap
            shared.putBooleanPair(STOP_ON_RETAP, new)
            _state.value = _state.value.copy(stopOnRetap = new)
        }
    }

    private fun toggleStopOnNewSound() {
        viewModelScope.launch {
            val new = !_state.value.stopOnNewSound
            shared.putBooleanPair(STOP_ON_NEW_SOUND, new)
            _state.value = _state.value.copy(stopOnNewSound = new)
        }
    }

    private fun getUserPreferences() {
        viewModelScope.launch {
            val themePref = shared.getStringPair(THEME_TYPE, ThemeType.DARK.name)
            val particlesPref = shared.getBooleanPair(PARTICLES_STATUS, false)
            val stopOnRetap = shared.getBooleanPair(STOP_ON_RETAP, false)
            val stopOnNewSound = shared.getBooleanPair(STOP_ON_NEW_SOUND, false)
            val sortOrderPref = shared.getStringPair(SOUND_SORT_ORDER, SoundSortOrder.TITLE_ASC.name)

            _sortOrder.value = toSoundSortOrder(sortOrderPref)
            _state.value = _state.value.copy(
                pickedTheme = toThemeType(themePref),
                areParticlesEnable = particlesPref,
                stopOnRetap = stopOnRetap,
                stopOnNewSound = stopOnNewSound
            )
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}