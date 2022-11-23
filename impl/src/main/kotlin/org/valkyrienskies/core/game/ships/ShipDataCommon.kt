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
import org.valkyrienskies.core.api.ships.properties.VSBlockType
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.datastructures.IBlockPosSet
import org.valkyrienskies.core.util.serialization.DeltaIgnore
import org.valkyrienskies.core.util.serialization.PacketIgnore
import org.valkyrienskies.core.util.toAABBd

open class ShipDataCommon(
    override val id: ShipId,
    var name: String,
    override val chunkClaim: ChunkClaim,
    override val chunkClaimDimension: DimensionId,
    @DeltaIgnore
    val physicsData: ShipPhysicsData,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform = shipTransform,
    shipAABB: AABBdc = shipTransform.createEmptyAABB(),
    override var shipVoxelAABB: AABBic?,
    override val shipActiveChunksSet: IShipActiveChunksSet
) : ShipInternal {

    override val velocity: Vector3dc
        get() = physicsData.linearVelocity

    override val omega: Vector3dc
        get() = physicsData.angularVelocity

    @DeltaIgnore
    override var shipTransform: ShipTransform = shipTransform
        set(shipTransform) {
            field = shipTransform
            // Update the [shipAABB]
            shipAABB = shipVoxelAABB?.toAABBd(AABBd())?.transform(shipTransform.shipToWorld, AABBd())
                ?: shipTransform.createEmptyAABB()
        }

    @PacketIgnore
    final override var prevTickShipTransform: ShipTransform = prevTickShipTransform
        private set

    @DeltaIgnore
    final override var shipAABB: AABBdc = shipAABB
        private set

    fun updatePrevTickShipTransform() {
        prevTickShipTransform = shipTransform
    }

    /**
     * Updates the [IBlockPosSet] and [ShipInertiaData] for this [ShipData]
     */
    override fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
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
        shipActiveChunksSet.add(posX shr 4, posZ shr 4)
        // Add the neighbors too (Required for rendering code in MC 1.16, chunks without neighbors won't render)
        // TODO: Make a separate set for keeping track of neighbors
        shipActiveChunksSet.add((posX shr 4) - 1, (posZ shr 4))
        shipActiveChunksSet.add((posX shr 4) + 1, (posZ shr 4))
        shipActiveChunksSet.add((posX shr 4), (posZ shr 4) - 1)
        shipActiveChunksSet.add((posX shr 4), (posZ shr 4) + 1)
    }

    override val shipToWorld: Matrix4dc
        get() = shipTransform.shipToWorld
    override val worldToShip: Matrix4dc
        get() = shipTransform.worldToShip

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
