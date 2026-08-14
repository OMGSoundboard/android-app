package audio.omgsoundboard.core.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [SoundTitleUtils]. */
class SoundTitleUtilsTest {

    @Test
    fun `duplicate detection is case insensitive for title and extension`() {
        val existing = existingSoundFileKeys(listOf("Airhorn" to "wav"))

        assertTrue(isDuplicateSoundFile("airhorn", "wav", existing))
        assertTrue(isDuplicateSoundFile(" AIRHORN ", "WAV", existing))
        assertFalse(isDuplicateSoundFile("Airhorn", "mp3", existing))
        assertFalse(isDuplicateSoundFile("Beep", "wav", existing))
    }

    @Test
    fun `duplicate detection ignores blank titles`() {
        val existing = existingSoundFileKeys(listOf("Beep" to "mp3"))

        assertFalse(isDuplicateSoundFile("   ", "mp3", existing))
    }
}
