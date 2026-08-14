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

/** Reads the display name for a content [uri], when available. */
fun getDisplayNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(
        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val titleIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return if (titleIndex != -1) it.getString(titleIndex) else null
        }
    }
    return null
}

/** Returns the normalized title parsed from a content [uri], when available. */
fun getTitleFromUri(context: Context, uri: Uri): String? {
    val displayName = getDisplayNameFromUri(context, uri) ?: return null
    return parseAudioFileName(displayName)?.first
        ?: displayName.substringBeforeLast('.').trim().takeIf { it.isNotEmpty() }
}

/** Returns the supported file extension parsed from a content [uri], when available. */
fun getExtensionFromUri(context: Context, uri: Uri): String? {
    val displayName = getDisplayNameFromUri(context, uri) ?: return null
    return parseAudioFileName(displayName)?.second
}

fun getUriPath(context: Context, resourceId: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.packageName)
        .appendPath("$resourceId")
        .build()
}

fun getFileFromUri(context: Context, uri: Uri, id: String, extension: String = DEFAULT_AUDIO_EXTENSION): File? {
    return try {
        val file = File(context.cacheDir, buildSoundFileName(id, extension))
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
