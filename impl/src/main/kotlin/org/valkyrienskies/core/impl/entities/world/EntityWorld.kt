package org.valkyrienskies.core.impl.entities.world

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.LongSet
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.impl.entities.EntityId

interface EntityWorld<out E : HasId> {
    val tickNum: Long

    fun getEntity(id: EntityId): E?

    /**
     * This returns a view of all the entities in the world. It is not thread-safe.
     */
    val entities: Collection<E>

    /**
     * This returns a map containing the entities added this tick. The map is mutated only to add entities,
     * a new map is created at the end of the tick.
     */
    val entitiesAddedThisTick: Long2ObjectMap<out E>

    /**
     * This returns a set containing the entities removed this tick. The set is mutated only to add entities,
     * a new set is created at the end of the tick.
     */
    val entitiesRemovedThisTick: LongSet
}