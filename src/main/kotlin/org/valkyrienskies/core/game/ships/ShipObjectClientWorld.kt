package org.valkyrienskies.core.game.ships

import dagger.Component
import org.valkyrienskies.core.game.ships.networking.ShipObjectNetworkManagerClient
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule
import javax.inject.Inject
import javax.inject.Singleton

class ShipObjectClientWorld @Inject constructor(
    networkManagerFactory: ShipObjectNetworkManagerClient.Factory
) : ShipObjectWorld<ShipObjectClient>() {

    @Singleton
    @Component(modules = [NetworkingModule::class])
    interface Factory {
        fun make(): ShipObjectClientWorld
    }

    override val queryableShipData: QueryableShipData<ShipObjectClient> get() = loadedShips

    private val _loadedShips: MutableQueryableShipData<ShipObjectClient> = QueryableShipDataImpl()

    override val loadedShips: QueryableShipData<ShipObjectClient>
        get() = _loadedShips

    val networkManager: ShipObjectNetworkManagerClient = networkManagerFactory.make(this)

    init {
        networkManager.registerPacketListeners()
    }

    fun addShip(ship: ShipDataCommon) {
        _loadedShips.addShipData(ShipObjectClient(ship))
    }

    fun removeShip(shipId: ShipId) {
        _loadedShips.removeShipData(shipId)
    }

    public override fun preTick() {
        super.preTick()

        shipObjects.forEach { (_, shipObjectClient) ->
            shipObjectClient.tickUpdateShipTransform()
        }
    }

    override fun destroyWorld() {
        networkManager.onDestroy()
    }
}
