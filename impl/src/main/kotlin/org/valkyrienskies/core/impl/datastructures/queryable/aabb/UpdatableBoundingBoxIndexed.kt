package org.valkyrienskies.core.impl.datastructures.queryable.aabb

import org.joml.primitives.AABBdc

interface UpdatableBoundingBoxIndexed<T> : BoundingBoxIndexed<T> {
    fun update(oldBB: AABBdc, newBB: AABBdc, id: Long)
}