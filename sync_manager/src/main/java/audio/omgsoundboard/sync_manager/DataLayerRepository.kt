package audio.omgsoundboard.sync_manager

import audio.omgsoundboard.core.domain.models.WearNode
import kotlinx.coroutines.flow.Flow

interface DataLayerRepository {
    fun getConnectedWearNodesAsFlow(): Flow<List<WearNode>>
    suspend fun syncDataToWearable(nodeId: String)
}