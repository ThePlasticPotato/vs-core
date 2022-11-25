package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.world.ClientShipWorldCore

interface VSCoreClient : VSCore {
    fun newShipWorldClient(): ClientShipWorldCore
}
