package audio.omgsoundboard.core.utils

const val DEFAULT_AUDIO_EXTENSION = "mp3"
const val AUDIO_PICKER_MIME_TYPE = "audio/*"

val SUPPORTED_AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a")

fun normalizeAudioExtension(extension: String): String =
    extension.trim().lowercase().removePrefix(".")

fun isSupportedAudioExtension(extension: String): Boolean =
    normalizeAudioExtension(extension) in SUPPORTED_AUDIO_EXTENSIONS

fun buildSoundFileName(title: String, extension: String): String =
    "${normalizeSoundTitle(title)}.${normalizeAudioExtension(extension)}"

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

fun mimeTypeForExtension(extension: String): String {
    return when (normalizeAudioExtension(extension)) {
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "m4a" -> "audio/mp4"
        else -> "audio/*"
    }
}

fun isAudioStorageFileName(fileName: String): Boolean =
    parseAudioFileName(fileName) != null

fun titleFromStorageFileName(fileName: String): String? =
    parseAudioFileName(fileName)?.first

fun extensionFromStorageFileName(fileName: String): String? =
    parseAudioFileName(fileName)?.second
