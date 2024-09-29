package audio.omgsoundboard.core.domain.models

import audio.omgsoundboard.core.data.local.entities.CategoryEntity

data class BackupMetadata(
    val sounds: List<SoundBackup>,
    val categories: List<CategoryEntity>,
)