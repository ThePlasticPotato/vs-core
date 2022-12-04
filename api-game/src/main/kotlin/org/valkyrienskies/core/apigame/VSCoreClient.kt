package org.valkyrienskies.core.apigame

import org.valkyrienskies.core.apigame.world.ClientShipWorldCore

interface VSCoreClient : VSCore {
    fun newShipWorldClient(): ClientShipWorldCore
}
