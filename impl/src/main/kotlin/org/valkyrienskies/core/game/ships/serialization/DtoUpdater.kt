package org.valkyrienskies.core.game.ships.serialization

interface DtoUpdater<OLD : Any, NEW : Any> {
    fun update(data: OLD): NEW
}
