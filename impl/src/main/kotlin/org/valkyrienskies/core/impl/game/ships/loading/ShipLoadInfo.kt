package org.valkyrienskies.core.impl.game.ships.loading

import org.valkyrienskies.core.impl.game.ships.ShipData

data class ShipLoadInfo(
    val shipsToLoad: Collection<ShipData>,
    val shipsToUnload: Collection<ShipData>,
)
