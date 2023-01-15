package org.valkyrienskies.core.impl.datastructures.queryable.id

interface MutableIdIndexed<T> : IdIndexed<T> {

    fun add(id: Long, value: T)

    fun remove(id: Long): T

    fun update(oldId: Long, newId: Long)

}