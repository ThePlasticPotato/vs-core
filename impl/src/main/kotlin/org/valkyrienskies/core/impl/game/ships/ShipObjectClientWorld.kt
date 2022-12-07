package org.valkyrienskies.core.impl.game.ships

import dagger.Subcomponent
import org.valkyrienskies.core.api.ships.QueryableShipData
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.ships.MutableQueryableShipData
import org.valkyrienskies.core.apigame.world.ClientShipWorldCore
import org.valkyrienskies.core.impl.game.ChunkAllocatorProvider
import org.valkyrienskies.core.impl.game.ships.modules.ClientShipWorldModule
import org.valkyrienskies.core.impl.game.ships.networking.ShipObjectNetworkManagerClient
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.core.impl.hooks.VSEvents.ShipLoadEventClient
import org.valkyrienskies.core.impl.util.WorldScoped
import java.net.SocketAddress
import javax.inject.Inject

@WorldScoped
@Subcomponent(modules = [ClientShipWorldModule.Declarations::class])
interface ShipObjectClientWorldComponent {
    fun newWorld(): ShipObjectClientWorld

    @Subcomponent.Factory
    interface Factory {
        fun newShipObjectClientWorldComponent(): ShipObjectClientWorldComponent
    }
}

@WorldScoped
class ShipObjectClientWorld @Inject constructor(
    networkManagerFactory: ShipObjectNetworkManagerClient.Factory,
    chunkAllocators: ChunkAllocatorProvider
) : ClientShipWorldCore, ShipObjectWorld<ShipObjectClient>(chunkAllocators) {
    override val allShips: QueryableShipData<ShipObjectClient> get() = loadedShips

    private val _loadedShips: MutableQueryableShipData<ShipObjectClient> = QueryableShipDataImpl()

    override val loadedShips: QueryableShipData<ShipObjectClient>
        get() = _loadedShips

    private val networkManager: ShipObjectNetworkManagerClient = networkManagerFactory.make(this)

    init {
        networkManager.registerPacketListeners()
    }

    fun addShip(ship: ShipDataCommon) {
        val shipObject = ShipObjectClient(ship)
        _loadedShips.addShipData(shipObject)
        VSEvents.shipLoadEventClient.emit(ShipLoadEventClient(shipObject))
    }

    fun removeShip(shipId: ShipId) {
        _loadedShips.removeShipData(shipId)
    }

    override fun tickNetworking(server: SocketAddress) {
        networkManager.tick(server)
    }

    override fun postTick() {
        super.preTick()

        loadedShips.forEach { it.tickUpdateShipTransform() }
    }

    override fun updateRenderTransforms(partialTicks: Double) {
        loadedShips.forEach { it.updateRenderShipTransform(partialTicks) }
    }

    override fun destroyWorld() {
        networkManager.onDestroy()
    }
}
