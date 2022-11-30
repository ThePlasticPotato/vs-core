package org.valkyrienskies.core.networking

import org.valkyrienskies.core.program.VSCoreInternal

/**
 * Passed in by the game and called by [VSCoreInternal] to configure an instance of [NetworkChannel] to send and receive.
 * In the future, consider having the game directly pass in some sort of interface with sendToClient/onReceiveClient
 * etc.
 */
fun interface VSNetworkingConfigurator {

    fun configure(channel: NetworkChannel)
}
