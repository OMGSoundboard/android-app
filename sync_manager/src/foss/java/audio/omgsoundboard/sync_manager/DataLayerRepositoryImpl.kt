package audio.omgsoundboard.sync_manager

import audio.omgsoundboard.core.domain.models.WearNode
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataLayerRepositoryImpl @Inject constructor() : DataLayerRepository {

    override fun getConnectedWearNodesAsFlow(): Flow<List<WearNode>> = flow {
        // FOSS version always emits empty list since no wear devices are supported
        emit(emptyList())
    }

    override suspend fun syncDataToWearable(nodeId: String) {
        // No-op in FOSS version
    }
}