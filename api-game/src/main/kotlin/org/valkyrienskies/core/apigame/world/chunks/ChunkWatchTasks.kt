package org.valkyrienskies.core.apigame.world.chunks

import java.util.*

interface ChunkWatchTasks {
    val watchTasks: SortedSet<ChunkWatchTask>
    val unwatchTasks: SortedSet<ChunkUnwatchTask>

    @JvmSynthetic
    operator fun component1() = watchTasks

    @JvmSynthetic
    operator fun component2() = unwatchTasks
}
