package org.valkyrienskies.core.util.serialization

import com.fasterxml.jackson.databind.module.SimpleModule
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.datastructures.IBlockPosSet
import org.valkyrienskies.core.datastructures.IBlockPosSetAABB
import org.valkyrienskies.core.datastructures.SmallBlockPosSet
import org.valkyrienskies.core.datastructures.SmallBlockPosSetAABB
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.game.ChunkClaimImpl
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.game.ships.ShipTransformImpl

class VSSerializationModule : SimpleModule() {
    init {
        addAbstractTypeMapping<IBlockPosSet, SmallBlockPosSet>()
        addAbstractTypeMapping<IBlockPosSetAABB, SmallBlockPosSetAABB>()
        addAbstractTypeMapping<IShipActiveChunksSet, ShipActiveChunksSet>()
        addAbstractTypeMapping<ChunkClaim, ChunkClaimImpl>()
        addAbstractTypeMapping<ShipTransform, ShipTransformImpl>()
    }
}
