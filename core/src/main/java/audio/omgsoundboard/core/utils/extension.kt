package audio.omgsoundboard.core.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun getTitleFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(
        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val titleIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return if (titleIndex != -1) it.getString(titleIndex).split(".")[0] else null
        }
    }
    return null
}