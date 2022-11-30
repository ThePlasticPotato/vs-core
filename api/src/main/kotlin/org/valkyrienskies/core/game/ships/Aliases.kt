package org.valkyrienskies.core.game.ships

import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.ServerShip

@Deprecated("renamed", ReplaceWith("ServerShip", "org.valkyrienskies.core.api.ships.ServerShip"))
typealias ShipData = ServerShip

@Deprecated("renamed", ReplaceWith("LoadedShip", "org.valkyrienskies.core.api.ships.LoadedShip"))
typealias ShipObject = LoadedShip

@Deprecated("renamed", ReplaceWith("ClientShip", "org.valkyrienskies.core.api.ships.ClientShip"))
typealias ShipObjectClient = ClientShip

@Deprecated("renamed", ReplaceWith("LoadedServerShip", "org.valkyrienskies.core.api.ships.LoadedServerShip"))
typealias ShipObjectServer = LoadedServerShip
