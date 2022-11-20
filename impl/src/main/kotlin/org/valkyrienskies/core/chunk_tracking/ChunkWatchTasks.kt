package org.valkyrienskies.core.chunk_tracking

import org.valkyrienskies.core.api.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.api.world.chunks.ChunkWatchTasks
import java.util.SortedSet

fun ChunkWatchTasks(watchTasks: SortedSet<ChunkWatchTask>, unwatchTasks: SortedSet<ChunkUnwatchTask>) =
    ChunkWatchTasksImpl(watchTasks, unwatchTasks)

data class ChunkWatchTasksImpl(
    override val watchTasks: SortedSet<ChunkWatchTask>,
    override val unwatchTasks: SortedSet<ChunkUnwatchTask>
) : ChunkWatchTasks
