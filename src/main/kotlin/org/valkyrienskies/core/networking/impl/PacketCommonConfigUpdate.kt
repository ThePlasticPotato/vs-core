package org.valkyrienskies.core.networking.impl

import com.fasterxml.jackson.databind.node.ObjectNode
import org.valkyrienskies.core.config.framework.scopes.single.SingleScopedConfig
import org.valkyrienskies.core.networking.simple.SimplePacket

/**
 * Sent by the client to the server to update the server-side config
 */
data class PacketCommonConfigUpdate(
    val mainClass: Class<out SingleScopedConfig>,
    val newConfig: ObjectNode
) : SimplePacket
