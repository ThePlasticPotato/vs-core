package org.valkyrienskies.core.ecs.components

import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet

data class RenderedChunks(
    // TODO make this the active chunks set
    val activeChunksSet: IShipActiveChunksSet
)
