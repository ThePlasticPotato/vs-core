package org.valkyrienskies.core.chunk_tracking

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.LongSet
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData

/**
 * A class containing the result of the chunk tracking. **This object is only valid for the tick it was produced in!**
 * Many of the maps/sets will be reused for efficiency's sake.
 */
internal data class ChunkTrackingInfo(
    val playersToShipsWatchingMap: Map<IPlayer, Map<ShipData, LongSet>>,
    val shipsToPlayersWatchingMap: Long2ObjectMap<MutableSet<IPlayer>>,
    val playersToShipsNewlyWatchingMap: Map<IPlayer, Set<ShipData>>,
    val playersToShipsNoLongerWatchingMap: Map<IPlayer, Set<ShipData>>,
    val shipsToLoad: Set<ShipData>,
    val shipsToUnload: Set<ShipData>,
) {
    fun getShipsPlayerIsWatching(player: IPlayer): Iterable<ShipData> {
        return (playersToShipsWatchingMap[player] ?: emptyMap()).keys
    }
}
