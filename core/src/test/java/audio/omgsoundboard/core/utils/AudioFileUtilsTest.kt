package audio.omgsoundboard.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioFileUtilsTest {

    @Test
    fun `parseAudioFileName handles dotted titles`() {
        assertEquals("my.sound" to "wav", parseAudioFileName("my.sound.wav"))
    }

    @Test
    fun `parseAudioFileName rejects unsupported extensions`() {
        assertNull(parseAudioFileName("clip.flac"))
    }

    @Test
    fun `buildSoundFileName preserves normalized title`() {
        assertEquals("airhorn.wav", buildSoundFileName(" airhorn ", "WAV"))
    }
}
