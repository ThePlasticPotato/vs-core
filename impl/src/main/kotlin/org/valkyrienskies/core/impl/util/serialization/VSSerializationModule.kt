package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.databind.module.SimpleModule
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.impl.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.impl.game.ChunkClaimImpl
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl

class VSSerializationModule : SimpleModule() {
    init {
        addAbstractTypeMapping<IShipActiveChunksSet, ShipActiveChunksSet>()
        addAbstractTypeMapping<ChunkClaim, ChunkClaimImpl>()
        addAbstractTypeMapping<ShipTransform, ShipTransformImpl>()
    }
}
