package org.valkyrienskies.core.api.ships.reference

import org.valkyrienskies.core.api.ships.Ship
import java.util.*

interface ShipReference<out S : Ship> {

    fun get(): S?

    fun getOptional(): Optional<out S> = Optional.ofNullable(get())

    fun orElseThrow(): S

}