package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.world.ShipWorld

interface VSCoreClient : VSCore {
    fun newShipWorldClient(): ShipWorld
}