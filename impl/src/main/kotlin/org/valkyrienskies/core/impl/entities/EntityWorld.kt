package org.valkyrienskies.core.impl.entities

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import org.valkyrienskies.core.api.util.HasId

open class EntityWorld<E : HasId>(val idAllocator: IdAllocator) {

    @Volatile
    var tickNum = 0L

    private val entities = Long2ObjectOpenHashMap<E>()

    val entitiesAddedThisTick = Long2ObjectOpenHashMap<E>()
    val entitiesRemovedThisTick = LongOpenHashSet()

    open fun getEntity(id: EntityId): E? {
        return entities.get(id)
    }

    open fun addEntity(entity: E) {
        entities.put(entity.id, entity)
        entitiesAddedThisTick.put(entity.id, entity)
    }

    open fun removeEntity(id: EntityId) {
        entities.remove(id)
        if (entitiesAddedThisTick.remove(id) == null) {
            entitiesRemovedThisTick.add(id)
        }
    }

    open fun tick() {
        tickNum++

        entitiesAddedThisTick.clear()
        entitiesRemovedThisTick.clear()
    }
}