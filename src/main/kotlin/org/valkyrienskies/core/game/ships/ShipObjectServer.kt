package org.valkyrienskies.core.game.ships

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Matrix4dc
import org.valkyrienskies.core.api.Ship
import org.valkyrienskies.core.chunk_tracking.IShipChunkTracker
import org.valkyrienskies.core.chunk_tracking.ShipChunkTracker
import org.valkyrienskies.core.networking.delta.DeltaEncodedChannelServerTCP
import org.valkyrienskies.core.util.serialization.VSJacksonUtil


class ShipObjectServer(
    override val shipData: ShipData
) : ShipObject(shipData), Ship {

    override val shipToWorld: Matrix4dc
        get() = shipData.shipToWorld
    override val worldToShip: Matrix4dc
        get() = shipData.worldToShip

    internal val shipDataChannel = DeltaEncodedChannelServerTCP(
        jsonDiffDeltaAlgorithm,
        VSJacksonUtil.deltaMapper.valueToTree(shipData)
    )

    // runtime attached data only server-side, cus syncing to clients would be pain
    private val attachedData = MutableClassToInstanceMap.create<Any>()

    override fun <T> setAttachment(clazz: Class<T>, value: T?) {
        if (value == null)
            attachedData.remove(clazz)
        else
            attachedData[clazz] = value
    }

    override fun <T> getAttachment(clazz: Class<T>): T? =
        attachedData[clazz] as T? ?: shipData.getAttachment(clazz) as T?

    override fun <T> saveAttachment(clazz: Class<T>, value: T?) {
        shipData.saveAttachment(clazz, value)
    }

    companion object {
        private const val DEFAULT_CHUNK_WATCH_DISTANCE = 128.0
        private const val DEFAULT_CHUNK_UNWATCH_DISTANCE = 192.0
    }
}
