package org.valkyrienskies.core.apigame.world

import org.joml.Vector3ic
import org.valkyrienskies.core.api.bodies.shape.VoxelUpdate
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.world.LevelYRange
import org.valkyrienskies.core.api.world.ServerShipWorld
import org.valkyrienskies.core.apigame.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTasks
import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate
import org.valkyrienskies.core.api.world.properties.DimensionId

interface ServerShipWorldCore : ShipWorldCore, ServerShipWorld {

    var players: Set<IPlayer>

    fun addTerrainUpdates(dimensionId: DimensionId, terrainUpdates: List<VoxelUpdate>)

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
     * @return True non-null if [vsConstraint] was created successfully.
     */
    fun createNewConstraint(vsConstraint: VSConstraint): VSConstraintId?

    /**
     * @return True iff the constraint with id [constraintId] was successfully updated.
     */
    fun updateConstraint(constraintId: VSConstraintId, updatedVSConstraint: VSConstraint): Boolean

    /**
     * @return True if a constraint with [constraintId] was removed successfully.
     */
    fun removeConstraint(constraintId: VSConstraintId): Boolean

    /**
     * Adds a newly loaded dimension with [dimensionId]. [yRange] specifies the range of valid y values for this dimension.
     * In older versions of Minecraft, this should be `[0, 255]`
     */
    fun addDimension(dimensionId: DimensionId, yRange: LevelYRange)
    fun removeDimension(dimensionId: DimensionId)
    fun onDisconnect(player: IPlayer)

    val dimensionToGroundBodyIdImmutable: Map<DimensionId, ShipId>
}
