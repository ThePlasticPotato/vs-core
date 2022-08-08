package org.valkyrienskies.core.game.ships.loading

import org.valkyrienskies.core.game.ships.ShipData

data class ShipLoadInfo(
    val shipsToLoad: Collection<ShipData>,
    val shipsToUnload: Collection<ShipData>,

    )
