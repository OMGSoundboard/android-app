package audio.omgsoundboard.core.data.local.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import audio.omgsoundboard.core.data.local.UriTypeConverter
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.utils.Constants.SOUNDS_TABLE
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION

@Entity(
    tableName = SOUNDS_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class SoundsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @TypeConverters(UriTypeConverter::class) val uri: Uri,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    @ColumnInfo(name = "res_id") val resId: Int? = null,
    /** Persisted file extension for user-imported sounds. */
    @ColumnInfo(name = "file_extension") val fileExtension: String = DEFAULT_AUDIO_EXTENSION,
)

fun PlayableSound.toEntity() = SoundsEntity(
    id = id,
    title = title,
    uri = uri,
    date = date,
    isFavorite = isFav,
    categoryId = categoryId,
    resId = resId,
    fileExtension = fileExtension
)
