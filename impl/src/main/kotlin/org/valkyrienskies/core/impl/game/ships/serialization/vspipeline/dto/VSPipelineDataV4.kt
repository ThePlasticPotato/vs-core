package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV4

data class VSPipelineDataV4(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV4>
) : VSPipelineData
