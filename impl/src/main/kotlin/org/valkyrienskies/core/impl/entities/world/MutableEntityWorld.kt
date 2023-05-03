package org.valkyrienskies.core.impl.entities.world

import org.valkyrienskies.core.api.util.HasId
import org.valkyrienskies.core.impl.entities.EntityId

interface MutableEntityWorld<E : HasId> {
    fun putEntity(entity: E): E?

    fun removeEntity(id: EntityId)
}