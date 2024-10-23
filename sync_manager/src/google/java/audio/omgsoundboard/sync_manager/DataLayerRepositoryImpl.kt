package audio.omgsoundboard.sync_manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import audio.omgsoundboard.core.data.local.daos.CategoryDao
import audio.omgsoundboard.core.data.local.daos.SoundsDao
import audio.omgsoundboard.core.domain.models.BackupMetadata
import audio.omgsoundboard.core.domain.models.WearNode
import audio.omgsoundboard.core.domain.models.toBackup
import audio.omgsoundboard.core.domain.models.toDomain
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
            val nodeClient = Wearable.getNodeClient(context)
            val capabilityClient = Wearable.getCapabilityClient(context)

            val capabilities =
                capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE).await()
            val nodes = nodeClient.connectedNodes.await()
            val installedWatchNodes = capabilities[WEAR_CAPABILITY]?.nodes?.map { it.id } ?: setOf()
            trySend(mapNodesToStatus(nodes, installedWatchNodes))

            val capabilityListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
                val updatedNodes = capabilityInfo.nodes.toList()
                val updatedInstalledWatchNodes = capabilityInfo.nodes.map { it.id }
                trySend(mapNodesToStatus(updatedNodes, updatedInstalledWatchNodes))
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

    override suspend fun syncDataToWearable(nodeId: String) {
        val dataClient = Wearable.getDataClient(context)
        val channelClient = Wearable.getChannelClient(context)

        val categories = categoryDao.getAllCategoriesOnce()
        val soundsEntities = soundsDao.getAllSoundsOnce()
        val soundsBackup = soundsEntities.map { it.toBackup() }

        val sounds = soundsEntities.map { it.toDomain() }

        val metadata = BackupMetadata(
            sounds = soundsBackup,
            categories = categories
        )
        val metadataJson = makeMetadataJson(metadata)

        val request = PutDataMapRequest.create(METADATA_PATH).apply {
            dataMap.putString(METADATA_KEY, metadataJson)
        }
            .asPutDataRequest()
            .setUrgent()

        dataClient.putDataItem(request).await()


        sounds.forEach {
            val soundUri = if (it.uri == Uri.EMPTY) {
                getUriPath(context, it.resId!!)
            } else {
                it.uri
            }

            val fileUri = if (soundUri.scheme == ContentResolver.SCHEME_FILE) {
                soundUri
            } else {
                val tempFile = getFileFromUri(context, soundUri, it.id.toString())
                if (tempFile != null) Uri.fromFile(tempFile) else null
            }

            if (fileUri != null) {
                val channel = channelClient.openChannel(nodeId, "/mp3_transfer/${it.id}").await()
                Wearable.getChannelClient(context).sendFile(channel, fileUri).addOnSuccessListener {
                    println("File sent successfully: ${fileUri.path}")
                }.addOnFailureListener { e ->
                    println("Failed to send file: ${e.message}")
                }
            } else {
                println("Failed to convert URI to file for sound ${it.id}")
            }
        }
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