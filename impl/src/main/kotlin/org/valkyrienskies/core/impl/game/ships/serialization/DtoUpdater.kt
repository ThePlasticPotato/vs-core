package org.valkyrienskies.core.impl.game.ships.serialization

interface DtoUpdater<OLD : Any, NEW : Any> {
    fun update(data: OLD): NEW
}
