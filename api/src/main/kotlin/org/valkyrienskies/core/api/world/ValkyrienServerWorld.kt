package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.bodies.ServerVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef

interface ValkyrienServerWorld : ValkyrienWorld {

    override fun getBody(id: BodyId): ServerVSBody?

    override fun getBodyReference(id: BodyId): VSRef<ServerVSBody>

}