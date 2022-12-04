package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.ShipPhysicsData
import org.valkyrienskies.core.impl.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0
import org.valkyrienskies.core.impl.game.ships.serialization.shiptransform.dto.ShipTransformDataV0
import org.valkyrienskies.core.impl.util.serialization.DeltaIgnore
import org.valkyrienskies.core.impl.util.serialization.PacketIgnore
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.impl.util.toAABBd

/**
 * This class contains an exact replication of the legacy ShipData class so that we can deserialize it and
 * not break backwards compatibility. This class must be deserialized using [VSJacksonUtil.defaultMapper].
 *
 * This is only guaranteed to deserialize correctly. I stripped all the other parts.
 */
class ServerShipDataV0(
    id: ShipId,
    name: String,
    chunkClaim: ChunkClaim,
    chunkClaimDimension: DimensionId,
    physicsData: ShipPhysicsData,
    @PacketIgnore val inertiaData: ShipInertiaDataV0,
    shipTransform: ShipTransformDataV0,
    prevTickShipTransform: ShipTransformDataV0,
    shipAABB: AABBdc,
    shipVoxelAABB: AABBic?,
    shipActiveChunksSet: IShipActiveChunksSet,
    var isStatic: Boolean = false
) : ShipDataCommon(
    id, name, chunkClaim, chunkClaimDimension, physicsData, shipTransform, prevTickShipTransform,
    shipAABB, shipVoxelAABB, shipActiveChunksSet
) {

    val persistentAttachedData = MutableClassToInstanceMap.create<Any>() // TODO a serializable class
}

open class ShipDataCommon(
    val id: ShipId,
    var name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    @DeltaIgnore
    val physicsData: ShipPhysicsData,
    shipTransform: ShipTransformDataV0,
    prevTickShipTransform: ShipTransformDataV0 = shipTransform,
    shipAABB: AABBdc = shipTransform.createEmptyAABB(),
    var shipVoxelAABB: AABBic?,
    val shipActiveChunksSet: IShipActiveChunksSet
) {

    val velocity: Vector3dc
        get() = physicsData.linearVelocity

    val omega: Vector3dc
        get() = physicsData.angularVelocity

    @DeltaIgnore
    var shipTransform: ShipTransformDataV0 = shipTransform
        set(shipTransform) {
            field = shipTransform
            // Update the [shipAABB]
            shipAABB = shipVoxelAABB?.toAABBd(AABBd())?.transform(shipTransform.shipToWorldMatrix, AABBd())
                ?: shipTransform.createEmptyAABB()
        }

    @PacketIgnore
    var prevTickShipTransform: ShipTransformDataV0 = prevTickShipTransform
        private set

    @DeltaIgnore
    var shipAABB: AABBdc = shipAABB
        private set
}
