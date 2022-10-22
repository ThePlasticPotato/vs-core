package org.valkyrienskies.core.hooks

import org.valkyrienskies.core.game.ships.ShipObjectClient
import org.valkyrienskies.core.game.ships.ShipObjectServer
import org.valkyrienskies.core.util.events.EventEmitter
import org.valkyrienskies.core.util.events.EventEmitterImpl

object VSEvents {

    internal val shipLoadEvent = EventEmitterImpl<ShipLoadEvent>()

    data class ShipLoadEvent(val ship: ShipObjectServer) {
        companion object : EventEmitter<ShipLoadEvent> by shipLoadEvent
    }

    internal val shipLoadEventClient = EventEmitterImpl<ShipLoadEventClient>()

    data class ShipLoadEventClient(val ship: ShipObjectClient) {
        companion object : EventEmitter<ShipLoadEventClient> by shipLoadEventClient
    }

    internal val tickEndEvent = EventEmitterImpl<TickEndEvent>()

    object TickEndEvent : EventEmitter<TickEndEvent> by tickEndEvent
}
