package audio.omgsoundboard.presentation.ui

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import audio.omgsoundboard.core.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearViewModel @Inject constructor(
    private val player: PlayerRepository,
    private val storage: StorageRepository
) : ViewModel () {

    var favorites = mutableStateListOf<PlayableSound>()
        private set

    init {
        getFavorites()
    }

    fun playSound(index: Int, resourceId: Int, uri: Uri?) {
        player.playFile(index, resourceId, uri)
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

    private fun getFavorites() {
        viewModelScope.launch {
            favorites.clear()
            val favSounds = storage.getAllFavorites().toMutableStateList()
            favorites.addAll(favSounds)
        }
    }


}