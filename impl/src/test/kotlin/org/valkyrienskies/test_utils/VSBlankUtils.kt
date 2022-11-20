package org.valkyrienskies.test_utils

import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.game.ChunkClaimImpl
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.api.ships.properties.ShipId

object VSBlankUtils {

    fun blankShipData(
        name: String = "fake_name",
        shipId: ShipId = 0,
        chunkClaim: ChunkClaim = ChunkClaimImpl(0, 0),
        chunkClaimDimension: DimensionId = "fake_dimension",
        shipCenterInWorldCoordinates: Vector3d = Vector3d(0.0, 0.0, 0.0),
        shipCenterInShipCoordinates: Vector3d = Vector3d(0.0, 0.0, 0.0)
    ) = ShipData.createEmpty(
        name, shipId, chunkClaim, chunkClaimDimension, shipCenterInWorldCoordinates, shipCenterInShipCoordinates
    )
}
