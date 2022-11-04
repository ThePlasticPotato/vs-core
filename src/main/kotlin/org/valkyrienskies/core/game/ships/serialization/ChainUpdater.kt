package org.valkyrienskies.core.game.ships.serialization

internal interface ChainUpdater<T> {
    fun updateToLatest(value: Any): T
}
