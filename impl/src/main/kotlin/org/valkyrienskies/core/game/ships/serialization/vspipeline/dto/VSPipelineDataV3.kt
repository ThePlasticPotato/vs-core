package org.valkyrienskies.core.game.ships.serialization.vspipeline.dto

import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV3

data class VSPipelineDataV3(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV3>
) : VSPipelineData
