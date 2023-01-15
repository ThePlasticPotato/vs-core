package org.valkyrienskies.core.impl.bodies.storage

import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.BaseVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.impl.datastructures.queryable.aabb.BoundingBoxIndexed
import org.valkyrienskies.core.impl.datastructures.queryable.aabb.PhTreeBoundingBoxIndex
import org.valkyrienskies.core.impl.datastructures.queryable.aabb.UpdatableBoundingBoxIndexed
import org.valkyrienskies.core.impl.datastructures.queryable.id.HashIdIndex
import org.valkyrienskies.core.impl.datastructures.queryable.id.IdIndexed

class BodyIndexedStorageImpl<B : BaseVSBody>(
    private val hashIndex: HashIdIndex<B> = HashIdIndex(),
    private val bbIndex: PhTreeBoundingBoxIndex<B> = PhTreeBoundingBoxIndex()
) : MutableBodyIndexedStorage<B>, IdIndexed<B> by hashIndex, UpdatableBoundingBoxIndexed<B> by bbIndex,
    Collection<B> by hashIndex.map.values {

    override fun add(body: B) {
        hashIndex.add(body.id, body)
        bbIndex.add(body.worldAABB, body.id, body)
    }

    override fun remove(id: BodyId) {
        val body = hashIndex.remove(id)
        bbIndex.remove(body.worldAABB, id)
    }
}

interface BodyIndexedStorage<B : BaseVSBody> : Collection<B>, BoundingBoxIndexed<B>, IdIndexed<B>

interface MutableBodyIndexedStorage<B : BaseVSBody> : BodyIndexedStorage<B>, UpdatableBoundingBoxIndexed<B> {
    fun add(body: B)
    fun remove(id: BodyId)
}