package org.valkyrienskies.core.api.ships

import org.joml.Matrix4dc
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.ships.ShipId

/**
 * Abstraction of a ship, there are many types such as offline ships
 *  or loaded ships so this is the generic interface for all ships.
 */
interface Ship {

    val id: ShipId

    val shipTransform: ShipTransform
    val prevTickShipTransform: ShipTransform

    val chunkClaim: ChunkClaim
    val chunkClaimDimension: DimensionId
    val shipAABB: AABBdc
    val shipVoxelAABB: AABBic?
    val velocity: Vector3dc
    val omega: Vector3dc

    val shipActiveChunksSet: IShipActiveChunksSet

    val shipToWorld: Matrix4dc get() = shipTransform.shipToWorldMatrix
    val worldToShip: Matrix4dc get() = shipTransform.worldToShipMatrix
}
