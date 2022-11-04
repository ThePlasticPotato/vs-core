package org.valkyrienskies.test_utils

import org.joml.Vector3d
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.ships.ShipData

object VSExampleUtils {

    fun exampleShipData1(
        name: String = "ship-example-1",
        shipId: Long = 1,
        chunkClaim: ChunkClaim = ChunkClaim(1, 2),
        chunkClaimDimension: DimensionId = "example-dimension",
        shipCenterInWorldCoordinates: Vector3d = Vector3d(1.0, 2.0, 3.0),
        shipCenterInShipCoordinates: Vector3d = Vector3d(-1.0, -2.0, -3.0)
    ) = ShipData.createEmpty(
        name, shipId, chunkClaim, chunkClaimDimension, shipCenterInWorldCoordinates, shipCenterInShipCoordinates
    )
}
