package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.ships.*

internal data class ServerShipDataV1(
    val id: ShipId,
    val name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    val physicsData: ShipPhysicsData,
    val inertiaData: ShipInertiaDataImpl,
    val shipTransform: ShipTransform,
    val prevTickShipTransform: ShipTransform,
    val shipAABB: AABBdc,
    val shipVoxelAABB: AABBic?,
    val shipActiveChunksSet: IShipActiveChunksSet,
    val isStatic: Boolean,
    val persistentAttachedData: MutableClassToInstanceMap<Any>
)
