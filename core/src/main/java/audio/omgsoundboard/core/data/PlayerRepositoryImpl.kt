package audio.omgsoundboard.core.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import androidx.core.content.FileProvider
import audio.omgsoundboard.core.R
import audio.omgsoundboard.core.domain.models.SoundWithUri
import audio.omgsoundboard.core.domain.repository.MediaManager
import audio.omgsoundboard.core.domain.repository.PlayerRepository
import audio.omgsoundboard.core.utils.Constants.STOP_ON_NEW_SOUND
import audio.omgsoundboard.core.utils.Constants.STOP_ON_RETAP
import audio.omgsoundboard.core.utils.buildSoundFileName
import audio.omgsoundboard.core.utils.getExtensionFromUri
import audio.omgsoundboard.core.utils.getTitleFromUri
import audio.omgsoundboard.core.utils.getUriPath
import audio.omgsoundboard.core.utils.isDuplicateSoundFile
import audio.omgsoundboard.core.utils.isSupportedAudioExtension
import audio.omgsoundboard.core.utils.mimeTypeForExtension
import audio.omgsoundboard.core.utils.normalizeAudioExtension
import audio.omgsoundboard.core.utils.normalizeSoundTitle
import audio.omgsoundboard.core.utils.soundFileKey
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PlayerRepositoryImpl @Inject constructor(
    private val context: Context,
) : PlayerRepository {

    private val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mediaPlayerList = mutableMapOf<Int, MediaPlayer>()
    private val progressJobs = mutableMapOf<Int, Job>()
    private val _playbackProgress = MutableStateFlow<Map<Int, Float>>(emptyMap())
    override val playbackProgress: StateFlow<Map<Int, Float>> = _playbackProgress.asStateFlow()

    private fun startProgressPolling(index: Int) {
        progressJobs[index]?.cancel()
        progressJobs[index] = scope.launch {
            try {
                while (true) {
                    val mp = mediaPlayerList[index] ?: break
                    try {
                        val duration = mp.duration
                        val position = mp.currentPosition
                        if (duration > 0) {
                            _playbackProgress.value += (index to position.toFloat() / duration.toFloat())
                        }
                    } catch (_: IllegalStateException) {
                        break
                    }
                    delay(100)
                }
            } finally {
                _playbackProgress.value -= index
                progressJobs.remove(index)
            }
        }
    }

    private fun stopSound(index: Int) {
        progressJobs[index]?.cancel()
        progressJobs.remove(index)
        mediaPlayerList[index]?.apply { reset(); release() }
        mediaPlayerList.remove(index)
        _playbackProgress.value -= index
    }

    override fun playFile(index: Int, resourceId: Int?, uri: Uri) {
        if (uri == Uri.EMPTY && resourceId == null) return

        val stopOnRetap = sharedPref.getBoolean(STOP_ON_RETAP, false)
        val stopOnNewSound = sharedPref.getBoolean(STOP_ON_NEW_SOUND, false)

        val playerUri = if (uri == Uri.EMPTY) {
            getUriPath(context, resourceId!!)
        } else {
            uri
        }

        if (mediaPlayerList.contains(index)) {
            stopSound(index)
            if (stopOnRetap) return
            // stopOnRetap is false: fall through to restart the sound
        }

        if (stopOnNewSound) {
            val indices = mediaPlayerList.keys.toList()
            indices.forEach { stopSound(it) }
        }

        val mediaPlayer = MediaPlayer.create(context, playerUri) ?: return
        mediaPlayerList[index] = mediaPlayer
        mediaPlayer.start()
        startProgressPolling(index)
        mediaPlayer.setOnCompletionListener {
            stopSound(index)
        }
    }

    override fun shareFile(fileName: String, resourceId: Int?, uri: Uri) {
        try {
            val audioUri = if (uri == Uri.EMPTY) {
                getAudioUri("$fileName.mp3", resourceId!!)
            } else {
                uri
            }
            context.grantUriPermission("android", audioUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, audioUri)

            val intentChooser =
                Intent.createChooser(
                    shareIntent,
                    context.resources.getString(R.string.drop_menu_share)
                )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intentChooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setMedia(
        type: MediaManager,
        fileName: String,
        resourceId: Int?,
        cUri: Uri,
        extension: String,
    ) {

        var mediaType = RingtoneManager.TYPE_RINGTONE
        val normalizedExtension = normalizeAudioExtension(extension)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {

            val mediaUri = if (cUri == Uri.EMPTY) {
               getAudioUri(buildSoundFileName(fileName, normalizedExtension), resourceId!!)
            } else {
                cUri
            }

            mediaType = when (type) {
                MediaManager.Ringtone -> {
                    RingtoneManager.TYPE_RINGTONE
                }
                MediaManager.Notification -> {
                    RingtoneManager.TYPE_NOTIFICATION
                }
                MediaManager.Alarm -> {
                    RingtoneManager.TYPE_ALARM
                }
            }

            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                mediaType,
                mediaUri
            )

        } else {
            val uri = if (cUri == Uri.EMPTY) {
                getAudioUri(buildSoundFileName(fileName, normalizedExtension), resourceId!!)
            } else {
                cUri
            }

            if (uri == null) return

            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, uri.path)
            values.put(MediaStore.MediaColumns.TITLE, fileName)
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeForExtension(normalizedExtension))
            values.put(AudioColumns.ARTIST, context.getString(R.string.app_name))
            values.put(AudioColumns.IS_MUSIC, false);

            when (type) {
                MediaManager.Ringtone -> {
                    values.put(AudioColumns.IS_RINGTONE, true)
                    values.put(AudioColumns.IS_NOTIFICATION, false)
                    values.put(AudioColumns.IS_ALARM, false)
                    mediaType = RingtoneManager.TYPE_RINGTONE
                }
                MediaManager.Notification -> {
                    values.put(AudioColumns.IS_RINGTONE, false)
                    values.put(AudioColumns.IS_NOTIFICATION, true)
                    values.put(AudioColumns.IS_ALARM, false)
                    mediaType = RingtoneManager.TYPE_NOTIFICATION
                }
                MediaManager.Alarm -> {
                    values.put(AudioColumns.IS_RINGTONE, false)
                    values.put(AudioColumns.IS_NOTIFICATION, false)
                    values.put(AudioColumns.IS_ALARM, true)
                    mediaType = RingtoneManager.TYPE_ALARM
                }
            }

            val url = MediaStore.Audio.Media.getContentUriForPath(uri.path!!)
            context.contentResolver.delete(
                url!!,
                MediaStore.MediaColumns.DATA + "=\"" + uri.path + "\"",
                null
            );
            val mediaUri = context.contentResolver.insert(url, values)

            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                mediaType,
                mediaUri
            )
        }
    }

    override fun addSound(fileName: String, uri: Uri, extension: String): Uri? {
        val normalizedExtension = normalizeAudioExtension(extension)
        if (!isSupportedAudioExtension(normalizedExtension)) return null

        val inputStream = context.contentResolver.openInputStream(uri)

        if (inputStream != null) {
            val outputFile = File(context.filesDir, buildSoundFileName(fileName, normalizedExtension))
            var outputStream: FileOutputStream? = null

            try {
                outputStream = FileOutputStream(outputFile)
                val bufferSize = 1024
                val buffer = ByteArray(bufferSize)
                var length: Int

                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.flush()
                    inputStream.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return FileProvider.getUriForFile(
                context,
                "audio.omgsoundboard.provider",
                outputFile
            )
        }
        return null
    }

    override fun addMultipleSounds(
        uris: List<Uri>,
        existingSoundKeys: Set<String>,
    ): List<SoundWithUri> {
        val knownSoundKeys = existingSoundKeys.toMutableSet()
        val addedInBatch = mutableSetOf<String>()

        return uris.mapNotNull { uri ->
            val title = getTitleFromUri(context, uri) ?: ""
            val extension = getExtensionFromUri(context, uri) ?: return@mapNotNull null
            val normalizedTitle = normalizeSoundTitle(title)
            val normalizedExtension = normalizeAudioExtension(extension)

            if (normalizedTitle.isEmpty() || !isSupportedAudioExtension(normalizedExtension)) {
                return@mapNotNull null
            }

            val soundKey = soundFileKey(normalizedTitle, normalizedExtension)
            if (isDuplicateSoundFile(normalizedTitle, normalizedExtension, knownSoundKeys) ||
                !addedInBatch.add(soundKey)
            ) {
                return@mapNotNull null
            }

            val outputFile = File(
                context.filesDir,
                buildSoundFileName(normalizedTitle, normalizedExtension)
            )
            if (outputFile.exists()) {
                return@mapNotNull null
            }

            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream != null) {
                var outputStream: FileOutputStream? = null

                try {
                    outputStream = FileOutputStream(outputFile)
                    val bufferSize = 1024
                    val buffer = ByteArray(bufferSize)
                    var length: Int

                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }

                    knownSoundKeys.add(soundKey)

                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "audio.omgsoundboard.provider",
                        outputFile
                    )
                    SoundWithUri(normalizedTitle, fileUri, normalizedExtension)
                } catch (e: IOException) {
                    e.printStackTrace()
                    addedInBatch.remove(soundKey)
                    null
                } finally {
                    try {
                        outputStream?.flush()
                        inputStream.close()
                        outputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else {
                addedInBatch.remove(soundKey)
                null
            }
        }
    }

    private fun getAudioUri(fileName: String, resourceId: Int): Uri? {
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val outputFile = File(context.cacheDir, fileName)

            val outputStream = FileOutputStream(outputFile)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            return FileProvider.getUriForFile(
                context,
                "audio.omgsoundboard.provider",
                outputFile
            )
        } catch (e: Exception) {
            return null
        }
    }

}