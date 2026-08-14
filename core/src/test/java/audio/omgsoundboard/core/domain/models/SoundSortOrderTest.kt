package audio.omgsoundboard.core.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [SoundSortOrder]. */
class SoundSortOrderTest {

    @Test
    fun `sorts by title ascending`() {
        val sounds = listOf(
            PlayableSound(id = 1, title = "Zebra"),
            PlayableSound(id = 2, title = "alpha"),
            PlayableSound(id = 3, title = "Beta"),
        )

        val sorted = sounds.sortedBy(SoundSortOrder.TITLE_ASC)

        assertEquals(listOf("alpha", "Beta", "Zebra"), sorted.map { it.title })
    }

    @Test
    fun `sorts by most used then recently added`() {
        val sounds = listOf(
            PlayableSound(id = 1, title = "A", playCount = 2, date = 10),
            PlayableSound(id = 2, title = "B", playCount = 5, date = 1),
            PlayableSound(id = 3, title = "C", playCount = 5, date = 20),
        )

        val sorted = sounds.sortedBy(SoundSortOrder.MOST_USED)

        assertEquals(listOf("C", "B", "A"), sorted.map { it.title })
    }
}
