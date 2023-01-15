package org.valkyrienskies.core.impl.datastructures.queryable.aabb

import org.joml.primitives.AABBdc

interface MutableBoundingBoxIndexed<T> : UpdatableBoundingBoxIndexed<T> {

    fun add(bb: AABBdc, id: Long, value: T)

    fun remove(bb: AABBdc, id: Long): T

}