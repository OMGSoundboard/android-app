package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.SoundWithUri
import kotlinx.coroutines.flow.StateFlow

enum class MediaManager {
    Ringtone,
    Alarm,
    Notification
}

interface PlayerRepository {
    val playbackProgress: StateFlow<Map<Int, Float>>
    fun playFile(index: Int, resourceId: Int?, uri: Uri)
    fun shareFile(fileName: String, resourceId: Int?, uri: Uri)
    fun setMedia(type: MediaManager, fileName: String, resourceId: Int?, cUri: Uri, extension: String = "mp3")
    fun addSound(fileName: String, uri: Uri, extension: String): Uri?
    fun addMultipleSounds(uris: List<Uri>, existingSoundKeys: Set<String> = emptySet()): List<SoundWithUri>
}