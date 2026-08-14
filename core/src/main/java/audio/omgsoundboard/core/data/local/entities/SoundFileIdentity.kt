package audio.omgsoundboard.core.data.local.entities

import androidx.room.ColumnInfo

data class SoundFileIdentity(
    val title: String,
    @ColumnInfo(name = "file_extension") val fileExtension: String,
)
