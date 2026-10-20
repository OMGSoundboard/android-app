package audio.omgsoundboard.core.data.local.entities

import androidx.room.ColumnInfo

/** Sound title and extension pair used for duplicate detection queries. */
data class SoundFileIdentity(
    /** Stored sound title. */
    val title: String,
    /** Stored file extension without a leading dot. */
    @ColumnInfo(name = "file_extension") val fileExtension: String,
)
