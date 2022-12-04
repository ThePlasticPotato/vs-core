package org.valkyrienskies.core.impl.game.ships

import org.valkyrienskies.core.impl.api.LoadedShipInternal
import org.valkyrienskies.core.impl.api.ShipInternal
import org.valkyrienskies.core.impl.networking.delta.JsonDiffDeltaAlgorithm
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil

/**
 * A [ShipObject] is essentially a [ShipData] that has been loaded.
 *
 * Its just is to interact with the player. This includes stuff like rendering, colliding with entities, and adding
 * a rigid body to the physics engine.
 */
open class ShipObject(
    shipData: ShipDataCommon
) : LoadedShipInternal, ShipInternal by shipData {
    @Suppress("CanBePrimaryConstructorProperty") // don't want to refer to open val in constructor
    open val shipData: ShipDataCommon = shipData

    companion object {
        @JvmStatic
        val jsonDiffDeltaAlgorithm = JsonDiffDeltaAlgorithm(VSJacksonUtil.deltaMapper)
    }
}
