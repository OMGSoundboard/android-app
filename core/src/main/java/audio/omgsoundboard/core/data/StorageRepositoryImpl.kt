package audio.omgsoundboard.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.data.local.entities.SoundsEntity
import audio.omgsoundboard.core.domain.models.BackupMetadata
import audio.omgsoundboard.core.domain.models.BackupResult
import audio.omgsoundboard.core.domain.models.toBackup
import audio.omgsoundboard.core.domain.repository.StorageRepository
import audio.omgsoundboard.core.utils.DEFAULT_AUDIO_EXTENSION
import audio.omgsoundboard.core.utils.buildSoundFileName
import audio.omgsoundboard.core.utils.extensionFromStorageFileName
import audio.omgsoundboard.core.utils.isAudioStorageFileName
import audio.omgsoundboard.core.utils.titleFromStorageFileName
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
                    if (isAudioStorageFileName(file.name)) {
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
                            metadata = readMetadataEntry(zipIn)
                        }
                        isRestorableAudioEntry(entry.name) -> {
                            restoredSounds += extractAudioZipEntry(entry, zipIn, privateFolder)
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


    override suspend fun copyToInternalStorage(
        uri: Uri,
        filename: String
    ): Uri? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val destFile = File(context.filesDir, filename)
                destFile.parentFile?.mkdirs()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                } ?: error("InputStream is null")

                destFile.toUri()
            }.getOrElse { throwable ->
                null
            }
        }
    }

    override suspend fun syncWearFiles() {
        try {
            val privateFolder = File(context.filesDir.absolutePath)
            privateFolder.listFiles()?.forEach { file ->
                if (!isAudioStorageFileName(file.name)) return@forEach

                val fileId = file.name.substringBeforeLast('.')
                val uri = FileProvider.getUriForFile(
                    context,
                    "audio.omgsoundboard.provider",
                    file
                )

                val sound = soundsDao.getSoundById(fileId.toIntOrNull() ?: return@forEach)
                if (sound != null){
                    val newSound = sound.copy(uri = uri)
                    soundsDao.updateSound(newSound)
                }
            }
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    private suspend fun restoredWithoutMetadata(sounds: List<SoundsEntity>): String {
        val category = categoryDao.getRandomCategory()

        val restoredSounds = sounds.map { soundBackup ->
            soundBackup.copy(
                categoryId = category.id,
                uri = soundProviderUri(soundBackup.title, soundBackup.fileExtension),
            )
        }
        soundsDao.insertSounds(restoredSounds)
        return category.name
    }

    private suspend fun restoreMetadata(metadata: BackupMetadata) {
        categoryDao.insertCategories(metadata.categories)

        val restoredSounds = metadata.sounds.map { soundBackup ->
            val extension = soundBackup.fileExtension.ifBlank { DEFAULT_AUDIO_EXTENSION }
            SoundsEntity(
                title = soundBackup.title,
                uri = soundProviderUri(soundBackup.title, extension),
                date = soundBackup.date,
                isFavorite = soundBackup.isFavorite,
                categoryId = soundBackup.categoryId,
                resId = soundBackup.resId,
                fileExtension = extension
            )
        }
        soundsDao.insertSounds(restoredSounds)
    }

    private fun readMetadataEntry(zipIn: ZipInputStream): BackupMetadata {
        val metadataJson = zipIn.bufferedReader().readText()
        return gson.fromJson(metadataJson, BackupMetadata::class.java)
    }

    private fun isRestorableAudioEntry(entryName: String): Boolean {
        return entryName.endsWith(".mp3") || isAudioStorageFileName(entryName)
    }

    private fun extractAudioZipEntry(
        entry: ZipEntry,
        zipIn: ZipInputStream,
        privateFolder: File,
    ): SoundsEntity {
        val extractedFile = File(privateFolder, entry.name)
        val normalizedPath = extractedFile.toPath().normalize()
        val targetDirPath = privateFolder.toPath().normalize()
        if (!normalizedPath.startsWith(targetDirPath)) {
            throw IllegalArgumentException("Bad zip entry: ${entry.name}")
        }
        FileOutputStream(extractedFile).use { output ->
            zipIn.copyTo(output)
        }

        val title = titleFromStorageFileName(entry.name)
            ?: entry.name.removeSuffix(".mp3")
        val extension = extensionFromStorageFileName(entry.name)
            ?: DEFAULT_AUDIO_EXTENSION

        return SoundsEntity(
            title = title,
            uri = Uri.EMPTY,
            date = System.currentTimeMillis(),
            isFavorite = false,
            categoryId = 0,
            resId = null,
            fileExtension = extension,
        )
    }

    private fun soundProviderUri(title: String, extension: String): Uri {
        val file = File(context.filesDir, buildSoundFileName(title, extension))
        return FileProvider.getUriForFile(
            context,
            "audio.omgsoundboard.provider",
            file
        )
    }
}
