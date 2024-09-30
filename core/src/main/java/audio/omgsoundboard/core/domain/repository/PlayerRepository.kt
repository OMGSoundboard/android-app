package audio.omgsoundboard.core.domain.repository

import android.net.Uri
import audio.omgsoundboard.core.domain.models.SoundWithUri

enum class MediaManager {
    Ringtone,
    Alarm,
    Notification
}

interface PlayerRepository {
    fun playFile(index: Int, resourceId: Int?, uri: Uri)
    fun shareFile(fileName: String, resourceId: Int?, uri: Uri)
    fun setMedia(type: MediaManager, fileName: String, resourceId: Int?, cUri: Uri)
    fun addSound(fileName: String, uri: Uri) : Uri?
    fun addMultipleSounds (uris: List<Uri>) : List<SoundWithUri>
}