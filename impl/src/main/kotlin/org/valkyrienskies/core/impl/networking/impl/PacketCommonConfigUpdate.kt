package org.valkyrienskies.core.impl.networking.impl

import com.fasterxml.jackson.databind.JsonNode
import org.valkyrienskies.core.impl.networking.simple.SimplePacket

/**
 * Sent by the client to the server to update the server-side config
 */
data class PacketCommonConfigUpdate(val mainClass: Class<*>, val newConfig: JsonNode) : SimplePacket
