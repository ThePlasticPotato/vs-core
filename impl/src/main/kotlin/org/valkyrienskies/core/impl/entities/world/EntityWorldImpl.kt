package org.valkyrienskies.core.impl.entities.world

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.impl.entities.EntityId

open class EntityWorldImpl<E : HasId> : EntityWorld<E> {

    @Volatile
    override var tickNum = 0L

    private val entitiesMap = Long2ObjectOpenHashMap<E>()

    override var entitiesAddedThisTick = Long2ObjectOpenHashMap<E>()
    override var entitiesRemovedThisTick = LongOpenHashSet()

    override var entities: Collection<E> = entitiesMap.values

    override fun getEntity(id: EntityId): E? {
        return entitiesMap.get(id)
    }

    open fun putEntity(entity: E): E? {
        val prev = entitiesMap.put(entity.id, entity)
        if (prev == null) {
            entitiesAddedThisTick.put(entity.id, entity)
        }
        return prev
    }

    open fun removeEntity(id: EntityId) {
        entitiesMap.remove(id)
        if (entitiesAddedThisTick.remove(id) == null) {
            entitiesRemovedThisTick.add(id)
        }
    }

    open fun tick() {
        tickNum++

        entitiesAddedThisTick = Long2ObjectOpenHashMap()
        entitiesRemovedThisTick = LongOpenHashSet()
    }
}