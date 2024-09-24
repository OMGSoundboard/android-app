package audio.omgsoundboard.core.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.repository.StorageRepository
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject


class StorageRepositoryImpl @Inject constructor(
    private val context: Context,
): StorageRepository {


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
            //customSoundsDao.insertCustomSound(CustomSoundsEntity(title = it.title, uri = it.uri, date = System.currentTimeMillis()))
        }

    }
}