package audio.omgsoundboard.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [AudioFileUtils]. */
class AudioFileUtilsTest {

    /** Parses titles that contain dots before the extension. */
    @Test
    fun `parseAudioFileName handles dotted titles`() {
        assertEquals("my.sound" to "wav", parseAudioFileName("my.sound.wav"))
    }

    /** Rejects file names with unsupported extensions. */
    @Test
    fun `parseAudioFileName rejects unsupported extensions`() {
        assertNull(parseAudioFileName("clip.flac"))
    }

    /** Normalizes whitespace and extension casing in generated file names. */
    @Test
    fun `buildSoundFileName preserves normalized title`() {
        assertEquals("airhorn.wav", buildSoundFileName(" airhorn ", "WAV"))
    }
}
