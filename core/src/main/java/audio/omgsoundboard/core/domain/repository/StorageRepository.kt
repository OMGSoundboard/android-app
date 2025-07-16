package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.BackupResult

interface StorageRepository {

    suspend fun backupFiles(uri: Uri): BackupResult
    suspend fun restoreBackup(uri: Uri) : BackupResult
    suspend fun copyToInternalStorage(uri: Uri, filename: String): Uri?
    suspend fun syncWearFiles()
}