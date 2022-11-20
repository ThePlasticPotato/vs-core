package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.game.ships.*
import org.valkyrienskies.core.util.serialization.DeltaIgnore
import org.valkyrienskies.core.util.serialization.PacketIgnore
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.util.toAABBd

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
    @PacketIgnore val inertiaData: ShipInertiaDataImpl,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform,
    shipAABB: AABBdc,
    shipVoxelAABB: AABBic?,
    shipActiveChunksSet: IShipActiveChunksSet,
    var isStatic: Boolean = false
) : ShipDataCommon(
    id, name, chunkClaim, chunkClaimDimension, physicsData, shipTransform, prevTickShipTransform,
    shipAABB, shipVoxelAABB, shipActiveChunksSet
) {

    internal val persistentAttachedData = MutableClassToInstanceMap.create<Any>() // TODO a serializable class
}

open class ShipDataCommon(
    val id: ShipId,
    var name: String,
    val chunkClaim: ChunkClaim,
    val chunkClaimDimension: DimensionId,
    @DeltaIgnore
    val physicsData: ShipPhysicsData,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform = shipTransform,
    shipAABB: AABBdc = shipTransform.createEmptyAABB(),
    var shipVoxelAABB: AABBic?,
    val shipActiveChunksSet: IShipActiveChunksSet
) {

    val velocity: Vector3dc
        get() = physicsData.linearVelocity

    val omega: Vector3dc
        get() = physicsData.angularVelocity

    @DeltaIgnore
    var shipTransform: ShipTransform = shipTransform
        set(shipTransform) {
            field = shipTransform
            // Update the [shipAABB]
            shipAABB = shipVoxelAABB?.toAABBd(AABBd())?.transform(shipTransform.shipToWorldMatrix, AABBd())
                ?: shipTransform.createEmptyAABB()
        }

    @PacketIgnore
    var prevTickShipTransform: ShipTransform = prevTickShipTransform
        private set

    @DeltaIgnore
    var shipAABB: AABBdc = shipAABB
        private set
}
