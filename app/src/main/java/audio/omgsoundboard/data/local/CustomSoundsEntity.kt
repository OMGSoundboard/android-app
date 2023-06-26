package audio.omgsoundboard.data.local

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import audio.omgsoundboard.utils.Constants

@Entity(tableName = Constants.CUSTOM_SOUNDS_TABLE)
class CustomSoundsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @TypeConverters(UriTypeConverter::class) val uri: Uri,
    @ColumnInfo(name = "date") val date: Long,
)

