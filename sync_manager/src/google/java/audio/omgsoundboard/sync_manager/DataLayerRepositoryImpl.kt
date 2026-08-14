package audio.omgsoundboard.sync_manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.BackupMetadata
import audio.omgsoundboard.core.domain.models.PlayableSound
import audio.omgsoundboard.core.domain.models.WearNode
import audio.omgsoundboard.core.domain.models.toBackup
import audio.omgsoundboard.core.domain.models.toDomain
import audio.omgsoundboard.core.utils.Constants.AUDIO_TRANSFER_PREFIX
import audio.omgsoundboard.core.utils.Constants.LEGACY_MP3_TRANSFER_PREFIX
import audio.omgsoundboard.core.utils.Constants.METADATA_KEY
import audio.omgsoundboard.core.utils.Constants.METADATA_PATH
import audio.omgsoundboard.core.utils.Constants.WEAR_CAPABILITY
import audio.omgsoundboard.core.utils.getFileFromUri
import audio.omgsoundboard.core.utils.getUriPath
import audio.omgsoundboard.core.utils.makeMetadataJson
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class DataLayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: CategoryDao,
    private val soundsDao: SoundsDao,
) : DataLayerRepository {

    override fun getConnectedWearNodesAsFlow(): Flow<List<WearNode>> = callbackFlow {
        try {
            val capabilityClient = Wearable.getCapabilityClient(context)
            trySend(fetchInstalledWearNodes())
            val capabilityListener = createCapabilityListener { nodes, installedWatchNodes ->
                trySend(mapNodesToStatus(nodes, installedWatchNodes))
            }
            capabilityClient.addListener(capabilityListener, WEAR_CAPABILITY)
            awaitClose {
                capabilityClient.removeListener(capabilityListener)
            }
        } catch (e: ApiException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }
    }.catch { _ ->
        emit(emptyList())
    }

    private suspend fun fetchInstalledWearNodes(): List<WearNode> {
        val nodeClient = Wearable.getNodeClient(context)
        val capabilityClient = Wearable.getCapabilityClient(context)
        val capabilities =
            capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE).await()
        val nodes = nodeClient.connectedNodes.await()
        val installedWatchNodes = capabilities[WEAR_CAPABILITY]?.nodes?.map { it.id } ?: setOf()
        return mapNodesToStatus(nodes, installedWatchNodes)
    }

    private fun createCapabilityListener(
        onNodesChanged: (List<Node>, List<String>) -> Unit,
    ): CapabilityClient.OnCapabilityChangedListener {
        return CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            onNodesChanged(
                capabilityInfo.nodes.toList(),
                capabilityInfo.nodes.map { it.id },
            )
        }
    }

    override suspend fun syncDataToWearable(nodeId: String) {
        uploadWearMetadata()
        soundsDao.getAllSoundsOnce()
            .map { it.toDomain() }
            .forEach { sendSoundToWear(nodeId, it) }
    }

    private suspend fun uploadWearMetadata() {
        val dataClient = Wearable.getDataClient(context)
        val metadata = BackupMetadata(
            sounds = soundsDao.getAllSoundsOnce().map { it.toBackup() },
            categories = categoryDao.getAllCategoriesOnce()
        )
        val request = PutDataMapRequest.create(METADATA_PATH).apply {
            dataMap.putString(METADATA_KEY, makeMetadataJson(metadata))
        }
            .asPutDataRequest()
            .setUrgent()

        dataClient.putDataItem(request).await()
    }

    private suspend fun sendSoundToWear(nodeId: String, sound: PlayableSound) {
        val fileUri = resolveWearFileUri(sound) ?: run {
            println("Failed to convert URI to file for sound ${sound.id}")
            return
        }

        val channelClient = Wearable.getChannelClient(context)
        val channel = channelClient.openChannel(nodeId, "$AUDIO_TRANSFER_PREFIX/${sound.id}").await()
        Wearable.getChannelClient(context).sendFile(channel, fileUri)
            .addOnSuccessListener {
                println("File sent successfully: ${fileUri.path}")
            }
            .addOnFailureListener { error ->
                println("Failed to send file: ${error.message}")
            }
    }

    private suspend fun resolveWearFileUri(sound: PlayableSound): Uri? {
        val soundUri = if (sound.uri == Uri.EMPTY) {
            getUriPath(context, sound.resId!!)
        } else {
            sound.uri
        }

        if (soundUri.scheme == ContentResolver.SCHEME_FILE) {
            return soundUri
        }

        val tempFile = getFileFromUri(context, soundUri, sound.id.toString(), sound.fileExtension)
        return tempFile?.let { Uri.fromFile(it) }
    }

    private fun mapNodesToStatus(
        nodes: List<Node>,
        installedWatchNodes: Collection<String>,
    ): List<WearNode> {
        return nodes.mapNotNull {
            val appInstallationStatus = installedWatchNodes.contains(it.id)
            if (appInstallationStatus) {
                WearNode(
                    id = it.id,
                    name = it.displayName,
                    isNearby = it.isNearby,
                )
            } else {
                null
            }
        }
    }
}