package audio.omgsoundboard.core.domain.models

import androidx.annotation.StringRes
import audio.omgsoundboard.core.R

/** Supported sort orders for the sound list. */
enum class SoundSortOrder(@StringRes val labelResId: Int) {
    TITLE_ASC(R.string.sort_alpha_asc),
    TITLE_DESC(R.string.sort_alpha_desc),
    MOST_USED(R.string.sort_most_used),
    RECENTLY_ADDED(R.string.sort_recently_added),
}

/** Parses a persisted sort order, falling back to alphabetical ascending. */
fun toSoundSortOrder(value: String): SoundSortOrder {
    return runCatching { SoundSortOrder.valueOf(value) }
        .getOrDefault(SoundSortOrder.TITLE_ASC)
}

/** Returns a copy of the list sorted by [order]. */
fun List<PlayableSound>.sortedBy(order: SoundSortOrder): List<PlayableSound> {
    return when (order) {
        SoundSortOrder.TITLE_ASC -> sortedWith(titleAscendingComparator())
        SoundSortOrder.TITLE_DESC -> sortedWith(titleDescendingComparator())
        SoundSortOrder.MOST_USED -> sortedWith(mostUsedComparator())
        SoundSortOrder.RECENTLY_ADDED -> sortedWith(recentlyAddedComparator())
    }
}

private fun titleAscendingComparator(): Comparator<PlayableSound> =
    compareBy({ it.title.lowercase() }, { it.id })

private fun titleDescendingComparator(): Comparator<PlayableSound> =
    compareByDescending<PlayableSound> { it.title.lowercase() }.thenByDescending { it.id }

private fun mostUsedComparator(): Comparator<PlayableSound> =
    compareByDescending<PlayableSound> { it.playCount }
        .thenByDescending { it.date }
        .thenBy { it.title.lowercase() }

private fun recentlyAddedComparator(): Comparator<PlayableSound> =
    compareByDescending<PlayableSound> { it.date }.thenBy { it.title.lowercase() }
