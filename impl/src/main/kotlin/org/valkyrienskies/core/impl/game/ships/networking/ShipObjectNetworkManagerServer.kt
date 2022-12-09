package org.valkyrienskies.core.impl.game.ships.networking

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import dagger.Lazy
import io.netty.buffer.Unpooled
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.impl.api.ServerShipInternal
import org.valkyrienskies.core.impl.chunk_tracking.ChunkTrackingInfo
import org.valkyrienskies.core.impl.game.ships.ShipObjectServer
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.impl.networking.NetworkChannel
import org.valkyrienskies.core.impl.networking.Packets
import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule.UDP
import org.valkyrienskies.core.impl.networking.impl.PacketShipDataCreate
import org.valkyrienskies.core.impl.networking.impl.PacketShipRemove
import org.valkyrienskies.core.impl.networking.simple.SimplePacketNetworking
import org.valkyrienskies.core.impl.util.getValue
import org.valkyrienskies.core.impl.util.logger
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.impl.util.toImmutableSet
import javax.inject.Inject

class ShipObjectNetworkManagerServer @Inject constructor(
    _parent: Lazy<ShipObjectServerWorld>,
    @TCP private val tcp: NetworkChannel,
    @UDP private val udp: NetworkChannel,
    private val spNetwork: SimplePacketNetworking,
    private val packets: Packets
) {

    private val parent by _parent

    private lateinit var players: Iterable<IPlayer>
    private lateinit var tracker: ChunkTrackingInfo

    fun tick(players: Iterable<IPlayer>, trackingInfo: ChunkTrackingInfo) {
        this.players = players
        this.tracker = trackingInfo

        updateShipData()
        updateTracking()
        updateTrackedShips()
        // Transforms are sent in [VSNetworkPipelineStage]
    }

    private fun IPlayer.getTrackedShips(): Iterable<ServerShipInternal> {
        return tracker.getShipsPlayerIsWatching(this)
    }

    private fun updateTrackedShips() {
        val builder = ImmutableMap.builder<IPlayer, ImmutableSet<ServerShipInternal>>()
        tracker.playersToShipsWatchingMap.forEach { (player, ships) ->
            builder.put(player, ships.keys.toImmutableSet())
        }
        playersToTrackedShips = builder.build()
    }

    /**
     * Used by VSNetworkPipeline as a threadsafe way to access the transforms to send
     */
    @Volatile
    var playersToTrackedShips: ImmutableMap<IPlayer, ImmutableSet<ServerShipInternal>> =
        ImmutableMap.of()

    /**
     * Send create and destroy packets for ships that players have started/stopped watching
     */
    private fun updateTracking() {
        tracker.playersToShipsNewlyWatchingMap
            .forEach { (player, ships) -> startTracking(player, ships) }

        for (player in players) {
            val shipsNoLongerWatching = tracker.playersToShipsNoLongerWatchingMap[player] ?: emptySet()
            endTracking(player, tracker.shipsToUnload + shipsNoLongerWatching)
        }
    }

    private fun endTracking(player: IPlayer, shipsToNotTrack: Iterable<ServerShipInternal>) {
        val shipIds = shipsToNotTrack.map { it.id }
        if (shipIds.isEmpty()) return
        logger.debug("${player.uuid} unwatched ships $shipIds")
        spNetwork.sendToClient(PacketShipRemove(shipIds), player)
    }

    private fun startTracking(player: IPlayer, shipsToTrack: Iterable<ServerShipInternal>) {
        val ships = shipsToTrack.map { it.asShipDataCommon() }
        if (ships.isEmpty()) return
        logger.debug("${player.uuid} watched ships: ${ships.map { it.id }}")
        spNetwork.sendToClient(PacketShipDataCreate(ships), player)
    }

    /**
     * Send ServerShipInternal deltas to players
     */
    private fun updateShipData() {
        for (player in players) {
            val buf = Unpooled.buffer()
            val newlyWatching = tracker.playersToShipsNewlyWatchingMap[player] ?: emptySet()
            val trackedShips: List<ShipObjectServer> = player.getTrackedShips()
                .filter { tracked -> !newlyWatching.contains(tracked) }
                .mapNotNull { parent.getShipObject(it) }

            if (trackedShips.isEmpty())
                continue

            trackedShips.forEach { ship ->
                buf.writeLong(ship.shipData.id)
                val json = VSJacksonUtil.deltaMapper.valueToTree<JsonNode>(ship.shipData)
                ship.shipDataChannel.encode(json, buf)
            }

            packets.TCP_SHIP_DATA_DELTA.sendToClient(buf, player)
        }
    }

    init {
        tcp.serverIsReady()
        udp.serverIsReady()
    }

    companion object {
        private val logger by logger()
    }
}
