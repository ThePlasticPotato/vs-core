package org.valkyrienskies.core.game.ships

import dagger.Subcomponent
import org.valkyrienskies.core.game.ships.networking.ShipObjectNetworkManagerClient
import org.valkyrienskies.core.util.WorldScoped
import javax.inject.Inject

@WorldScoped
@Subcomponent
interface ShipObjectClientWorldComponent {
    fun newWorld(): ShipObjectClientWorld

    @Subcomponent.Factory
    interface Factory {
        fun newShipObjectClientWorldComponent(): ShipObjectClientWorldComponent
    }
}

@WorldScoped
class ShipObjectClientWorld @Inject constructor(
    networkManagerFactory: ShipObjectNetworkManagerClient.Factory
) : ShipObjectWorld<ShipObjectClient>() {
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

    public override fun postTick() {
        super.preTick()

        shipObjects.forEach { (_, shipObjectClient) ->
            shipObjectClient.tickUpdateShipTransform()
        }
    }

    override fun destroyWorld() {
        networkManager.onDestroy()
    }
}
