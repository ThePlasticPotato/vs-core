package org.valkyrienskies.core.game.ships.serialization.vspipeline.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV2

@JsonTypeName("v2")
data class VSPipelineDataV2(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV2>
) : VSPipelineData
