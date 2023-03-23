package org.valkyrienskies.core.impl.entities

import java.util.concurrent.atomic.AtomicLong

class IdAllocator(
    private val nextId: AtomicLong = AtomicLong(1)
) {

    fun nextId(): EntityId {
        val id = nextId.getAndIncrement()
        check(id != 0L)

        return id
    }

}