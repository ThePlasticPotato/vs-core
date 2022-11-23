package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import dagger.Reusable
import org.mapstruct.Mapper
import org.valkyrienskies.core.game.ships.ShipTransformImpl
import org.valkyrienskies.core.game.ships.serialization.DtoUpdater
import org.valkyrienskies.core.game.ships.serialization.VSMapStructConfig
import javax.inject.Inject


@Mapper(config = VSMapStructConfig::class)
interface ShipTransformConverter {
    fun convertToModel(data: ShipTransformDataV0): ShipTransformImpl

    fun convertToDto(model: ShipTransformImpl): ShipTransformDataV0
}

interface ServerShipDataV0Updater : DtoUpdater<ServerShipDataV0, ServerShipDataV1>

@Reusable
class ServerShipDataV0UpdaterImpl @Inject constructor(
    private val transformConverter: ShipTransformConverterImpl
) : ServerShipDataV0Updater {
    override fun update(data: ServerShipDataV0) = ServerShipDataV1(
        id = data.id,
        name = data.name,
        chunkClaim = data.chunkClaim,
        chunkClaimDimension = data.chunkClaimDimension,
        physicsData = data.physicsData,
        inertiaData = data.inertiaData,
        shipTransform = transformConverter.convertToModel(data.shipTransform),
        prevTickShipTransform = transformConverter.convertToModel(data.shipTransform),
        shipAABB = data.shipAABB,
        shipVoxelAABB = data.shipVoxelAABB,
        shipActiveChunksSet = data.shipActiveChunksSet,
        isStatic = data.isStatic,
        persistentAttachedData = data.persistentAttachedData
    )
}
