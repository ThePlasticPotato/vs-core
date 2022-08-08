package org.valkyrienskies.core.game.ships.networking

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import dagger.Lazy
import io.netty.buffer.Unpooled
import org.valkyrienskies.core.chunk_tracking.ChunkTrackingInfo
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData
import org.valkyrienskies.core.game.ships.ShipObjectServerWorld
import org.valkyrienskies.core.networking.Packets
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.networking.impl.PacketShipDataCreate
import org.valkyrienskies.core.networking.impl.PacketShipRemove
import org.valkyrienskies.core.networking.simple.sendToClient
import org.valkyrienskies.core.util.getValue
import org.valkyrienskies.core.util.logger
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.util.toImmutableSet
import javax.inject.Inject

internal class ShipObjectNetworkManagerServer @Inject constructor(
    _parent: Lazy<ShipObjectServerWorld>,
    private val network: VSNetworking,
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

    private fun IPlayer.getTrackedShips(): Iterable<ShipData> {
        return tracker.getShipsPlayerIsWatching(this)
    }

    private fun updateTrackedShips() {
        val builder = ImmutableMap.builder<IPlayer, ImmutableSet<ShipData>>()
        tracker.playersToShipsWatchingMap.forEach { (player, ships) ->
            builder.put(player, ships.keys.toImmutableSet())
        }
        playersToTrackedShips = builder.build()
    }

    /**
     * Used by VSNetworkPipeline as a threadsafe way to access the transforms to send
     */
    var playersToTrackedShips: ImmutableMap<IPlayer, ImmutableSet<ShipData>> = ImmutableMap.of()

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

    private fun endTracking(player: IPlayer, shipsToNotTrack: Iterable<ShipData>) {
        val shipIds = shipsToNotTrack.map { it.id }
        if (shipIds.isEmpty()) return
        logger.debug("${player.uuid} unwatched ships $shipIds")
        PacketShipRemove(shipIds).sendToClient(player)
    }

    private fun startTracking(player: IPlayer, shipsToTrack: Iterable<ShipData>) {
        val ships = shipsToTrack.toList()
        if (ships.isEmpty()) return
        logger.debug("${player.uuid} watched ships: ${ships.map { it.id }}")
        PacketShipDataCreate(ships).sendToClient(player)
    }

    /**
     * Send ShipData deltas to players
     */
    private fun updateShipData() {
        for (player in players) {
            val buf = Unpooled.buffer()
            val newlyWatching = tracker.playersToShipsNewlyWatchingMap[player] ?: emptySet()
            val trackedShips = player.getTrackedShips()
                .filter { tracked -> !newlyWatching.contains(tracked) }
                .map { parent.getShipObject(it)!! }

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
        network.TCP.serverIsReady()
        network.UDP.serverIsReady()
    }

    companion object {
        private val logger by logger()
    }
}
