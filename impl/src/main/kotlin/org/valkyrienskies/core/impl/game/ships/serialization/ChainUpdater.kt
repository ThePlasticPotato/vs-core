package org.valkyrienskies.core.impl.game.ships.serialization

interface ChainUpdater<T> {
    fun updateToLatest(value: Any): T
}
