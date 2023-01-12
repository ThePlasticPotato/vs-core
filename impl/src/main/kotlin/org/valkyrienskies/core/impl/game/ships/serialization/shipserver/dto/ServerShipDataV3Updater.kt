package org.valkyrienskies.core.impl.game.ships.serialization.shipserver.dto

import dagger.Reusable
import org.valkyrienskies.core.api.ships.Wing
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.game.ships.serialization.DtoUpdater
import javax.inject.Inject

interface ServerShipDataV3Updater : DtoUpdater<ServerShipDataV3, ServerShipDataV4>

@Reusable
class ServerShipDataV3UpdaterImpl @Inject constructor() : ServerShipDataV3Updater {
    override fun update(data: ServerShipDataV3): ServerShipDataV4 {
        return ServerShipDataV4(
            id = data.id,
            name = data.name,
            chunkClaim = data.chunkClaim,
            chunkClaimDimension = data.chunkClaimDimension,
            velocity = data.velocity,
            omega = data.omega,
            inertiaData = data.inertiaData,
            transform = data.transform,
            prevTickTransform = data.prevTickTransform,
            worldAABB = data.worldAABB,
            shipAABB = data.shipAABB,
            activeChunks = data.activeChunks,
            isStatic = data.isStatic,
            persistentAttachedData = data.persistentAttachedData,
            wingsMap = BlockPos2ObjectOpenHashMap()
        )
    }
}
