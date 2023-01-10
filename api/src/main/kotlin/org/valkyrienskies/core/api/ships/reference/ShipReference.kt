package org.valkyrienskies.core.api.ships.reference

import org.valkyrienskies.core.api.reference.VSReference
import org.valkyrienskies.core.api.ships.Ship
import java.util.*

interface ShipReference<out S : Ship> : VSReference<S>