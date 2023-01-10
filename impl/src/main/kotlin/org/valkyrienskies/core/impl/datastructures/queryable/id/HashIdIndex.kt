package org.valkyrienskies.core.impl.datastructures.queryable.id

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

class HashIdIndex<T> : MutableIdIndexed<T> {

    private val map = Long2ObjectOpenHashMap<T>()

    override fun add(id: Long, value: T) {
        require(map.put(id, value) == null) { "Tried to add object already in index (ID: $id)" }
    }

    override fun remove(id: Long, value: T): T {
        return requireNotNull(map.remove(id)) { "Tried to remove object not in index (ID: $id)" }
    }

    override fun update(oldId: Long, newId: Long) {
        require(map.containsKey(oldId)) { "Tried to update object not index (old ID: $oldId, new ID: $newId)" }
        require(!map.containsKey(newId)) { "Tried to update object but new ID already in index (old ID: $oldId, new ID: $newId) " }

        map.put(newId, map.remove(oldId))
    }

    override fun getById(id: Long): T? {
        return map.get(id)
    }
}