package org.valkyrienskies.core.game.ships

import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ShipInternal
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.world.chunks.BlockType
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.datastructures.IBlockPosSet
import org.valkyrienskies.core.util.serialization.DeltaIgnore
import org.valkyrienskies.core.util.serialization.PacketIgnore
import org.valkyrienskies.core.util.toAABBd

open class ShipDataCommon(
    override val id: ShipId,
    override var slug: String,
    override val chunkClaim: ChunkClaim,
    override val chunkClaimDimension: DimensionId,
    @DeltaIgnore
    val physicsData: ShipPhysicsData,
    transform: ShipTransform,
    prevTickShipTransform: ShipTransform = transform,
    worldAABB: AABBdc = transform.createEmptyAABB(),
    override var shipAABB: AABBic?,
    override val activeChunksSet: IShipActiveChunksSet
) : ShipInternal {

    override val velocity: Vector3dc
        get() = physicsData.linearVelocity

    override val omega: Vector3dc
        get() = physicsData.angularVelocity

    @DeltaIgnore
    override var transform: ShipTransform = transform
        set(transform) {
            field = transform
            // Update the [shipAABB]
            worldAABB = shipAABB?.toAABBd(AABBd())?.transform(transform.shipToWorld, AABBd())
                ?: transform.createEmptyAABB()
        }

    @PacketIgnore
    final override var prevTickTransform: ShipTransform = prevTickShipTransform
        private set

    @DeltaIgnore
    final override var worldAABB: AABBdc = worldAABB
        private set

    fun updatePrevTickShipTransform() {
        prevTickTransform = transform
    }

    /**
     * Updates the [IBlockPosSet] and [ShipInertiaData] for this [ShipData]
     */
    override fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        oldBlockType: BlockType,
        newBlockType: BlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        // Sanity check
        require(
            chunkClaim.contains(
                posX shr 4,
                posZ shr 4
            )
        ) { "Block at <$posX, $posY, $posZ> is not in the chunk claim belonging to $this" }

        // Add the chunk to the active chunk set
        activeChunksSet.add(posX shr 4, posZ shr 4)
        // Add the neighbors too (Required for rendering code in MC 1.16, chunks without neighbors won't render)
        // TODO: Make a separate set for keeping track of neighbors
        activeChunksSet.add((posX shr 4) - 1, (posZ shr 4))
        activeChunksSet.add((posX shr 4) + 1, (posZ shr 4))
        activeChunksSet.add((posX shr 4), (posZ shr 4) - 1)
        activeChunksSet.add((posX shr 4), (posZ shr 4) + 1)
    }

    override val shipToWorld: Matrix4dc
        get() = transform.shipToWorld
    override val worldToShip: Matrix4dc
        get() = transform.worldToShip

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShipDataCommon

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
