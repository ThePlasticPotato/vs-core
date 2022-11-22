package org.valkyrienskies.core.game.ships

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Matrix4dc
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ServerShip
import org.valkyrienskies.core.api.ServerShipUser
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.datastructures.BlockPosSetAABBGenerator
import org.valkyrienskies.core.datastructures.IBlockPosSetAABB
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.util.serialization.PacketIgnore

/**
 * The purpose of [ShipData] is to keep track of the state of a ship; it does not manage the behavior of a ship.
 *
 * See [ShipObject] to find the code that defines ship behavior (movement, player interactions, etc)
 */
class ShipData(
    id: ShipId,
    name: String,
    chunkClaim: ChunkClaim,
    chunkClaimDimension: DimensionId,
    physicsData: ShipPhysicsData,
    @PacketIgnore val inertiaData: ShipInertiaData,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform,
    shipAABB: AABBdc,
    shipVoxelAABB: AABBic?,
    shipActiveChunksSet: IShipActiveChunksSet,
    var isStatic: Boolean = false
) : ShipDataCommon(
    id, name, chunkClaim, chunkClaimDimension, physicsData, shipTransform, prevTickShipTransform,
    shipAABB, shipVoxelAABB, shipActiveChunksSet
),
    ServerShip {
    /**
     * The set of chunks that must be loaded before this ship is fully loaded.
     *
     * We need to keep track of this regardless of whether a ShipObject for this exists, so we keep track of it here.
     *
     * Also, this is transient, so we don't want to save it
     */
    @JsonIgnore
    private val missingLoadedChunks: IShipActiveChunksSet = ShipActiveChunksSet.create()

    internal val persistentAttachedData = MutableClassToInstanceMap.create<Any>() // TODO a serializable class

    /**
     * Generates the [shipVoxelAABB] in O(1) time. However, this object is too large for us to persistently store it,
     * so we make it transient.
     *
     * This can also be used to quickly iterate over every block in this ship.
     */
    @JsonIgnore
    private val shipAABBGenerator: IBlockPosSetAABB =
        BlockPosSetAABBGenerator(chunkClaim)

    override val shipToWorld: Matrix4dc
        get() = shipTransform.shipToWorldMatrix
    override val worldToShip: Matrix4dc
        get() = shipTransform.worldToShipMatrix

    init {
        shipActiveChunksSet.iterateChunkPos { chunkX: Int, chunkZ: Int ->
            missingLoadedChunks.addChunkPos(chunkX, chunkZ)
        }

        for (attachment in this.persistentAttachedData) {
            if (
                ServerShipUser::class.java.isAssignableFrom(attachment.key) &&
                attachment.value != null &&
                (attachment.value as ServerShipUser).ship == null
            ) {
                (attachment.value as ServerShipUser).ship = this
            }
        }
    }

    override fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        super.onSetBlock(posX, posY, posZ, oldBlockType, newBlockType, oldBlockMass, newBlockMass)

        // Update [inertiaData]
        inertiaData.onSetBlock(posX, posY, posZ, oldBlockMass, newBlockMass)

        // Update [shipVoxelAABB]
        updateShipAABBGenerator(posX, posY, posZ, newBlockType != VSBlockType.AIR)
    }

    /**
     * Update the [shipVoxelAABB] to when a block is added/removed.
     */
    fun updateShipAABBGenerator(posX: Int, posY: Int, posZ: Int, set: Boolean) {
        if (set) {
            shipAABBGenerator.add(posX, posY, posZ)
        } else {
            shipAABBGenerator.remove(posX, posY, posZ)
        }
        val rawVoxelAABB = shipAABBGenerator.makeAABB()
        if (rawVoxelAABB != null) {
            // Increment the maximums by 1
            rawVoxelAABB.maxX += 1
            rawVoxelAABB.maxY += 1
            rawVoxelAABB.maxZ += 1
        }
        shipVoxelAABB = rawVoxelAABB
    }

    fun onLoadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ)) {
            missingLoadedChunks.removeChunkPos(chunkX, chunkZ)
        }
    }

    fun onUnloadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ) && shipActiveChunksSet.containsChunkPos(chunkX, chunkZ)) {
            missingLoadedChunks.addChunkPos(chunkX, chunkZ)
        }
    }

    fun areVoxelsFullyLoaded(): Boolean {
        // We are fully loaded if we have 0 missing chunks
        return missingLoadedChunks.getTotalChunks() == 0
    }

    override fun <T> saveAttachment(clazz: Class<T>, value: T?) {
        if (value is ServerShipUser && value.ship == null) {
            value.ship = this
        }

        if (value == null)
            persistentAttachedData.remove(clazz)
        else
            persistentAttachedData[clazz] = value
    }

    override fun <T> getAttachment(clazz: Class<T>): T? = persistentAttachedData.getInstance(clazz)

    companion object {
        /**
         * Creates a new [ShipData] from the given name and coordinates. The resulting [ShipData] is completely empty,
         * so it must be filled with blocks by other code.
         */
        internal fun createEmpty(
            name: String,
            shipId: ShipId,
            chunkClaim: ChunkClaim,
            chunkClaimDimension: DimensionId,
            shipCenterInWorldCoordinates: Vector3dc,
            shipCenterInShipCoordinates: Vector3dc,
            scaling: Double = 1.0,
            isStatic: Boolean = false
        ): ShipData {
            val shipTransform = ShipTransform.createFromCoordinatesAndRotationAndScaling(
                shipCenterInWorldCoordinates,
                shipCenterInShipCoordinates,
                Quaterniond().fromAxisAngleDeg(0.0, 1.0, 0.0, 0.0),
                Vector3d(scaling)
            )

            return ShipData(
                id = shipId,
                name = name,
                chunkClaim = chunkClaim,
                chunkClaimDimension = chunkClaimDimension,
                physicsData = ShipPhysicsData.createEmpty(),
                inertiaData = ShipInertiaData.newEmptyShipInertiaData(),
                shipTransform = shipTransform,
                prevTickShipTransform = shipTransform,
                shipAABB = shipTransform.createEmptyAABB(),
                shipVoxelAABB = null,
                shipActiveChunksSet = ShipActiveChunksSet.create(),
                isStatic = isStatic
            )
        }
    }
}
