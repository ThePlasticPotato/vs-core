package org.valkyrienskies.core.api.world.chunks

import java.util.*

interface ChunkWatchTasks {
    val watchTasks: SortedSet<ChunkWatchTask>
    val unwatchTasks: SortedSet<ChunkUnwatchTask>
}
