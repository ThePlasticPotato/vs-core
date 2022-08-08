package org.valkyrienskies.core.chunk_tracking

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.valkyrienskies.core.game.IPlayer
import org.valkyrienskies.core.game.ships.ShipData

/**
 * A class containing the result of the chunk tracking. **This object is only valid for the tick it was produced in!**
 * Many of the maps/sets will be reused for efficiency's sake.
 */
data class ChunkTrackingInfo(
    val playersToShipsWatchingMap: Map<IPlayer, Object2IntMap<ShipData>>,
    val shipsToPlayersWatchingMap: Long2ObjectMap<MutableSet<IPlayer>>,
    val playersToShipsNewlyWatchingMap: Map<IPlayer, MutableSet<ShipData>>,
    val playersToShipsNoLongerWatchingMap: Map<IPlayer, MutableSet<ShipData>>,
    val shipsToLoad: Set<ShipData>,
    val shipsToUnload: Set<ShipData>,
) {
    fun getShipsPlayerIsWatching(player: IPlayer): Iterable<ShipData> {
        return (playersToShipsWatchingMap[player] ?: emptyMap()).keys
    }
}
