package org.valkyrienskies.core.impl.game.ships.serialization

fun interface DtoUpdater<OLD : Any, NEW : Any> {
    fun update(data: OLD): NEW
}
