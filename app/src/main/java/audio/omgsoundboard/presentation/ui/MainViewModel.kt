package audio.omgsoundboard.presentation.ui

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.domain.models.PlayableSound
import audio.omgsoundboard.domain.repository.MediaManager
import audio.omgsoundboard.domain.repository.PlayerRepository
import audio.omgsoundboard.domain.repository.StorageRepository
import audio.omgsoundboard.domain.repository.UserPreferences
import audio.omgsoundboard.presentation.navigation.Screens
import audio.omgsoundboard.presentation.theme.ThemeType
import audio.omgsoundboard.presentation.theme.toThemeType
import audio.omgsoundboard.utils.Constants.PARTICLES_STATUS
import audio.omgsoundboard.utils.Constants.THEME_TYPE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val userPreferences: UserPreferences,
    private val storage: StorageRepository
) : ViewModel() {

    var currentScreen by mutableStateOf(Screens.CategorySoundsScreen, policy = neverEqualPolicy())
        private set

    var searchText by mutableStateOf("")
        private set

    var areParticlesEnabled by mutableStateOf(false)
        private set

    var selectedTheme by mutableStateOf(ThemeType.DARK)
        private set

    var customSounds = mutableStateListOf<PlayableSound>()
        private set

    private var customSoundsTemp = mutableStateListOf<PlayableSound>()

    var favorites = mutableStateListOf<PlayableSound>()
        private set

    var favoritesTemp = mutableStateListOf<PlayableSound>()
        private set

    var fabPress by mutableStateOf(false)
        private set

    var exportBackup by mutableStateOf(false)
        private set

    var restoreBackup by mutableStateOf(false)
        private set

    var isDropMenuExpanded by mutableStateOf(false)
        private set

    init {
        getFavorites()
        readUserPreferences()
    }

    fun setCurrentScreenValue(screens: Screens, category: String = "") {
        if (category.isNotBlank()) {
            screens.title = category
        }
        currentScreen = screens
    }

    fun setParticlesState() {
        areParticlesEnabled = !areParticlesEnabled
        userPreferences.putBooleanPair(PARTICLES_STATUS, areParticlesEnabled)
    }

    fun setThemeType(type: ThemeType) {
        selectedTheme = type
        userPreferences.putStringPair(THEME_TYPE, type.toString())
    }


    fun addCustomSound(fileName: String, uri: Uri) {
        val newCustomSound = player.addCustomSound(fileName, uri)
        if (newCustomSound != null) {
            viewModelScope.launch {
                val newBatch = storage.insertNewCustomSound(fileName, newCustomSound)
                customSounds.clear()
                customSoundsTemp.clear()
                customSounds.addAll(newBatch)
                customSoundsTemp.addAll(newBatch)
            }
        } else {
            //Something wrong
        }
    }

    fun exportBackup(uri: Uri) {
        storage.backupFiles(uri, customSounds)
    }

    fun restoreBackup(uri: Uri){
        viewModelScope.launch {
            storage.restoreBackup(uri)
            getCustomSounds()
        }
    }

    fun setSearchTextValue(newValue: String) {
        searchText = newValue

        if (currentScreen == Screens.CustomScreen){
            customSounds.clear()
            if (searchText == "") {
                customSounds.addAll(customSoundsTemp)
            } else {
                customSoundsTemp.forEach {
                    if (it.title.lowercase().contains(searchText.lowercase())) {
                        customSounds.add(it)
                    }
                }
            }
        } else if (currentScreen == Screens.FavoritesScreen){
            favorites.clear()
            if (searchText == "") {
                favorites.addAll(favoritesTemp)
            } else {
                favoritesTemp.forEach {
                    if (it.title.lowercase().contains(searchText.lowercase())) {
                        favorites.add(it)
                    }
                }
            }
        }
    }

    fun playSound(index: Int, resourceId: Int, uri: Uri?) {
        player.playFile(index, resourceId, uri)
    }

    fun shareSound(fileName: String, resourceId: Int) {
        player.shareFile(fileName, resourceId)
    }

    fun setMedia(type: MediaManager, fileName: String, resourceId: Int, uri: Uri?) {
        player.setMedia(type, fileName, resourceId, uri)
    }

    fun favorite(item: PlayableSound, isCustom: Boolean = false, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            var index = favorites.indexOfFirst { it.resId == item.resId }

            if (isCustom){
                index = favorites.indexOfFirst { it.uri == item.uri }
            }

            if (index != -1) {
                storage.deleteFavorite(favorites[index].id)
                favorites.removeAt(index)
                callback(false)
            } else {
                storage.insertNewFavorite(item)
                getFavorites()
                callback(true)
            }
        }
    }

    fun changeFav(index: Int, isFav: Boolean){
        customSounds[index] = customSounds[index].copy(isFav = isFav)
    }

    fun toggleDropMenu() {
        isDropMenuExpanded = !isDropMenuExpanded
    }

    fun fabPress(status: Boolean) {
        fabPress = status
    }

    fun backupPress(export: Boolean?, restore: Boolean?) {
        if (export != null) {
            exportBackup = export
        }
        if (restore != null) {
            restoreBackup = restore
        }
    }


    private fun getFavorites() {
        viewModelScope.launch {
            favorites.clear()
            favoritesTemp.clear()
            val favSounds = storage.getAllFavorites().toMutableStateList()
            favorites.addAll(favSounds)
            favoritesTemp.addAll(favSounds)
        }
    }

    fun getCustomSounds() {
        viewModelScope.launch {
            customSounds.clear()
            customSoundsTemp.clear()
            val sounds = storage.getAllCustomSounds().toMutableStateList()
            customSounds.addAll(sounds)
            customSoundsTemp.addAll(sounds)
        }
    }

    private fun readUserPreferences() {
        areParticlesEnabled = userPreferences.getBooleanPair(PARTICLES_STATUS)
        selectedTheme = toThemeType(userPreferences.getStringPair(THEME_TYPE))
    }
}