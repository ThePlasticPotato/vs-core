package org.valkyrienskies.core.impl.game.ships

import com.google.common.collect.MutableClassToInstanceMap
import org.valkyrienskies.core.api.ships.WingManager
import org.valkyrienskies.core.impl.api.LoadedServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipUser
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.api.Ticked
import org.valkyrienskies.core.impl.networking.delta.DeltaEncodedChannelServerTCP
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil

class ShipObjectServer(
    override val shipData: ShipData
) : ShipObject(shipData), LoadedServerShipInternal, ServerShipInternal by shipData {

    val shipDataChannel = DeltaEncodedChannelServerTCP(
        jsonDiffDeltaAlgorithm,
        VSJacksonUtil.deltaMapper.valueToTree(shipData)
    )

    // runtime attached data only server-side, cus syncing to clients would be pain
    val attachedData = MutableClassToInstanceMap.create<Any>()
    val forceInducers = mutableListOf<ShipForcesInducer>()
    val toBeTicked = mutableListOf<Ticked>()

    init {
        for (data in shipData.legacyPersistentAttachedData) {
            applyAttachmentInterfaces(data.key, data.value)
        }
        val wingManager = WingManagerImpl()
        wingManager.createWingGroup()
        setAttachment(WingManager::class.java, wingManager)
    }

    override fun <T> setAttachment(clazz: Class<T>, value: T?) {
        if (value == null) {
            attachedData.remove(clazz)
        } else {
            attachedData[clazz] = value
        }

        applyAttachmentInterfaces(clazz, value)
    }

    override fun <T> getAttachment(clazz: Class<T>): T? =
        attachedData.getInstance(clazz) ?: shipData.getAttachment(clazz)

    override fun <T> saveAttachment(clazz: Class<T>, value: T?) {
        applyAttachmentInterfaces(clazz, value)

        shipData.saveAttachment(clazz, value)
    }

    private fun applyAttachmentInterfaces(clazz: Class<*>, value: Any?) {
        if (value == null) {
            forceInducers.removeIf { clazz.isAssignableFrom(it::class.java) }
            toBeTicked.removeIf { clazz.isAssignableFrom(it::class.java) }
        } else {
            if (value is ShipForcesInducer) {
                forceInducers.add(value)
            }
            if (value is ServerShipUser) {
                value.ship = this
            }
            if (value is Ticked) {
                toBeTicked.add(value)
            }
        }
    }

}
