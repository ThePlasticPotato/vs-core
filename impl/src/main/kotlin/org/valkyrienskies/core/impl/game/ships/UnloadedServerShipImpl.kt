package org.valkyrienskies.core.impl.game.ships

import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.api.ShipInternal
import org.valkyrienskies.core.impl.datastructures.IBlockPosSet
import org.valkyrienskies.core.impl.util.serialization.DeltaIgnore
import org.valkyrienskies.core.impl.util.serialization.PacketIgnore
import org.valkyrienskies.core.impl.util.toAABBd

class AbstractUnloadedShip(private val data: ShipDataCommon) : ShipInternal {

    override val velocity: Vector3dc
        get() = data.physicsData.linearVelocity

    override val omega: Vector3dc
        get() = data.physicsData.angularVelocity
    override val activeChunksSet: IShipActiveChunksSet
        get() = data.activeChunksSet

    override val id: ShipId
        get() = data.id

    override val slug: String?
        get() = data.slug

    @DeltaIgnore
    override var transform: ShipTransform = shipTransform
        set(shipTransform) {
            field = shipTransform
            // Update the [shipAABB]
            worldAABB = shipAABB?.toAABBd(AABBd())?.transform(shipTransform.shipToWorld, AABBd())
                ?: shipTransform.createEmptyAABB()
        }

    @PacketIgnore
    override var prevTickTransform: ShipTransform = data.prevTickTransform
        private set
    override val chunkClaim: ChunkClaim
        get() = data.chunkClaim
    override val chunkClaimDimension: DimensionId
        get() = data.chunkClaimDimension

    @DeltaIgnore
    override var worldAABB: AABBdc = data.worldAABB
        private set
    override val shipAABB: AABBic?
        get() = data.shipAABB

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
        newBlockMass: Double,
        isRunningOnServer: Boolean,
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
