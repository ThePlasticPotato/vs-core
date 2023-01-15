package org.valkyrienskies.core.api.world

data class LevelYRange(val minY: Int, val maxY: Int) {
    val center: Int
        get() = (minY + maxY + 1) / 2

    val size: Int
        get() = (maxY - minY + 1)
}
