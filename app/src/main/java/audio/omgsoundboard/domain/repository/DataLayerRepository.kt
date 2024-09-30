package audio.omgsoundboard.domain.repository

import audio.omgsoundboard.domain.models.WearNode
import kotlinx.coroutines.flow.Flow

interface DataLayerRepository {
    fun getConnectedWearNodesAsFlow(): Flow<List<WearNode>>
    suspend fun syncDataToWearable(nodeId: String)

}