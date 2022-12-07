package org.valkyrienskies.core.impl.api

import org.valkyrienskies.core.apigame.ships.LoadedServerShipCore

interface LoadedServerShipInternal : LoadedServerShipCore, LoadedShipInternal,
    ServerShipInternal
