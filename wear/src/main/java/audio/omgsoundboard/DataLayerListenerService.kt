package audio.omgsoundboard

import android.net.Uri
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.CategoryEntity
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.utils.Constants.METADATA_KEY
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
        if (channel.path.startsWith("/mp3_transfer")) {
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
                val file = File(this@DataLayerListenerService.filesDir, "$fileId.mp3")
                fileOutputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    fileOutputStream?.write(buffer, 0, bytesRead)
                }

                fileOutputStream?.flush()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println("Error receiving file: ${e.message}")

        } finally {
            withContext(Dispatchers.IO) {
                try {
                    inputStream?.close()
                    fileOutputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Error closing streams: ${e.message}")
                }
            }
        }
    }

    private suspend fun processReceivedData(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val categoriesArray = jsonObject.getJSONArray("categories")
            val soundsArray = jsonObject.getJSONArray("sounds")

            val categories = mutableListOf<CategoryEntity>()
            for (i in 0 until categoriesArray.length()) {
                val categoryJson = categoriesArray.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        id = categoryJson.getInt("id"),
                        name = categoryJson.getString("name")
                    )
                )
            }

            val sounds = mutableListOf<SoundsEntity>()
            for (i in 0 until soundsArray.length()) {
                val soundJson = soundsArray.getJSONObject(i)
                sounds.add(
                    SoundsEntity(
                        id = soundJson.getInt("id"),
                        title = soundJson.getString("title"),
                        uri = Uri.EMPTY,
                        date = soundJson.getLong("date"),
                        isFavorite = soundJson.getBoolean("isFavorite"),
                        categoryId = if (soundJson.has("categoryId")) soundJson.getInt("categoryId") else null,
                        resId = if (soundJson.has("resId")) soundJson.getInt("resId") else null
                    )
                )
            }

            categoryDao.deleteAllCategories()
            soundsDao.deleteAllSounds()
            categories.forEach { categoryDao.insertCategory(it) }
            sounds.forEach { soundsDao.insertSound(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}