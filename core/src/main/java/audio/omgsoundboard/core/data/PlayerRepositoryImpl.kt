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
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
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
        }

        if (stopOnNewSound) {
            mediaPlayerList.keys.toList().forEach { stopSound(it) }
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
        val normalizedExtension = normalizeAudioExtension(extension)
        val mediaUri = resolveSoundMediaUri(cUri, fileName, normalizedExtension, resourceId) ?: return
        val mediaType = ringtoneTypeFor(type)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            RingtoneManager.setActualDefaultRingtoneUri(context, mediaType, mediaUri)
            return
        }

        setLegacyMedia(type, fileName, normalizedExtension, mediaUri, mediaType)
    }

    override fun addSound(fileName: String, uri: Uri, extension: String): Uri? {
        val normalizedExtension = normalizeAudioExtension(extension)
        if (!isSupportedAudioExtension(normalizedExtension)) return null

        val outputFile = File(context.filesDir, buildSoundFileName(fileName, normalizedExtension))
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        return try {
            if (copyStreamToFile(inputStream, outputFile)) {
                FileProvider.getUriForFile(
                    context,
                    "audio.omgsoundboard.provider",
                    outputFile
                )
            } else {
                null
            }
        } finally {
            closeQuietly(inputStream)
        }
    }

    override fun addMultipleSounds(
        uris: List<Uri>,
        existingSoundKeys: Set<String>,
    ): List<SoundWithUri> {
        val knownSoundKeys = existingSoundKeys.toMutableSet()
        val addedInBatch = mutableSetOf<String>()

        return uris.mapNotNull { uri ->
            importSoundFromUri(uri, knownSoundKeys, addedInBatch)
        }
    }

    private fun importSoundFromUri(
        uri: Uri,
        knownSoundKeys: MutableSet<String>,
        addedInBatch: MutableSet<String>,
    ): SoundWithUri? {
        val candidate = parseImportCandidate(uri) ?: return null
        if (shouldSkipImport(candidate, knownSoundKeys, addedInBatch)) {
            return null
        }

        val outputFile = File(
            context.filesDir,
            buildSoundFileName(candidate.title, candidate.extension)
        )
        if (outputFile.exists()) {
            return null
        }

        return persistImportedSound(uri, candidate, outputFile, knownSoundKeys, addedInBatch)
    }

    private fun parseImportCandidate(uri: Uri): SoundImportCandidate? {
        val title = getTitleFromUri(context, uri) ?: ""
        val extension = getExtensionFromUri(context, uri) ?: return null
        val normalizedTitle = normalizeSoundTitle(title)
        val normalizedExtension = normalizeAudioExtension(extension)

        if (normalizedTitle.isEmpty() || !isSupportedAudioExtension(normalizedExtension)) {
            return null
        }

        return SoundImportCandidate(
            title = normalizedTitle,
            extension = normalizedExtension,
            soundKey = soundFileKey(normalizedTitle, normalizedExtension),
        )
    }

    private fun shouldSkipImport(
        candidate: SoundImportCandidate,
        knownSoundKeys: Set<String>,
        addedInBatch: MutableSet<String>,
    ): Boolean {
        return isDuplicateSoundFile(candidate.title, candidate.extension, knownSoundKeys) ||
            !addedInBatch.add(candidate.soundKey)
    }

    private fun persistImportedSound(
        uri: Uri,
        candidate: SoundImportCandidate,
        outputFile: File,
        knownSoundKeys: MutableSet<String>,
        addedInBatch: MutableSet<String>,
    ): SoundWithUri? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: run {
            addedInBatch.remove(candidate.soundKey)
            return null
        }

        return inputStream.use { stream ->
            if (!copyStreamToFile(stream, outputFile)) {
                addedInBatch.remove(candidate.soundKey)
                return null
            }

            knownSoundKeys.add(candidate.soundKey)
            val fileUri = FileProvider.getUriForFile(
                context,
                "audio.omgsoundboard.provider",
                outputFile
            )
            SoundWithUri(candidate.title, fileUri, candidate.extension)
        }
    }

    private data class SoundImportCandidate(
        val title: String,
        val extension: String,
        val soundKey: String,
    )

    private fun resolveSoundMediaUri(
        cUri: Uri,
        fileName: String,
        extension: String,
        resourceId: Int?,
    ): Uri? {
        return if (cUri == Uri.EMPTY) {
            resourceId?.let { getAudioUri(buildSoundFileName(fileName, extension), it) }
        } else {
            cUri
        }
    }

    private fun setLegacyMedia(
        type: MediaManager,
        fileName: String,
        extension: String,
        uri: Uri,
        mediaType: Int,
    ) {
        val values = createLegacyMediaValues(type, fileName, extension, uri)
        val mediaUri = insertLegacyMediaUri(uri, values)
        RingtoneManager.setActualDefaultRingtoneUri(context, mediaType, mediaUri)
    }

    private fun createLegacyMediaValues(
        type: MediaManager,
        fileName: String,
        extension: String,
        uri: Uri,
    ): ContentValues {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, uri.path)
            put(MediaStore.MediaColumns.TITLE, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeForExtension(extension))
            put(AudioColumns.ARTIST, context.getString(R.string.app_name))
            put(AudioColumns.IS_MUSIC, false)
        }
        applyLegacyMediaTypeFlags(type, values)
        return values
    }

    private fun insertLegacyMediaUri(uri: Uri, values: ContentValues): Uri? {
        val url = MediaStore.Audio.Media.getContentUriForPath(uri.path!!)
        context.contentResolver.delete(
            url!!,
            MediaStore.MediaColumns.DATA + "=\"" + uri.path + "\"",
            null
        )
        return context.contentResolver.insert(url, values)
    }

    private fun applyLegacyMediaTypeFlags(type: MediaManager, values: ContentValues) {
        when (type) {
            MediaManager.Ringtone -> {
                values.put(AudioColumns.IS_RINGTONE, true)
                values.put(AudioColumns.IS_NOTIFICATION, false)
                values.put(AudioColumns.IS_ALARM, false)
            }
            MediaManager.Notification -> {
                values.put(AudioColumns.IS_RINGTONE, false)
                values.put(AudioColumns.IS_NOTIFICATION, true)
                values.put(AudioColumns.IS_ALARM, false)
            }
            MediaManager.Alarm -> {
                values.put(AudioColumns.IS_RINGTONE, false)
                values.put(AudioColumns.IS_NOTIFICATION, false)
                values.put(AudioColumns.IS_ALARM, true)
            }
        }
    }

    private fun ringtoneTypeFor(type: MediaManager): Int = when (type) {
        MediaManager.Ringtone -> RingtoneManager.TYPE_RINGTONE
        MediaManager.Notification -> RingtoneManager.TYPE_NOTIFICATION
        MediaManager.Alarm -> RingtoneManager.TYPE_ALARM
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File): Boolean {
        var outputStream: FileOutputStream? = null
        return try {
            outputStream = FileOutputStream(outputFile)
            copyStream(inputStream, outputStream)
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            closeQuietly(outputStream)
        }
    }

    private fun copyStream(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    }

    private fun closeQuietly(vararg closeables: Closeable?) {
        closeables.forEach { closeable ->
            try {
                closeable?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getAudioUri(fileName: String, resourceId: Int): Uri? {
        return try {
            context.resources.openRawResource(resourceId).use { inputStream ->
                val outputFile = File(context.cacheDir, fileName)
                if (copyStreamToFile(inputStream, outputFile)) {
                    FileProvider.getUriForFile(
                        context,
                        "audio.omgsoundboard.provider",
                        outputFile
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val BUFFER_SIZE = 1024
    }
}
