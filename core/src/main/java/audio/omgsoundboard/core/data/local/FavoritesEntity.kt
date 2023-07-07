package audio.omgsoundboard.core.data.local

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.utils.Constants.FAVORITES_TABLE

@Entity(tableName = FAVORITES_TABLE)
class FavoritesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "resId") val resId: Int,
    @TypeConverters(UriTypeConverter::class) val uri: Uri,
)

fun PlayableSound.toEntity() = FavoritesEntity(
    title = title,
    resId = resId,
    uri = uri
)
