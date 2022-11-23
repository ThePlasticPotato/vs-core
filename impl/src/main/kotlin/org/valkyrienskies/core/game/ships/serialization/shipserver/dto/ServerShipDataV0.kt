package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Matrix4d
import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.game.ships.ShipInertiaDataImpl
import org.valkyrienskies.core.game.ships.ShipPhysicsData
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

    internal val persistentAttachedData = MutableClassToInstanceMap.create<Any>() // TODO a serializable class
}

data class ShipTransformDataV0(
    val shipPositionInWorldCoordinates: Vector3dc,
    val shipPositionInShipCoordinates: Vector3dc,
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc,
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc,
) {
    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    val shipToWorldMatrix: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    val worldToShipMatrix: Matrix4dc

    init {
        shipToWorldMatrix = Matrix4d()
            .translate(shipPositionInWorldCoordinates)
            .rotate(shipCoordinatesToWorldCoordinatesRotation)
            .scale(shipCoordinatesToWorldCoordinatesScaling)
            .translate(
                -shipPositionInShipCoordinates.x(),
                -shipPositionInShipCoordinates.y(),
                -shipPositionInShipCoordinates.z()
            )
        worldToShipMatrix = shipToWorldMatrix.invert(Matrix4d())
    }

    fun createEmptyAABB(): AABBdc {
        return AABBd(shipPositionInWorldCoordinates, shipPositionInWorldCoordinates)
    }
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
