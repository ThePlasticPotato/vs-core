package org.valkyrienskies.core.impl.datastructures.queryable.id

interface MutableIdIndexed<T> : IdIndexed<T> {

    fun add(id: Long, value: T)

    fun remove(id: Long, value: T): T

    fun update(oldId: Long, newId: Long)

}