package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.PlayableSound

interface StorageRepository {

    fun backupFiles(uri: Uri, metadata: List<PlayableSound>)
    suspend fun restoreBackup(uri: Uri)
}