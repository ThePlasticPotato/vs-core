package org.valkyrienskies.core.impl.hooks

import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.game.ships.ShipObjectClient
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.impl.util.events.EventEmitter
import org.valkyrienskies.core.impl.util.events.EventEmitterImpl

object VSEvents {

    val shipLoadEvent = EventEmitterImpl<ShipLoadEvent>()
    val airPocketModifyEvent = EventEmitterImpl<AirPocketModifyEvent>()

    data class ShipLoadEvent(val ship: ShipObjectServer) {
        companion object : EventEmitter<ShipLoadEvent> by shipLoadEvent
    }

    val shipLoadEventClient = EventEmitterImpl<ShipLoadEventClient>()

    data class ShipLoadEventClient(val ship: ShipObjectClient) {
        companion object : EventEmitter<ShipLoadEventClient> by shipLoadEventClient
    }

    val tickEndEvent = EventEmitterImpl<TickEndEvent>()

    data class TickEndEvent(val world: ShipObjectServerWorld) {
        companion object : EventEmitter<TickEndEvent> by tickEndEvent
    }

    data class AirPocketModifyEvent(val shipId: ShipId, val airPocketId: Int, val removed: Boolean) {
        companion object : EventEmitter<AirPocketModifyEvent> by airPocketModifyEvent
    }
}
