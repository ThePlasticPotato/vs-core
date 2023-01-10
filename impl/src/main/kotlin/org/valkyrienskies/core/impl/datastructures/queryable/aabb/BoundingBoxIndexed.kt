package org.valkyrienskies.core.impl.datastructures.queryable.aabb

import org.joml.primitives.AABBdc

interface BoundingBoxIndexed<T> {

    fun getIntersecting(bb: AABBdc): Iterable<T>

}