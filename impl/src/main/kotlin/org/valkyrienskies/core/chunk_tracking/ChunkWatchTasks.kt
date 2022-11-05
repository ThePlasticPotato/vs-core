package org.valkyrienskies.core.chunk_tracking

import java.util.SortedSet

data class ChunkWatchTasks(
    val watchTasks: SortedSet<ChunkWatchTask>,
    val unwatchTasks: SortedSet<ChunkUnwatchTask>
)
