package audio.omgsoundboard

import android.net.Uri
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.CategoryEntity
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.Constants.AUDIO_TRANSFER_PREFIX
import audio.omgsoundboard.core.utils.Constants.LEGACY_MP3_TRANSFER_PREFIX
import audio.omgsoundboard.core.utils.Constants.METADATA_KEY
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION
import audio.omgsoundboard.core.utils.buildSoundFileName
import audio.omgsoundboard.core.utils.Constants.METADATA_PATH
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {

    @Inject
    lateinit var categoryDao: CategoryDao

    @Inject
    lateinit var soundsDao: SoundsDao


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.dataItem.uri.path == METADATA_PATH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val jsonData = dataMap.getString(METADATA_KEY)

                if (jsonData != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        processReceivedData(jsonData)
                    }
                }
            }
        }
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        if (channel.path.startsWith(AUDIO_TRANSFER_PREFIX) ||
            channel.path.startsWith(LEGACY_MP3_TRANSFER_PREFIX)
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                receiveFile(channel)
            }
        }
    }

    private suspend fun receiveFile(channel: ChannelClient.Channel) {
        val fileId = channel.path.substringAfterLast("/")
        val channelClient = Wearable.getChannelClient(this)
        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            withContext(Dispatchers.IO) {
                inputStream = channelClient.getInputStream(channel).await()
                val extension = resolveWearExtension(fileId)
                val file = File(
                    this@DataLayerListenerService.filesDir,
                    buildSoundFileName(fileId, extension)
                )
                fileOutputStream = FileOutputStream(file)
                copyStream(inputStream!!, fileOutputStream!!)
                fileOutputStream?.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error receiving file: ${e.message}")
        } finally {
            withContext(Dispatchers.IO) {
                closeQuietly(inputStream, fileOutputStream)
            }
        }
    }

    private suspend fun resolveWearExtension(fileId: String): String {
        val soundId = fileId.toIntOrNull() ?: return DEFAULT_AUDIO_EXTENSION
        return soundsDao.getSoundById(soundId)?.fileExtension ?: DEFAULT_AUDIO_EXTENSION
    }

    private suspend fun processReceivedData(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val categories = parseCategories(jsonObject.getJSONArray("categories"))
            val sounds = parseSounds(jsonObject.getJSONArray("sounds"))

            categoryDao.deleteAllCategories()
            soundsDao.deleteAllSounds()
            categories.forEach { categoryDao.insertCategory(it) }
            sounds.forEach { soundsDao.insertSound(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseCategories(categoriesArray: JSONArray): List<CategoryEntity> {
        return buildList {
            for (index in 0 until categoriesArray.length()) {
                val categoryJson = categoriesArray.getJSONObject(index)
                add(
                    CategoryEntity(
                        id = categoryJson.getInt("id"),
                        name = categoryJson.getString("name")
                    )
                )
            }
        }
    }

    private fun parseSounds(soundsArray: JSONArray): List<SoundsEntity> {
        return buildList {
            for (index in 0 until soundsArray.length()) {
                val soundJson = soundsArray.getJSONObject(index)
                add(
                    SoundsEntity(
                        id = soundJson.getInt("id"),
                        title = soundJson.getString("title"),
                        uri = Uri.EMPTY,
                        date = soundJson.getLong("date"),
                        isFavorite = soundJson.getBoolean("isFavorite"),
                        categoryId = if (soundJson.has("categoryId")) soundJson.getInt("categoryId") else null,
                        resId = if (soundJson.has("resId")) soundJson.getInt("resId") else null,
                        fileExtension = soundJson.optString("fileExtension", DEFAULT_AUDIO_EXTENSION),
                        playCount = soundJson.optInt("playCount", 0),
                    )
                )
            }
        }
    }

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
    }

    private fun closeQuietly(vararg closeables: AutoCloseable?) {
        closeables.forEach { closeable ->
            try {
                closeable?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private companion object {
        const val BUFFER_SIZE = 1024
    }
}
