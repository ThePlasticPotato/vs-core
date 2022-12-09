package org.valkyrienskies.core.apigame

import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.apigame.hooks.CoreHooks
import org.valkyrienskies.core.apigame.world.VSPipeline
import org.valkyrienskies.core.apigame.world.chunks.BlockTypes
import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate

interface VSCore {

    val hooks: CoreHooks

    val blockTypes: BlockTypes

    fun newEmptyVoxelShapeUpdate(chunkX: Int, chunkY: Int, chunkZ: Int, overwrite: Boolean): TerrainUpdate

    /**
     * Creates a new terrain update that deletes the specified chunk
     */
    fun newDeleteTerrainUpdate(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate

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

    fun newChunkClaim(claimX: Int, claimZ: Int): ChunkClaim
    fun newChunkClaimFromChunkPos(chunkX: Int, chunkZ: Int): ChunkClaim

    @Deprecated("Surely we can do better than this")
    var clientUsesUDP: Boolean
}
