package audio.omgsoundboard.core.utils

fun normalizeSoundTitle(title: String): String = title.trim()

fun normalizedSoundTitleKey(title: String): String = normalizeSoundTitle(title).lowercase()

fun soundFileKey(title: String, extension: String): String =
    "${normalizedSoundTitleKey(title)}|${normalizeAudioExtension(extension)}"

fun isDuplicateSoundFile(title: String, extension: String, existingKeys: Set<String>): Boolean {
    if (normalizeSoundTitle(title).isEmpty()) return false
    return soundFileKey(title, extension) in existingKeys
}

fun existingSoundFileKeys(identities: Collection<Pair<String, String>>): Set<String> {
    return identities.map { (title, extension) -> soundFileKey(title, extension) }.toSet()
}
