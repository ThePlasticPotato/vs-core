package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import dagger.Reusable
import org.valkyrienskies.core.impl.game.ships.serialization.DtoUpdater
import javax.inject.Inject

interface ServerShipDataV2Updater : DtoUpdater<ServerShipDataV2, ServerShipDataV3>

@Reusable
class ServerShipDataV2UpdaterImpl @Inject constructor() : ServerShipDataV2Updater {
    override fun update(data: ServerShipDataV2): ServerShipDataV3 {
        return ServerShipDataV3(
            id = data.id,
            name = data.name,
            chunkClaim = data.chunkClaim,
            chunkClaimDimension = data.chunkClaimDimension,
            velocity = data.physicsData.linearVelocity,
            omega = data.physicsData.angularVelocity,
            inertiaData = data.inertiaData,
            transform = data.shipTransform,
            prevTickTransform = data.prevTickShipTransform,
            worldAABB = data.shipAABB,
            shipAABB = data.shipVoxelAABB,
            activeChunks = data.shipActiveChunksSet,
            isStatic = data.isStatic,
            persistentAttachedData = data.persistentAttachedData
        )
    }
}
