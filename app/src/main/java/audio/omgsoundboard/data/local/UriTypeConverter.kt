package audio.omgsoundboard.data.local

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(string: String?): Uri? {
        return string?.let { Uri.parse(it) }
    }
}