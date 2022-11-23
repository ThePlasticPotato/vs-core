package org.valkyrienskies.core.game.ships.serialization

interface ChainUpdater<T> {
    fun updateToLatest(value: Any): T
}
