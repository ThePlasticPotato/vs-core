package org.valkyrienskies.core.impl.game.ships

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Matrix4dc
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipUser
import org.valkyrienskies.core.impl.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.impl.datastructures.DynamicBlockPosSetAABB
import org.valkyrienskies.core.impl.datastructures.IBlockPosSetAABB
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.core.impl.util.serialization.PacketIgnore

/**
 * The purpose of [ShipData] is to keep track of the state of a ship; it does not manage the behavior of a ship.
 *
 * See [ShipObject] to find the code that defines ship behavior (movement, player interactions, etc)
 */
class ShipData(
    id: ShipId,
    slug: String?,
    chunkClaim: ChunkClaim,
    chunkClaimDimension: DimensionId,
    physicsData: ShipPhysicsData,
    @PacketIgnore override val inertiaData: ShipInertiaDataImpl,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform,
    shipAABB: AABBdc,
    shipVoxelAABB: AABBic?,
    shipActiveChunksSet: IShipActiveChunksSet,
    var isStatic: Boolean = false,
    val persistentAttachedData: MutableClassToInstanceMap<Any> = MutableClassToInstanceMap.create(),
) : ShipDataCommon(
    id, slug, chunkClaim, chunkClaimDimension, physicsData, shipTransform, prevTickShipTransform,
    shipAABB, shipVoxelAABB, shipActiveChunksSet
), ServerShipInternal {
    /**
     * The set of chunks that must be loaded before this ship is fully loaded.
     *
     * We need to keep track of this regardless of whether a ShipObject for this exists, so we keep track of it here.
     *
     * Also, this is transient, so we don't want to save it
     */
    @JsonIgnore
    private val missingLoadedChunks: IShipActiveChunksSet = ShipActiveChunksSet.create()

    /**
     * Generates the [shipAABB] in O(1) time. However, this object is too large for us to persistently store it,
     * so we make it transient.
     *
     * This can also be used to quickly iterate over every block in this ship.
     */
    @JsonIgnore
    private val shipAABBGenerator: IBlockPosSetAABB = DynamicBlockPosSetAABB()

    override val shipToWorld: Matrix4dc
        get() = transform.shipToWorld
    override val worldToShip: Matrix4dc
        get() = transform.worldToShip

    init {
        shipActiveChunksSet.forEach { chunkX: Int, chunkZ: Int ->
            missingLoadedChunks.add(chunkX, chunkZ)
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
        oldBlockType: BlockType,
        newBlockType: BlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        super.onSetBlock(posX, posY, posZ, oldBlockType, newBlockType, oldBlockMass, newBlockMass)

        inertiaData.onSetBlockUseSphereMOI(posX, posY, posZ, oldBlockMass, newBlockMass)

        // Update [shipVoxelAABB]
        updateShipAABBGenerator(posX, posY, posZ, newBlockType != BlockTypeImpl.AIR)
    }

    /**
     * Update the [shipAABB] to when a block is added/removed.
     */
    override fun updateShipAABBGenerator(posX: Int, posY: Int, posZ: Int, set: Boolean) {
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
        shipAABB = rawVoxelAABB
    }

    override fun onLoadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ)) {
            missingLoadedChunks.remove(chunkX, chunkZ)
        }
    }

    override fun onUnloadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ) && activeChunksSet.contains(chunkX, chunkZ)) {
            missingLoadedChunks.add(chunkX, chunkZ)
        }
    }

    override fun areVoxelsFullyLoaded(): Boolean {
        // We are fully loaded if we have 0 missing chunks
        return missingLoadedChunks.size == 0
    }

    override fun asShipDataCommon(): ShipDataCommon = this

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
        fun createEmpty(
            slug: String,
            shipId: ShipId,
            chunkClaim: ChunkClaim,
            chunkClaimDimension: DimensionId,
            shipCenterInWorldCoordinates: Vector3dc,
            shipCenterInShipCoordinates: Vector3dc,
            scaling: Double = 1.0,
            isStatic: Boolean = false
        ): ShipData {
            val shipTransform = ShipTransformImpl.create(
                shipCenterInWorldCoordinates,
                shipCenterInShipCoordinates,
                Quaterniond().fromAxisAngleDeg(0.0, 1.0, 0.0, 0.0),
                Vector3d(scaling)
            )

            return ShipData(
                id = shipId,
                slug = slug,
                chunkClaim = chunkClaim,
                chunkClaimDimension = chunkClaimDimension,
                physicsData = ShipPhysicsData.createEmpty(),
                inertiaData = ShipInertiaDataImpl.newEmptyShipInertiaData(),
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
