package org.valkyrienskies.core.api

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.game.ships.ShipTransform

interface ClientShipCore : ClientShip, LoadedShipCore
