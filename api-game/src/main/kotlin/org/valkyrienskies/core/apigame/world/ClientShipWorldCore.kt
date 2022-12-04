package org.valkyrienskies.core.apigame.world

import org.valkyrienskies.core.apigame.world.ClientShipWorld
import java.net.SocketAddress

// not impressed at all with this interface, just exists because
// it's too much of a hassle to refactor everything right now
// just how many tick() methods do we need?!
interface ClientShipWorldCore : ShipWorldCore, ClientShipWorld {

    fun tickNetworking(server: SocketAddress)

    fun postTick()

    fun updateRenderTransforms(partialTicks: Double)

    fun destroyWorld()
}
