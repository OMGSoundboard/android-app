package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.PlayableSound

interface StorageRepository {

    suspend fun getAllFavorites(): List<PlayableSound>
    suspend fun insertNewFavorite(sound: PlayableSound)
    suspend fun deleteFavorite(favoriteId: Int)

    suspend fun getAllCustomSounds(): List<PlayableSound>
    suspend fun insertNewCustomSound(title: String, uri: Uri): List<PlayableSound>
    suspend fun updateCustomSound(customSound: PlayableSound)
    suspend fun deleteCustomSound(customSoundId: Int)

    fun backupFiles(uri: Uri, metadata: List<PlayableSound>)
    suspend fun restoreBackup(uri: Uri)
}