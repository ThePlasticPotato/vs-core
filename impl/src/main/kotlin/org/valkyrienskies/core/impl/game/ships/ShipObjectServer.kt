package org.valkyrienskies.core.impl.game.ships

import com.google.common.collect.MutableClassToInstanceMap
import org.joml.Vector3ic
import org.valkyrienskies.core.api.ships.WingManager
import org.valkyrienskies.core.apigame.ShipTeleportData
import org.valkyrienskies.core.impl.api.LoadedServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.api.ServerShipUser
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.api.Ticked
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.networking.delta.DeltaEncodedChannelServerTCP
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil

class ShipObjectServer(
    override val shipData: ShipData,
) : ShipObject(shipData), LoadedServerShipInternal, ServerShipInternal by shipData {
    // @Volatile
    //
    // Technically, this should be volatile to prevent a race condition where this is updated, the physics pipeline
    // consumes this, and the physics pipeline produces another update before the transform is updated. In this case
    // the teleport would fail.
    //
    // However, in practice the physics pipeline will never do this because it takes significantly longer to produce the
    // next physics frame than we take to update our transform reference.
    override var shipTeleportId: Int = 0
        private set

    val shipDataChannel = DeltaEncodedChannelServerTCP(
        jsonDiffDeltaAlgorithm,
        VSJacksonUtil.deltaMapper.valueToTree(shipData)
    )

    // runtime attached data only server-side, cus syncing to clients would be pain
    val attachedData = MutableClassToInstanceMap.create<Any>()
    val forceInducers = mutableListOf<ShipForcesInducer>()
    val toBeTicked = mutableListOf<Ticked>()

    init {
        for (data in shipData.persistentAttachedData) {
            applyAttachmentInterfaces(data.key, data.value)
        }
        val wingManager = WingManagerImpl()
        wingManager.createWingGroup()
        val connManager = ConnectivityForestImpl(ConnGraph(), HashMap<Vector3ic, BlockPosVertex>(), mutableSetOf(), mutableSetOf(), mutableSetOf())
        val airManager = AirPocketForestImpl(ConnGraph(), HashMap<Vector3ic, BlockPosVertex>(), HashMap<Vector3ic, BlockPosVertex>(), HashMap<Vector3ic, BlockPosVertex>())
        setAttachment(WingManager::class.java, wingManager)
        setAttachment(ConnectivityForest::class.java, connManager)
        setAttachment(AirPocketForest::class.java, airManager)
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

    override fun teleportShip(teleportData: ShipTeleportData) {
        // Increment shipTeleportId to avoid race conditions (only increment to avoid reusing old values)
        shipTeleportId++
        shipData.transform = teleportData.createNewShipTransform(transform)
        shipData.physicsData.linearVelocity = teleportData.newVel
        shipData.physicsData.angularVelocity = teleportData.newOmega
    }
}
