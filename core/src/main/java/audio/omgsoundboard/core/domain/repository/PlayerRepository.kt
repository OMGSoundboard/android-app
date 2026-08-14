package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.SoundWithUri
import kotlinx.coroutines.flow.StateFlow

/** Supported media types for ringtone, alarm, and notification assignment. */
enum class MediaManager {
    Ringtone,
    Alarm,
    Notification
}

/** Plays sounds and manages imported audio files. */
interface PlayerRepository {
    /** Playback progress keyed by sound list index. */
    val playbackProgress: StateFlow<Map<Int, Float>>
    /** Starts playback for the sound at [index]. */
    fun playFile(index: Int, resourceId: Int?, uri: Uri)
    /** Shares a sound file through the system share sheet. */
    fun shareFile(fileName: String, resourceId: Int?, uri: Uri)
    /** Sets a sound as the device ringtone, alarm, or notification sound. */
    fun setMedia(type: MediaManager, fileName: String, resourceId: Int?, cUri: Uri, extension: String = "mp3")
    /** Copies a single imported sound into app storage. */
    fun addSound(fileName: String, uri: Uri, extension: String): Uri?
    /** Imports multiple sounds while skipping duplicates in [existingSoundKeys]. */
    fun addMultipleSounds(uris: List<Uri>, existingSoundKeys: Set<String> = emptySet()): List<SoundWithUri>
}