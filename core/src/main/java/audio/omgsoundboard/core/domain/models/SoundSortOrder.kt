package audio.omgsoundboard.core.domain.models

enum class SoundSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    MOST_USED,
    RECENTLY_ADDED,
}

fun toSoundSortOrder(value: String): SoundSortOrder {
    return runCatching { SoundSortOrder.valueOf(value) }
        .getOrDefault(SoundSortOrder.TITLE_ASC)
}

fun List<PlayableSound>.sortedBy(order: SoundSortOrder): List<PlayableSound> {
    return when (order) {
        SoundSortOrder.TITLE_ASC -> sortedWith(
            compareBy({ it.title.lowercase() }, { it.id })
        )
        SoundSortOrder.TITLE_DESC -> sortedWith(
            compareByDescending<PlayableSound> { it.title.lowercase() }.thenByDescending { it.id }
        )
        SoundSortOrder.MOST_USED -> sortedWith(
            compareByDescending<PlayableSound> { it.playCount }
                .thenByDescending { it.date }
                .thenBy { it.title.lowercase() }
        )
        SoundSortOrder.RECENTLY_ADDED -> sortedWith(
            compareByDescending<PlayableSound> { it.date }.thenBy { it.title.lowercase() }
        )
    }
}
