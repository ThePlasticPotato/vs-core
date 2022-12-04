package org.valkyrienskies.core.impl.chunk_tracking

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.LongSet
import org.valkyrienskies.core.apigame.world.IPlayer

/**
 * A class containing the result of the chunk tracking. **This object is only valid for the tick it was produced in!**
 * Many of the maps/sets will be reused for efficiency's sake.
 */
data class ChunkTrackingInfo(
    val playersToShipsWatchingMap: Map<IPlayer, Map<org.valkyrienskies.core.impl.api.ServerShipInternal, LongSet>>,
    val shipsToPlayersWatchingMap: Long2ObjectMap<MutableSet<IPlayer>>,
    val playersToShipsNewlyWatchingMap: Map<IPlayer, Set<org.valkyrienskies.core.impl.api.ServerShipInternal>>,
    val playersToShipsNoLongerWatchingMap: Map<IPlayer, Set<org.valkyrienskies.core.impl.api.ServerShipInternal>>,
    val shipsToLoad: Set<org.valkyrienskies.core.impl.api.ServerShipInternal>,
    val shipsToUnload: Set<org.valkyrienskies.core.impl.api.ServerShipInternal>,
) {
    fun getShipsPlayerIsWatching(player: IPlayer): Iterable<org.valkyrienskies.core.impl.api.ServerShipInternal> {
        return (playersToShipsWatchingMap[player] ?: emptyMap()).keys
    }
}
