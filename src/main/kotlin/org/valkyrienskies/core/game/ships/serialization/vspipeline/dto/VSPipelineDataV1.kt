package org.valkyrienskies.core.game.ships.serialization.vspipeline.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV1

@JsonTypeName("v1")
internal data class VSPipelineDataV1(
    val chunkAllocator: ChunkAllocator,
    val ships: List<ServerShipDataV1>
) : VSPipelineData
