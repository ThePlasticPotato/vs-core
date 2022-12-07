package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV2

@JsonTypeName("v2")
data class VSPipelineDataV2(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV2>
) : VSPipelineData
