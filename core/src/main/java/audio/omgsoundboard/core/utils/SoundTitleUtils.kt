package audio.omgsoundboard.core.utils

/** Trims leading and trailing whitespace from a sound title. */
fun normalizeSoundTitle(title: String): String = title.trim()

/** Returns a case-insensitive key for comparing sound titles. */
fun normalizedSoundTitleKey(title: String): String = normalizeSoundTitle(title).lowercase()

/**
 * Returns a deduplication key for a sound file.
 *
 * @param title Display title of the sound.
 * @param extension File extension without a leading dot.
 */
fun soundFileKey(title: String, extension: String): String =
    "${normalizedSoundTitleKey(title)}|${normalizeAudioExtension(extension)}"

/** Returns whether a sound with [title] and [extension] already exists in [existingKeys]. */
fun isDuplicateSoundFile(title: String, extension: String, existingKeys: Set<String>): Boolean {
    if (normalizeSoundTitle(title).isEmpty()) return false
    return soundFileKey(title, extension) in existingKeys
}

/** Builds a set of deduplication keys from stored sound identities. */
fun existingSoundFileKeys(identities: Collection<Pair<String, String>>): Set<String> {
    return identities.map { (title, extension) -> soundFileKey(title, extension) }.toSet()
}
