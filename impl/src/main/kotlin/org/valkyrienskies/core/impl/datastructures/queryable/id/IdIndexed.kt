package org.valkyrienskies.core.impl.datastructures.queryable.id

interface IdIndexed<T> {

    fun getById(id: Long): T?

}

