package org.valkyrienskies.core.api.ships

import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface Ship {

    val id: ShipId

    val transform: ShipTransform
    val prevTickTransform: ShipTransform

    val chunkClaim: ChunkClaim
    val chunkClaimDimension: DimensionId
    val worldAABB: AABBdc
    val shipAABB: AABBic?
    val velocity: Vector3dc
    val omega: Vector3dc

    val activeChunksSet: IShipActiveChunksSet

    val shipToWorld: Matrix4dc get() = transform.shipToWorld
    val worldToShip: Matrix4dc get() = transform.worldToShip

    @Deprecated("renamed", ReplaceWith("prevTickTransform"))
    val prevTickShipTransform: ShipTransform get() = prevTickTransform

    @Deprecated("renamed", ReplaceWith("transform"))
    val shipTransform: ShipTransform get() = transform

    @Deprecated("renamed", ReplaceWith("shipAABB"))
    val shipVoxelAABB: AABBic? get() = shipAABB

    @Deprecated("renamed", ReplaceWith("activeChunksSet"))
    val shipActiveChunksSet: IShipActiveChunksSet get() = activeChunksSet
}
