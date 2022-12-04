package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import dagger.Reusable
import org.valkyrienskies.core.impl.game.ships.serialization.DtoUpdater
import javax.inject.Inject


interface ServerShipDataV0Updater : DtoUpdater<ServerShipDataV0, ServerShipDataV1>

@Reusable
class ServerShipDataV0UpdaterImpl @Inject constructor() : ServerShipDataV0Updater {
    override fun update(data: ServerShipDataV0) = ServerShipDataV1(
        id = data.id,
        name = data.name,
        chunkClaim = data.chunkClaim,
        chunkClaimDimension = data.chunkClaimDimension,
        physicsData = data.physicsData,
        inertiaData = data.inertiaData,
        shipTransform = data.shipTransform,
        prevTickShipTransform = data.shipTransform,
        shipAABB = data.shipAABB,
        shipVoxelAABB = data.shipVoxelAABB,
        shipActiveChunksSet = data.shipActiveChunksSet,
        isStatic = data.isStatic,
        persistentAttachedData = data.persistentAttachedData
    )
}
