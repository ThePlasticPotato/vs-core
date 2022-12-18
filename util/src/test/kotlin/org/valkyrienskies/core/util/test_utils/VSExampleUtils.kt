package org.valkyrienskies.test_utils

import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ChunkClaimImpl
import org.valkyrienskies.core.impl.game.ships.ShipData

object VSExampleUtils {

    fun exampleShipData1(
        name: String = "ship-example-1",
        shipId: Long = 1,
        chunkClaim: ChunkClaim = ChunkClaimImpl(1, 2),
        chunkClaimDimension: DimensionId = "example-dimension",
        shipCenterInWorldCoordinates: Vector3d = Vector3d(1.0, 2.0, 3.0),
        shipCenterInShipCoordinates: Vector3d = Vector3d(-1.0, -2.0, -3.0)
    ) = ShipData.createEmpty(
        name, shipId, chunkClaim, chunkClaimDimension, shipCenterInWorldCoordinates, shipCenterInShipCoordinates
    )
}
