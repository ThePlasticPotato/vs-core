package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.Wing
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0
import org.valkyrienskies.core.impl.game.ships.serialization.shiptransform.dto.ShipTransformDataV0

data class ServerShipDataV4(
    val id: ShipId,
    val name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    val velocity: Vector3dc,
    val omega: Vector3dc,
    val inertiaData: ShipInertiaDataV0,
    val transform: ShipTransformDataV0,
    val prevTickTransform: ShipTransformDataV0,
    val worldAABB: AABBdc,
    val shipAABB: AABBic?,
    val activeChunks: IShipActiveChunksSet,
    val isStatic: Boolean,
    val persistentAttachedData: MutableClassToInstanceMap<Any>,
    val wingsMap: BlockPos2ObjectOpenHashMap<Wing>
)
