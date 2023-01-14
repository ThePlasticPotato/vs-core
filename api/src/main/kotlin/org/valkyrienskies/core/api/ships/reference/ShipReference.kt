package org.valkyrienskies.core.api.ships.reference

import org.valkyrienskies.core.api.reference.VSRef
import org.valkyrienskies.core.api.ships.Ship

interface ShipReference<out S : Ship> : VSRef<S>