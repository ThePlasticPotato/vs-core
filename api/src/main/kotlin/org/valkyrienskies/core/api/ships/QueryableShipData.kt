package org.valkyrienskies.core.api.ships

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.world.properties.DimensionId

interface QueryableShipData<out ShipType : Ship> : Collection<ShipType> {

    /**
     * Returns the ship with the supplied [shipId] if it's in this collection, returns `null`.
     */
    fun getById(shipId: ShipId): ShipType?

    /**
     * Returns the ship whose chunk claim contains [chunkX] and [chunkZ] and is inside of [dimensionId],
     * otherwise returns `null`.
     */
    fun getByChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): ShipType?

    /**
     * Returns an [Iterable] containing every ship with an in-world AABB that intersects the supplied [aabb]
     */
    fun getIntersecting(aabb: AABBdc): Iterable<ShipType>

    /**
     * Returns true if a ship with the supplied [shipId] exists in this collection
     */
    fun contains(shipId: ShipId): Boolean = getById(shipId) != null

    /**
     * Returns true if a ship with the same ID as the supplied [element] exists in this collection
     */
    override fun contains(element: @UnsafeVariance ShipType): Boolean = contains(element.id)

    /**
     * Returns true if the ID of every ship in [elements] is also contained in this collection
     */
    override fun containsAll(elements: Collection<@UnsafeVariance ShipType>): Boolean = elements.all(::contains)

    override fun isEmpty(): Boolean = size == 0

    @Deprecated("renamed", ReplaceWith("getIntersecting(aabb)"))
    fun getShipDataIntersecting(aabb: AABBdc): Iterable<ShipType> = getIntersecting(aabb)

    @Deprecated("renamed", ReplaceWith("getByChunkPos(chunkX, chunkZ, dimensionId)"))
    fun getShipDataFromChunkPos(chunkX: Int, chunkZ: Int, dimensionId: DimensionId): ShipType? =
        getByChunkPos(chunkX, chunkZ, dimensionId)

    @Deprecated(message = "Use the specific functions instead, such as #getById or #iterator")
    val idToShipData: Map<ShipId, ShipType>
}

