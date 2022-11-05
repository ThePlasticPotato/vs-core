package org.valkyrienskies.core.game.ships.serialization

internal interface DtoUpdater<OLD : Any, NEW : Any> {
    fun update(data: OLD): NEW
}
