package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto.ServerShipDataV1

@JsonTypeName("v1")
data class VSPipelineDataV1(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV1>
) : VSPipelineData
