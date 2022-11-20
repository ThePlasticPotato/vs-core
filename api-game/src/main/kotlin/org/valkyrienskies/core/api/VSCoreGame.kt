package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.world.VSPipeline
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate

interface VSCoreGame {

    /**
     * Creates a new dense terrain update builder. A dense terrain update will
     * update every single block in the chunk, and by default contains only air.
     */
    fun newDenseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder

    /**
     * Creates a new sparse terrain update builder. A sparse terrain update will
     * only update blocks that are added to it.
     */
    fun newSparseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder

    /**
     * Deserializes a pipeline from the legacy serialization system that
     * serialized the ship data and chunk allocator separately
     */
    fun newPipelineLegacyData(queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray): VSPipeline

    /**
     * Creates an empty pipeline
     */
    fun newPipeline(): VSPipeline

    /**
     * Deserializes a pipeline from data produced by serializing it using [serializePipeline]
     */
    fun newPipeline(data: ByteArray): VSPipeline

    /**
     * Serializes a pipeline to be deserialized by `newPipeline(data)`
     */
    fun serializePipeline(pipeline: VSPipeline): ByteArray
}
