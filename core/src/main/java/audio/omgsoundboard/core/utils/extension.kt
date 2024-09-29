package audio.omgsoundboard.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import audio.omgsoundboard.core.domain.models.BackupMetadata
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

fun getUriPath(context: Context, resourceId: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.packageName)
        .appendPath("$resourceId")
        .build()
}

fun getFileFromUri(context: Context, uri: Uri, id: String): File? {
    return try {
        val file = File(context.cacheDir, "$id.mp3")
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun makeMetadataJson(
    metadata: BackupMetadata
): String {
    val gson = Gson()
    return gson.toJson(metadata)
}