package org.valkyrienskies.core.hooks

import org.valkyrienskies.core.game.ships.ShipObjectServer
import org.valkyrienskies.core.util.events.EventEmitter
import org.valkyrienskies.core.util.events.EventEmitterImpl

object VSCoreEvents {

    internal val shipLoadEvent = EventEmitterImpl<ShipLoadEvent>()

    data class ShipLoadEvent(val ship: ShipObjectServer) {
        companion object : EventEmitter<ShipLoadEvent> by shipLoadEvent
    }
}
