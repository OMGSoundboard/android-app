package audio.omgsoundboard.core.domain.models

sealed class BackupResult {
    data class Success(val message: String? = null) : BackupResult()
    data class Error(val exception: Exception) : BackupResult()
}