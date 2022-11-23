package org.valkyrienskies.core.api.world

import org.joml.Vector3ic
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.VSBlockType
import org.valkyrienskies.core.api.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTasks
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ServerShipWorldGame : ShipWorld {

    /**
     * Add the update to [shipToVoxelUpdates].
     */
    fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        dimensionId: DimensionId,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    )

    fun addTerrainUpdates(dimensionId: DimensionId, terrainUpdates: List<TerrainUpdate>)

    /**
     * If the chunk at [chunkX], [chunkZ] is a ship chunk, then this returns the [IPlayer]s that are watching that ship chunk.
     *
     * If the chunk at [chunkX], [chunkZ] is not a ship chunk, then this returns nothing.
     */
    fun getIPlayersWatchingShipChunk(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): Iterator<IPlayer>

    /**
     * Determines which ship chunks should be watched/unwatched by the players.
     *
     * It only returns the tasks, it is up to the caller to execute the tasks; however they do not have to execute all of them.
     * It is up to the caller to decide which tasks to execute, and which ones to skip.
     */
    fun getChunkWatchTasks(): ChunkWatchTasks
    fun setExecutedChunkWatchTasks(watchTasks: Iterable<ChunkWatchTask>, unwatchTasks: Iterable<ChunkUnwatchTask>)

    /**
     * Creates a new [ShipData] centered at the block at [blockPosInWorldCoordinates].
     *
     * If [createShipObjectImmediately] is true then a [ShipObject] will be created immediately.
     */
    fun createNewShipAtBlock(
        blockPosInWorldCoordinates: Vector3ic, createShipObjectImmediately: Boolean, scaling: Double = 1.0,
        dimensionId: DimensionId
    ): ServerShip

    /**
     * Adds a newly loaded dimension with [dimensionId]. [yRange] specifies the range of valid y values for this dimension.
     * In older versions of Minecraft, this should be `[0, 255]`
     */
    fun addDimension(dimensionId: DimensionId, yRange: IntRange)
    fun removeDimension(dimensionId: DimensionId)
    fun onDisconnect(player: IPlayer)
}
