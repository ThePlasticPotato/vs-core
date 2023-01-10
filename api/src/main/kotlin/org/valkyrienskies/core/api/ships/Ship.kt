package org.valkyrienskies.core.api.ships

import org.joml.Matrix4dc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.bodies.VSBody
import org.valkyrienskies.core.api.world.properties.DimensionId

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface Ship : VSBody {

    override val id: ShipId

    override val transform: ShipTransform
    override val prevTickTransform: ShipTransform

    val chunkClaim: ChunkClaim
    val chunkClaimDimension: DimensionId
    val shipAABB: AABBic?

    val activeChunksSet: IShipActiveChunksSet

    val shipToWorld: Matrix4dc get() = transform.toWorld
    val worldToShip: Matrix4dc get() = transform.toModel

    @Deprecated("renamed", ReplaceWith("prevTickTransform"))
    val prevTickShipTransform: ShipTransform get() = prevTickTransform

    @Deprecated("renamed", ReplaceWith("transform"))
    val shipTransform: ShipTransform get() = transform

    @Deprecated("renamed", ReplaceWith("shipAABB"))
    val shipVoxelAABB: AABBic? get() = shipAABB

    @Deprecated("renamed", ReplaceWith("activeChunksSet"))
    val shipActiveChunksSet: IShipActiveChunksSet get() = activeChunksSet
}
