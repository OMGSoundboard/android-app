package audio.omgsoundboard.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import audio.omgsoundboard.data.local.CustomSoundsDao
import audio.omgsoundboard.data.local.CustomSoundsEntity
import audio.omgsoundboard.data.local.FavoritesDao
import audio.omgsoundboard.data.local.toEntity
import audio.omgsoundboard.domain.models.PlayableSound
import audio.omgsoundboard.domain.models.toDomain
import audio.omgsoundboard.domain.repository.StorageRepository
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject


class StorageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val favoritesDao: FavoritesDao,
    private val customSoundsDao: CustomSoundsDao
): StorageRepository {

    override suspend fun getAllFavorites(): List<PlayableSound> {
       return favoritesDao.getFavorites().map { it.toDomain() }
    }

    override suspend fun insertNewFavorite(sound: PlayableSound) {
        favoritesDao.insertFavorite(sound.toEntity())
    }

    override suspend fun deleteFavorite(favoriteId: Int) {
       favoritesDao.deleteFavorite(favoriteId)
    }

    override suspend fun getAllCustomSounds(): List<PlayableSound> {
       return customSoundsDao.getCustomSounds().map { it.toDomain() }
    }

    override suspend fun insertNewCustomSound(title: String, uri: Uri) : List<PlayableSound> {
        customSoundsDao.insertCustomSound(CustomSoundsEntity(title = title, uri = uri, date = System.currentTimeMillis()))
        return customSoundsDao.getCustomSounds().map { it.toDomain() }
    }

    override suspend fun deleteCustomSound(customSoundId: Int) {
       customSoundsDao.deleteCustomSound(customSoundId)
    }

    override fun backupFiles(uri: Uri, metadata: List<PlayableSound>) {
        runCatching {
            val privateFolder = File(context.filesDir.absolutePath)
            val backupFile = context.contentResolver.openOutputStream(uri)

            ZipOutputStream(backupFile).use { zipOut ->
                privateFolder.listFiles()?.forEach { file ->
                    if (file.name != "profileInstalled"){

                        val entry = ZipEntry(file.name)
                        zipOut.putNextEntry(entry)

                        file.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }

                        zipOut.closeEntry()

                    }
                }
            }

            backupFile?.close()
        }
    }

    override suspend fun restoreBackup(uri: Uri) {
        val privateFolder = File(context.filesDir.absolutePath)
        val backupFile = context.contentResolver.openInputStream(uri)

        val playableSounds = arrayListOf<PlayableSound>()

        ZipInputStream(backupFile).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry

            while (entry != null) {
                val extractedFile = File(privateFolder, entry.name)

                if (!entry.isDirectory) {

                    if (entry.name.endsWith(".mp3")){
                        FileOutputStream(extractedFile).use { output ->
                            zipIn.copyTo(output)
                        }

                        val extractedFileUri =  FileProvider.getUriForFile(
                            context,
                            "audio.omgsoundboard.provider",
                            extractedFile
                        )

                        playableSounds.add(PlayableSound(title = entry.name.replace(".mp3", ""), uri = extractedFileUri))
                    }
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        playableSounds.forEach {
            customSoundsDao.insertCustomSound(CustomSoundsEntity(title = it.title, uri = it.uri, date = System.currentTimeMillis()))
        }

    }
}