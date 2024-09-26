package audio.omgsoundboard.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.CategoryEntity
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.domain.models.BackupResult
import audio.omgsoundboard.core.domain.models.SoundBackup
import audio.omgsoundboard.core.domain.models.toBackup
import audio.omgsoundboard.core.domain.repository.StorageRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject


class StorageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val soundsDao: SoundsDao,
    private val categoryDao: CategoryDao,
) : StorageRepository {

    private val gson = Gson()

    data class BackupMetadata(
        val sounds: List<SoundBackup>,
        val categories: List<CategoryEntity>,
    )


    override suspend fun backupFiles(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupFile = context.contentResolver.openOutputStream(uri)
            val privateFolder = File(context.filesDir.absolutePath)

            ZipOutputStream(backupFile).use { zipOut ->
                val sounds = soundsDao.getAllSoundsOnce().map { it.toBackup() }

                val metadata = BackupMetadata(
                    sounds = sounds,
                    categories = categoryDao.getAllCategoriesOnce()
                )

                val metadataJson = gson.toJson(metadata)
                zipOut.putNextEntry(ZipEntry("metadata.json"))
                zipOut.write(metadataJson.toByteArray())
                zipOut.closeEntry()

                privateFolder.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".mp3")) {
                        val entry = ZipEntry(file.name)
                        zipOut.putNextEntry(entry)
                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }

            BackupResult.Success()
        } catch (e: Exception) {
            BackupResult.Error(e)
        }
    }

    override suspend fun restoreBackup(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val privateFolder = File(context.filesDir.absolutePath)
            val backupFile = context.contentResolver.openInputStream(uri)
            var metadata: BackupMetadata? = null
            val restoredSounds = mutableListOf<SoundsEntity>()

            ZipInputStream(backupFile).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "metadata.json" -> {
                            val metadataJson = zipIn.bufferedReader().readText()
                            metadata = gson.fromJson(metadataJson, BackupMetadata::class.java)
                        }

                        entry.name.endsWith(".mp3") -> {
                            val extractedFile = File(privateFolder, entry.name)
                            FileOutputStream(extractedFile).use { output ->
                                zipIn.copyTo(output)
                            }

                            val soundEntity = SoundsEntity(
                                title = entry.name.removeSuffix(".mp3"),
                                uri = Uri.EMPTY,
                                date = System.currentTimeMillis(),
                                isFavorite = false,
                                categoryId = 0,
                                resId = null
                            )

                            restoredSounds.add(soundEntity)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (metadata != null) {
                restoreMetadata(metadata!!)
                BackupResult.Success()
            } else {
                val catName = restoredWithoutMetadata(restoredSounds)
                BackupResult.Success(catName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e)
        }
    }


    private suspend fun restoredWithoutMetadata(sounds: List<SoundsEntity>): String {
        val category = categoryDao.getRandomCategory()

        val restoredSounds = sounds.map { soundBackup ->
            val file = File(context.filesDir, "${soundBackup.title}.mp3")
            val uri = FileProvider.getUriForFile(
                context,
                "audio.omgsoundboard.provider",
                file
            )
            soundBackup.copy(categoryId = category.id, uri = uri)
        }
        soundsDao.insertSounds(restoredSounds)
        return category.name
    }

    private suspend fun restoreMetadata(metadata: BackupMetadata) {
        categoryDao.insertCategories(metadata.categories)

        val restoredSounds = metadata.sounds.map { soundBackup ->
            val file = File(context.filesDir, "${soundBackup.title}.mp3")
            val uri = FileProvider.getUriForFile(
                context,
                "audio.omgsoundboard.provider",
                file
            )
            SoundsEntity(
                title = soundBackup.title,
                uri = uri,
                date = soundBackup.date,
                isFavorite = soundBackup.isFavorite,
                categoryId = soundBackup.categoryId,
                resId = soundBackup.resId
            )
        }
        soundsDao.insertSounds(restoredSounds)
    }
}