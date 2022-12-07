package org.valkyrienskies.core.impl.networking.delta

import io.netty.buffer.ByteBuf

class DeltaEncodedChannelClientTCP<T>(
    private val algorithm: DeltaAlgorithm<T>,
    initialSnapshot: T? = null
) {

    var latestSnapshot: T? = initialSnapshot

    fun decode(data: ByteBuf): T {
        return algorithm.apply(latestSnapshot!!, data).also { latestSnapshot = it }
    }
}
