package org.valkyrienskies.core.networking

import org.valkyrienskies.core.program.VSCore

/**
 * Passed in by the game and called by [VSCore] to configure an instance of [VSNetworking] to send and receive.
 * In the future, consider having the game directly pass in some sort of interface with sendToClient/onReceiveClient
 * etc.
 */
fun interface VSNetworkingConfigurator {

    fun configure(networking: VSNetworking)
}
