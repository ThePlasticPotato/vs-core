package org.valkyrienskies.core.game.ships.serialization.shipserver.dto

import dagger.Reusable
import org.valkyrienskies.core.game.ships.serialization.DtoUpdater
import javax.inject.Inject

interface ServerShipDataV1Updater : DtoUpdater<ServerShipDataV1, ServerShipDataV2>

@Reusable
class ServerShipDataV1UpdaterImpl @Inject constructor() : ServerShipDataV1Updater {
    override fun update(data: ServerShipDataV1): ServerShipDataV2 {
        return ServerShipDataV2(
            id = data.id,
            name = data.name,
            chunkClaim = data.chunkClaim,
            chunkClaimDimension = data.chunkClaimDimension,
            physicsData = data.physicsData,
            inertiaData = data.inertiaData,
            shipTransform = data.shipTransform,
            prevTickShipTransform = data.prevTickShipTransform,
            shipAABB = data.shipAABB,
            shipVoxelAABB = data.shipVoxelAABB,
            shipActiveChunksSet = data.shipActiveChunksSet,
            isStatic = data.isStatic,
            persistentAttachedData = data.persistentAttachedData
        )
    }
}

