package org.valkyrienskies.core.game.ships.serialization.shipserver

import com.google.common.collect.MutableClassToInstanceMap
import dagger.Reusable
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.game.ships.ShipPhysicsData
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV3
import javax.inject.Inject

internal interface ServerShipDataConverter {
    fun convertToDto(model: ShipData): ServerShipDataV3

    fun convertToModel(dto: ServerShipDataV3): ShipData
}

@Reusable
internal class ServerShipDataConverterImpl @Inject constructor() : ServerShipDataConverter {
    override fun convertToDto(model: ShipData) = ServerShipDataV3(
        id = model.id,
        name = model.name,
        chunkClaim = model.chunkClaim,
        chunkClaimDimension = model.chunkClaimDimension,
        velocity = model.physicsData.linearVelocity,
        omega = model.physicsData.angularVelocity,
        inertiaData = model.inertiaData,
        transform = model.shipTransform,
        prevTickTransform = model.prevTickShipTransform,
        worldAABB = model.shipAABB,
        shipAABB = model.shipVoxelAABB,
        activeChunks = model.shipActiveChunksSet,
        isStatic = model.isStatic,
        persistentAttachedData = model.persistentAttachedData
    )

    override fun convertToModel(dto: ServerShipDataV3): ShipData = ShipData(
        id = dto.id,
        name = dto.name,
        chunkClaim = dto.chunkClaim,
        chunkClaimDimension = dto.chunkClaimDimension,
        physicsData = ShipPhysicsData(dto.velocity, dto.omega),
        inertiaData = dto.inertiaData,
        shipTransform = dto.transform,
        prevTickShipTransform = dto.prevTickTransform,
        shipAABB = dto.worldAABB,
        shipVoxelAABB = dto.shipAABB,
        shipActiveChunksSet = dto.activeChunks,
        isStatic = dto.isStatic,
        persistentAttachedData = MutableClassToInstanceMap.create(dto.persistentAttachedData)
    )
}
