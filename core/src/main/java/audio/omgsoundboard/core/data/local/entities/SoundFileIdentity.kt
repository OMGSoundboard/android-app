package audio.omgsoundboard.core.data.local.entities

import androidx.room.ColumnInfo

/** Lightweight title and extension pair used for duplicate detection. */
data class SoundFileIdentity(
    val title: String,
    @ColumnInfo(name = "file_extension") val fileExtension: String,
)
