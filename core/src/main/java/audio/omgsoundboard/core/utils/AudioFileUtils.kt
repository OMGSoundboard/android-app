package audio.omgsoundboard.core.utils

/** Default file extension when none is known. */
const val DEFAULT_AUDIO_EXTENSION = "mp3"

/** MIME type used by the audio file picker. */
const val AUDIO_PICKER_MIME_TYPE = "audio/*"

/** File extensions supported for import, storage, and playback. */
val SUPPORTED_AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a")

/** Normalizes an extension by trimming whitespace and removing a leading dot. */
fun normalizeAudioExtension(extension: String): String =
    extension.trim().lowercase().removePrefix(".")

/** Returns whether [extension] is a supported audio extension. */
fun isSupportedAudioExtension(extension: String): Boolean =
    normalizeAudioExtension(extension) in SUPPORTED_AUDIO_EXTENSIONS

/** Builds a storage file name from [title] and [extension]. */
fun buildSoundFileName(title: String, extension: String): String =
    "${normalizeSoundTitle(title)}.${normalizeAudioExtension(extension)}"

/**
 * Parses a display name into a normalized title and extension pair.
 *
 * @return `title to extension`, or `null` when the name is invalid.
 */
fun parseAudioFileName(displayName: String): Pair<String, String>? {
    val normalizedName = displayName.trim()
    if (normalizedName.isEmpty()) return null

    val dotIndex = normalizedName.lastIndexOf('.')
    if (dotIndex <= 0 || dotIndex == normalizedName.lastIndex) return null

    val title = normalizeSoundTitle(normalizedName.substring(0, dotIndex))
    val extension = normalizeAudioExtension(normalizedName.substring(dotIndex + 1))

    if (title.isEmpty() || !isSupportedAudioExtension(extension)) return null
    return title to extension
}

/** Returns the MIME type for a supported [extension]. */
fun mimeTypeForExtension(extension: String): String {
    return when (normalizeAudioExtension(extension)) {
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "m4a" -> "audio/mp4"
        else -> "audio/*"
    }
}

/** Returns whether [fileName] matches a supported stored audio file name. */
fun isAudioStorageFileName(fileName: String): Boolean =
    parseAudioFileName(fileName) != null

/** Returns the title portion of a stored audio [fileName], if present. */
fun titleFromStorageFileName(fileName: String): String? =
    parseAudioFileName(fileName)?.first

/** Returns the extension portion of a stored audio [fileName], if present. */
fun extensionFromStorageFileName(fileName: String): String? =
    parseAudioFileName(fileName)?.second
