package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.core.impl.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0
import org.valkyrienskies.core.impl.game.ships.serialization.shiptransform.dto.ShipTransformDataV0

data class ServerShipDataV1(
    val id: ShipId,
    val name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    val physicsData: ShipPhysicsData,
    val inertiaData: ShipInertiaDataV0,
    val shipTransform: ShipTransformDataV0,
    val prevTickShipTransform: ShipTransformDataV0,
    val shipAABB: AABBdc,
    val shipVoxelAABB: AABBic?,
    val shipActiveChunksSet: IShipActiveChunksSet,
    val isStatic: Boolean,
    val persistentAttachedData: MutableClassToInstanceMap<Any>
)
