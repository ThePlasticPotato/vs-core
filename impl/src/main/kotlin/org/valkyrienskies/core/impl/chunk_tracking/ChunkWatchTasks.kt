package org.valkyrienskies.core.impl.chunk_tracking

import org.valkyrienskies.core.apigame.world.chunks.ChunkUnwatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTask
import org.valkyrienskies.core.apigame.world.chunks.ChunkWatchTasks
import java.util.*

fun ChunkWatchTasks(watchTasks: SortedSet<ChunkWatchTask>, unwatchTasks: SortedSet<ChunkUnwatchTask>) =
    ChunkWatchTasksImpl(watchTasks, unwatchTasks)

data class ChunkWatchTasksImpl(
    override val watchTasks: SortedSet<ChunkWatchTask>,
    override val unwatchTasks: SortedSet<ChunkUnwatchTask>
) : ChunkWatchTasks
