package audio.omgsoundboard.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import audio.omgsoundboard.core.domain.models.BackupMetadata
import com.google.gson.Gson
import java.io.File

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

/** Builds an Android resource [Uri] for a bundled sound. */
fun getUriPath(context: Context, resourceId: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.packageName)
        .appendPath("$resourceId")
        .build()
}

/** Copies a content [uri] into cache using [id] and [extension] for the file name. */
fun getFileFromUri(context: Context, uri: Uri, id: String, extension: String = DEFAULT_AUDIO_EXTENSION): File? {
    return try {
        val file = File(context.cacheDir, buildSoundFileName(id, extension))
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
                output.flush()
            }
        } ?: return null
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/** Serializes backup metadata to JSON. */
fun makeMetadataJson(
    metadata: BackupMetadata
): String {
    val gson = Gson()
    return gson.toJson(metadata)
}
