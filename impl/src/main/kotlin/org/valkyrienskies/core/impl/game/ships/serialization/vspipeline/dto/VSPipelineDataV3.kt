package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV3

data class VSPipelineDataV3(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV3>
) : VSPipelineData
