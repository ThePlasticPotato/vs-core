package org.valkyrienskies.core.impl.chunk_tracking

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet.Companion.chunkPosToLong
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet.Companion.longToChunkX
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet.Companion.longToChunkZ
import org.valkyrienskies.core.api.util.functions.IntBinaryConsumer

@JsonIncludeProperties("chunkClaimSet")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class ShipActiveChunksSet constructor(
    private val chunkClaimSet: LongOpenHashSet
) : IShipActiveChunksSet {
    override fun add(chunkX: Int, chunkZ: Int): Boolean {
        return chunkClaimSet.add(chunkPosToLong(chunkX, chunkZ))
    }

    override fun remove(chunkX: Int, chunkZ: Int): Boolean {
        return chunkClaimSet.remove(chunkPosToLong(chunkX, chunkZ))
    }

    override fun contains(chunkX: Int, chunkZ: Int): Boolean {
        return chunkClaimSet.contains(chunkPosToLong(chunkX, chunkZ))
    }

    @Deprecated(
        "Uses boxed integers as parameters and requires Unit return type, bad for performance and Java interop",
        replaceWith = ReplaceWith("forEach(func)")
    )
    override fun iterateChunkPos(func: (Int, Int) -> Unit) {
        val chunkClaimIterator = chunkClaimSet.iterator()
        while (chunkClaimIterator.hasNext()) {
            val currentChunkClaimAsLong = chunkClaimIterator.nextLong()
            val chunkX = longToChunkX(currentChunkClaimAsLong)
            val chunkZ = longToChunkZ(currentChunkClaimAsLong)
            func(chunkX, chunkZ)
        }
    }

    override fun forEach(func: IntBinaryConsumer) {
        val chunkClaimIterator = chunkClaimSet.iterator()
        while (chunkClaimIterator.hasNext()) {
            val currentChunkClaimAsLong = chunkClaimIterator.nextLong()
            val chunkX = longToChunkX(currentChunkClaimAsLong)
            val chunkZ = longToChunkZ(currentChunkClaimAsLong)
            func.accept(chunkX, chunkZ)
        }
    }

    override val size: Int
        get() = chunkClaimSet.size

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            return true
        }
        if (other is ShipActiveChunksSet) {
            return this.chunkClaimSet == other.chunkClaimSet
        }
        return false
    }

    override fun hashCode(): Int {
        return chunkClaimSet.hashCode()
    }

    companion object {
        fun create(): ShipActiveChunksSet {
            return ShipActiveChunksSet(LongOpenHashSet())
        }
    }
}
