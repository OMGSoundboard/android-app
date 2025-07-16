package audio.omgsoundboard.presentation.ui.widget.widget_config

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.domain.repository.StorageRepository
import audio.omgsoundboard.domain.models.BackgroundType
import audio.omgsoundboard.domain.repository.SharedPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    private val soundsDao: SoundsDao,
    private val sharedPrefRepository: SharedPrefRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _sounds = soundsDao.getAllSounds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(WidgetConfigurationState())
    val state = combine(_state, _sounds) { state, sounds ->
        state.copy(
            sounds = sounds.map { it.toDomain() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetConfigurationState())

    fun onEvents(event: WidgetConfigurationEvents){
        when(event){
            is WidgetConfigurationEvents.OnLoadInitialState -> loadInitialState(event.appWidgetId)
            is WidgetConfigurationEvents.OnSoundSelected -> onSoundSelected(event.sound)
            is WidgetConfigurationEvents.OnBackgroundTypeChange -> onBackgroundTypeChanged(event.type)
            is WidgetConfigurationEvents.OnColorSelected -> onColorSelected(event.color)
            is WidgetConfigurationEvents.OnCopyToInternalStorage -> copyToInternalStorage(event.uri, event.filename)
            is WidgetConfigurationEvents.OnFontColorSelected -> onFontColorSelected(event.color)
            is WidgetConfigurationEvents.OnFontSizeChange -> onFontSizeChange(event.size)
        }
    }

    private fun loadInitialState(appWidgetId: Int) {
        viewModelScope.launch {
            val widgetPrefs = sharedPrefRepository.getWidgetPreferences(appWidgetId)

            val initialSound = widgetPrefs.soundId.let { soundsDao.getSoundById(it)?.toDomain() }

            _state.update { currentState ->
                currentState.copy(
                    selectedSound = initialSound,
                    backgroundType = widgetPrefs.backgroundType,
                    selectedColor = widgetPrefs.backgroundColor,
                    pickedImageUri = widgetPrefs.backgroundImageUri,
                    fontColor = widgetPrefs.fontColor,
                    fontSize = widgetPrefs.fontSize,
                )
            }
        }
    }

    private fun onSoundSelected(sound: PlayableSound) {
        _state.update { it.copy(selectedSound = sound) }
    }

    private fun onBackgroundTypeChanged(type: BackgroundType) {
        _state.update { it.copy(backgroundType = type) }
    }

    private fun onColorSelected(color: Color) {
        _state.update { it.copy(selectedColor = color) }
    }

    private fun onFontColorSelected(color: Color) {
        _state.update { it.copy(fontColor = color) }
    }

    private fun onFontSizeChange(size: Float) {
        _state.update { it.copy(fontSize = size) }
    }

    private fun copyToInternalStorage(uri: Uri, filename: String){
        viewModelScope.launch {
            val path = storageRepository.copyToInternalStorage(uri, filename)
            _state.update {
                it.copy(pickedImageUri = path)
            }
        }
    }
}