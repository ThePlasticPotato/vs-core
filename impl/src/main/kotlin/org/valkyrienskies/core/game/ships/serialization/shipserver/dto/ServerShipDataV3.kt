package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.game.ships.ShipInertiaDataImpl
import org.valkyrienskies.core.api.ships.properties.ShipTransform

data class ServerShipDataV3(
    val id: ShipId,
    val name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    val inertiaData: ShipInertiaDataImpl,
    val transform: ShipTransform,
    val prevTickTransform: ShipTransform,
    val worldAABB: AABBdc,
    val shipAABB: AABBic?,
    val activeChunks: IShipActiveChunksSet,
    val isStatic: Boolean,
    val persistentAttachedData: Map<Class<*>, Any>
)
